/*
 * Copyright 2014, Stratio.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.stratio.deep.config;

import static com.stratio.deep.rdd.CassandraRDDUtils.createTableQueryGenerator;
import static com.stratio.deep.utils.Utils.quote;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;

import scala.Tuple2;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.stratio.deep.entity.CassandraCell;
import com.stratio.deep.entity.Cell;
import com.stratio.deep.entity.Cells;
import com.stratio.deep.exception.DeepIOException;
import com.stratio.deep.exception.DeepIllegalAccessException;
import com.stratio.deep.exception.DeepIndexNotFoundException;
import com.stratio.deep.exception.DeepNoSuchFieldException;
import com.stratio.deep.utils.Constants;

/**
 * Base class for all config implementations providing default implementations for methods defined
 * in {@link com.stratio.deep.config.IDeepJobConfig}.
 */
public abstract class GenericDeepJobConfig<T> implements IDeepJobConfig<T>, AutoCloseable {

  private static final Logger LOG = Logger
      .getLogger("com.stratio.deep.config.GenericDeepJobConfig");

  private static final long serialVersionUID = -7179376653643603038L;

  /**
   * keyspace name
   */
  private String catalog;

  /**
   * name of the columnFamily from which data will be fetched
   */
  private String table;

  /**
   * Database server host name.
   */
  private String host;

  /**
   * Database server port.
   */
  private Integer port = Constants.DEFAULT_PORT;

  /**
   * Cassandra username. Leave empty if you do not need authentication.
   */
  private String username;

  /**
   * Cassandra password. Leave empty if you do not need authentication.
   */
  private String password;

  /**
   * default "where" filter to use to access ColumnFamily's data.
   */
  private Map<String, Serializable> additionalFilters = new TreeMap<>();

  /**
   * Defines a projection over the CF columns.
   */
  private String[] inputColumns;

  /**
   * holds columns metadata fetched from Cassandra.
   */
  private transient Map<String, Cell> columnDefinitionMap;

  /**
   * Default read consistency level. Defaults to LOCAL_ONE.
   */
  private String readConsistencyLevel = ConsistencyLevel.LOCAL_ONE.name();

  /**
   * Default write consistency level. Defaults to QUORUM.
   */
  private String writeConsistencyLevel = ConsistencyLevel.QUORUM.name();

  /**
   * Enables/Disables auto-creation of column family when writing to Cassandra. By Default we do not
   * create the output column family.
   */
  private Boolean createTableOnWrite = Boolean.FALSE;

  private transient Session session;

  private Boolean isInitialized = Boolean.FALSE;

  private int pageSize = Constants.DEFAULT_PAGE_SIZE;

  protected Boolean isWriteConfig = Boolean.FALSE;

  private int bisectFactor = Constants.DEFAULT_BISECT_FACTOR;

  private Map<String, Object> customConfiguration = new HashMap<>();

