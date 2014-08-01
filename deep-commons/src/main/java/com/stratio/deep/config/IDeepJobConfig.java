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

import java.io.Serializable;
import java.util.Map;

import com.stratio.deep.entity.Cell;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 * 
 * @param <T> the generic type associated to this configuration object.
 */
public interface IDeepJobConfig<T> extends Serializable {

  /**
   * Returns the password needed to authenticate to the remote datastore cluster.
   * 
   * @return the password used to login to the remote cluster.
   */
  String getPassword();

  /**
   * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is
   * the column name, and the value is the {@link com.stratio.deep.entity.Cell} containing column's
   * metadata.
   * 
   * @return the map of column names and the corresponding Cell object containing its metadata.
   */
  Map<String, Cell> columnDefinitions();

  /**
   * Sets the number of rows to retrieve for each page of data fetched from Cassandra.<br/>
   * Defaults to 1000 rows.
   * 
   * @param pageSize the number of rows per page
   * @return this configuration object.
   */
  IDeepJobConfig<T> pageSize(int pageSize);


  /* Getters */

  /**
   * Returns the underlying testentity class used to map the Cassandra Column family.
   * 
   * @return the entity class object associated to this configuration object.
   */
  Class<T> getEntityClass();

  /**
   * Returns the hostname of the cassandra server.
   * 
   * @return the endpoint of the cassandra server.
   */
  String getHost();

  /**
   * Returns the list of column names that will be fetched from the underlying datastore.
   * 
   * @return the array of column names that will be retrieved from the data store.
   */
  String[] getInputColumns();

  /**
   * Returns the username used to authenticate to the cassandra server. Defaults to the empty
   * string.
   * 
   * @return the username to use to login to the remote server.
   */
  String getUsername();

  /**
   * Sets the datastore hostname
   * 
   * @param hostname the cassandra server endpoint.
   * @return this object.
   */
  IDeepJobConfig<T> host(String hostname);

  /**
   * Initialized the current configuration object.
   * 
   * @return this object.
   */
  IDeepJobConfig<T> initialize();

  /**
   * Defines a projection over the CF columns. <br/>
   * Key columns will always be returned, even if not specified in the columns input array.
   * 
   * @param columns list of columns we want to retrieve from the datastore.
   * @return this object.
   */
  IDeepJobConfig<T> inputColumns(String... columns);

  /**
   * Sets the password to use to login to Cassandra. Leave empty if you do not need authentication.
   * 
   * @return this object.
   */
  IDeepJobConfig<T> password(String password);

  /**
   * /** Sets the username to use to login to Cassandra. Leave empty if you do not need
   * authentication.
   * 
   * @return this object.
   */
  IDeepJobConfig<T> username(String username);

  /**
   * Returns the maximum number of rows that will be retrieved when fetching data pages from
   * Cassandra.
   * 
   * @return the page size
   */
  int getPageSize();

  /**
   * Sets the underlying datastore table or collection from which data will be read from.
   * 
   * @param table the table name.
   * @return this configuration object.
   */
  IDeepJobConfig<T> table(String table);

  /**
   * Returns the name of the configured column family. Column family name is case sensitive.
   * 
   * @return the table name.
   */
  String getTable();


  /**
   * Sets database catalog.
   * 
   * @param catalog the catalog to use.
   * @return this object.
   */
  IDeepJobConfig<T> catalog(String catalog);

  /**
   * Returns the name of the current catalog.
   * 
   * @return the catalog name.
   */
  String getCatalog();

  /**
   * Returns whether or not in this configuration object we specify to automatically create the
   * output table.
   * 
   * @return true if this configuration object has been configured to create missing tables on
   *         writes.
   */
  Boolean isCreateTableOnWrite();

  /**
   * Whether or not to create the output table on write.<br/>
   * .
   * <p/>
   * Defaults to FALSE.
   * 
   * @param createTableOnWrite a boolean that tells this configuration object to create missing
   *        tables on write.
   * @return this configuration object.
   */
  IDeepJobConfig<T> createTableOnWrite(Boolean createTableOnWrite);

  /**
   * Adds a new filter for the underlying datastore.<br/>
   * Once a new filter has been added, all subsequent queries generated to the underlying datastore
   * will include the filter on the specified column called <i>filterColumnName</i>. Before
   * propagating the filter we check if an index exists.
   * 
   * @param filterColumnName the name of the columns (as known by the datastore) to filter on.
   * @param filterValue the value of the filter to use. May be any expression, depends on the actual
   *        index implementation.
   * @return this configuration object.
   * @throws com.stratio.deep.exception.DeepIndexNotFoundException if the specified field has not
   *         been indexed in the database.
   * @throws com.stratio.deep.exception.DeepNoSuchFieldException if the specified field is not a
   *         valid column in the database.
   */
  public IDeepJobConfig<T> filterByField(String filterColumnName, Serializable filterValue);

  /**
   * Returns the map of custom configuration for the current database.
   * 
   * @return Database specific configuration map.
   */
  public Map<String, Object> getCustomConfiguration();

  /**
   * Sets the custom configuration for the current database.
   * 
   * @param customConfiguration Database specific configuration map.
   */
  public IDeepJobConfig<T> customConfiguration(Map<String, Object> customConfiguration);

  /**
   * @return
   */
  Integer getPort();

  /**
   * @param port
   * @return
   */
  IDeepJobConfig<T> port(Integer port);
}