  /**
   * {@inheritDoc}
   */
  private synchronized Session getSession() {
    if (session == null) {
      Cluster cluster =
          Cluster.builder().withPort(this.port).addContactPoint(this.host)
              .withCredentials(this.username, this.password).build();

      session = cluster.connect(quote(this.catalog));
    }

    return session;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    LOG.debug("closing " + getClass().getCanonicalName());
    if (session != null) {
      session.close();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @Override protected void finalize() { LOG.debug("finalizing " + getClass().getCanonicalName());
   *           close(); }
   */
  /**
   * Checks if this configuration object has been initialized or not.
   * 
   * @throws com.stratio.deep.exception.DeepIllegalAccessException if not initialized
   */
  protected void checkInitialized() {
    if (!isInitialized) {
      throw new DeepIllegalAccessException("DeepJobConfig has not been initialized!");
    }
  }

  /**
   * Fetches table metadata from the underlying datastore, using DataStax java driver.
   * 
   * @return the table metadata as returned by the driver.
   */
  public TableMetadata fetchTableMetadata() {

    Metadata metadata = getSession().getCluster().getMetadata();
    KeyspaceMetadata ksMetadata = metadata.getKeyspace(quote(this.catalog));

    if (ksMetadata != null) {
      return ksMetadata.getTable(quote(this.table));
    } else {
      return null;
    }
  }

  /**
   * Creates the output column family if not exists. <br/>
   * We first check if the column family exists. <br/>
   * If not, we get the first element from <i>tupleRDD</i> and we use it as a template to get
   * columns metadata.
   * <p>
   * This is a very heavy operation since to obtain the schema we need to get at least one element
   * of the output RDD.
   * </p>
   * 
   * @param tupleRDD the pair RDD.
   */
  public void createOutputTableIfNeeded(RDD<Tuple2<Cells, Cells>> tupleRDD) {

    TableMetadata metadata =
        getSession().getCluster().getMetadata().getKeyspace(this.catalog)
            .getTable(quote(this.table));

    if (metadata == null && !createTableOnWrite) {
      throw new DeepIOException(
          "Cannot write RDD, output table does not exists and configuration object has "
              + "'createTableOnWrite' = false");
    }

    if (metadata != null) {
      return;
    }

    Tuple2<Cells, Cells> first = tupleRDD.first();

    if (first._1() == null || first._1().isEmpty()) {
      throw new DeepNoSuchFieldException("no key structure found on row metadata");
    }
    String createTableQuery =
        createTableQueryGenerator(first._1(), first._2(), this.catalog, quote(this.table));
    getSession().execute(createTableQuery);
    waitForNewTableMetadata();
  }

  /**
   * waits until table metadata is not null
   */
  private void waitForNewTableMetadata() {
    TableMetadata metadata;
    int retries = 0;
    final int waitTime = 100;
    do {
      metadata =
          getSession().getCluster().getMetadata().getKeyspace(this.catalog)
              .getTable(quote(this.table));

      if (metadata != null) {
        continue;
      }

      LOG.warn(String.format("Metadata for new table %s.%s NOT FOUND, waiting %d millis",
          this.catalog, this.table, waitTime));
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        LOG.error("Sleep interrupted", e);
      }

      retries++;

      if (retries >= 10) {
        throw new DeepIOException("Cannot retrieve metadata for the newly created CF ");
      }
    } while (metadata == null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized Map<String, Cell> columnDefinitions() {
    if (columnDefinitionMap != null) {
      return columnDefinitionMap;
    }

    TableMetadata tableMetadata = fetchTableMetadata();

    if (tableMetadata == null && !createTableOnWrite) {
      LOG.warn("Configuration not suitable for writing RDD: output table does not exists and configuration "
          + "object has 'createTableOnWrite' = false");

      return null;
    } else if (tableMetadata == null) {
      return null;
    }

    initColumnDefinitionMap(tableMetadata);

    return columnDefinitionMap;
  }

  private void initColumnDefinitionMap(TableMetadata tableMetadata) {
    columnDefinitionMap = new HashMap<>();

    List<ColumnMetadata> partitionKeys = tableMetadata.getPartitionKey();
    List<ColumnMetadata> clusteringKeys = tableMetadata.getClusteringColumns();
    List<ColumnMetadata> allColumns = tableMetadata.getColumns();

    for (ColumnMetadata key : partitionKeys) {
      Cell metadata =
          CassandraCell.create(key.getName(), key.getType(), Boolean.TRUE, Boolean.FALSE);
      columnDefinitionMap.put(key.getName(), metadata);
    }

    for (ColumnMetadata key : clusteringKeys) {
      Cell metadata =
          CassandraCell.create(key.getName(), key.getType(), Boolean.FALSE, Boolean.TRUE);
      columnDefinitionMap.put(key.getName(), metadata);
    }

    for (ColumnMetadata key : allColumns) {
      Cell metadata =
          CassandraCell.create(key.getName(), key.getType(), Boolean.FALSE, Boolean.FALSE);
      if (!columnDefinitionMap.containsKey(key.getName())) {
        columnDefinitionMap.put(key.getName(), metadata);
      }
    }
    columnDefinitionMap = Collections.unmodifiableMap(columnDefinitionMap);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratio.deep.config.IDeepJobConfig#getColumnFamily()
   */
  @Override
  public String getTable() {
    return table;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratio.deep.config.IDeepJobConfig#getHost()
   */
  @Override
  public String getHost() {
    checkInitialized();
    return host;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public IDeepJobConfig<T> port(Integer port) {

    this.port = port;

    return this;
  }

  @Override
  public String[] getInputColumns() {
    checkInitialized();
    return inputColumns == null ? new String[0] : inputColumns.clone();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratio.deep.config.IDeepJobConfig#getKeyspace()
   */
  @Override
  public String getCatalog() {
    checkInitialized();
    return catalog;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.stratio.deep.config.IDeepJobConfig#getPassword()
   */
  @Override
  public String getPassword() {
    checkInitialized();
    return password;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUsername() {
    checkInitialized();
    return username;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> host(String hostname) {
    this.host = hostname;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> initialize() {
    if (isInitialized) {
      return this;
    }

    if (StringUtils.isEmpty(host)) {
      try {
        host = InetAddress.getLocalHost().getCanonicalHostName();
      } catch (UnknownHostException e) {
        LOG.warn("Cannot resolve local host canonical name, using \"localhost\"");
        host = InetAddress.getLoopbackAddress().getCanonicalHostName();
      }
    }


    validate();

    columnDefinitions();
    isInitialized = Boolean.TRUE;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> inputColumns(String... columns) {
    this.inputColumns = columns;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> catalog(String catalog) {
    this.catalog = catalog;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> password(String password) {
    this.password = password;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> username(String username) {
    this.username = username;

    return this;
  }

  /**
   * Validates if any of the mandatory fields have been configured or not. Throws an
   * {@link IllegalArgumentException} if any of the mandatory properties have not been configured.
   */
  private void validate() {
    validateCassandraParams();

    if (pageSize <= 0) {
      throw new IllegalArgumentException("pageSize cannot be zero");
    }

    if (pageSize > Constants.DEFAULT_MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("pageSize cannot exceed "
          + Constants.DEFAULT_MAX_PAGE_SIZE);
    }

    validateConsistencyLevels();

    TableMetadata tableMetadata = fetchTableMetadata();

    validateTableMetadata(tableMetadata);
    validateAdditionalFilters(tableMetadata);

    if (bisectFactor != Constants.DEFAULT_BISECT_FACTOR && !checkIsPowerOfTwo(bisectFactor)) {
      throw new IllegalArgumentException(
          "Bisect factor should be greater than zero and a power of 2");
    }
  }

  private void validateCassandraParams() {
    if (StringUtils.isEmpty(host)) {
      throw new IllegalArgumentException("host cannot be null");
    }

    if (port == null) {
      throw new IllegalArgumentException("rpcPort cannot be null");
    }

    if (StringUtils.isEmpty(catalog)) {
      throw new IllegalArgumentException("keyspace cannot be null");
    }

    if (StringUtils.isEmpty(table)) {
      throw new IllegalArgumentException("columnFamily cannot be null");
    }
  }

  private void validateTableMetadata(TableMetadata tableMetadata) {

    if (tableMetadata == null && !isWriteConfig) {
      throw new IllegalArgumentException(String.format("Column family {%s.%s} does not exist",
          catalog, table));
    }

    if (tableMetadata == null && !createTableOnWrite) {
      throw new IllegalArgumentException(String.format("Column family {%s.%s} does not exist and "
          + "createTableOnWrite = false", catalog, table));
    }

    if (!ArrayUtils.isEmpty(inputColumns)) {
      for (String column : inputColumns) {
        assert tableMetadata != null;
        ColumnMetadata columnMetadata = tableMetadata.getColumn(column);

        if (columnMetadata == null) {
          throw new DeepNoSuchFieldException("No column with name " + column
              + " has been found on table " + this.catalog + "." + this.table);
        }
      }
    }

  }

  private void validateAdditionalFilters(TableMetadata tableMetadata) {
    for (Map.Entry<String, Serializable> entry : additionalFilters.entrySet()) {
      /* check if there's an index specified on the provided column */
      ColumnMetadata columnMetadata = tableMetadata.getColumn(entry.getKey());

      if (columnMetadata == null) {
        throw new DeepNoSuchFieldException("No column with name " + entry.getKey()
            + " has been found on " + "table " + this.catalog + "." + this.table);
      }

      if (columnMetadata.getIndex() == null) {
        throw new DeepIndexNotFoundException("No index has been found on column "
            + columnMetadata.getName() + " on table " + this.catalog + "." + this.table);
      }
    }
  }

  private void validateConsistencyLevels() {
    if (readConsistencyLevel != null) {
      try {
        ConsistencyLevel.valueOf(readConsistencyLevel);

      } catch (Exception e) {
        throw new IllegalArgumentException("readConsistencyLevel not valid, "
            + "should be one of thos defined in org.apache.cassandra.db.ConsistencyLevel", e);
      }
    }

    if (writeConsistencyLevel != null) {
      try {
        ConsistencyLevel.valueOf(writeConsistencyLevel);

      } catch (Exception e) {
        throw new IllegalArgumentException("writeConsistencyLevel not valid, "
            + "should be one of those defined in org.apache.cassandra.db.ConsistencyLevel", e);
      }
    }
  }

  private boolean checkIsPowerOfTwo(int n) {
    return (n > 0) && ((n & (n - 1)) == 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Boolean isCreateTableOnWrite() {
    return createTableOnWrite;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> createTableOnWrite(Boolean createTableOnWrite) {
    this.createTableOnWrite = createTableOnWrite;

    return this;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, Serializable> getAdditionalFilters() {
    return Collections.unmodifiableMap(additionalFilters);
  }

  @Override
  public int getPageSize() {
    checkInitialized();
    return this.pageSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IDeepJobConfig<T> filterByField(String filterColumnName, Serializable filterValue) {
    /* check if there's an index specified on the provided column */
    additionalFilters.put(filterColumnName, filterValue);
    return this;
  }

  public Map<String, Object> getCustomConfiguration() {
    return customConfiguration;
  }

  public IDeepJobConfig<T> customConfiguration(Map<String, Object> customConfiguration) {

    this.customConfiguration = customConfiguration;

    return this;
  }

}
