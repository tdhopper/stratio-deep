package com.stratio.deep.config.impl;

import com.stratio.deep.annotations.DeepEntity;
import com.stratio.deep.config.DeepJobConfigFactory;
import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.exception.DeepGenericException;
import com.stratio.deep.exception.DeepIOException;
import com.stratio.deep.exception.DeepIllegalAccessException;
import com.stratio.deep.serializer.IDeepSerializer;
import com.stratio.deep.util.Constants;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlConfigHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.annotation.AnnotationTypeMismatchException;

/**
 * Class containing the appropiate configuration for a CassandraRDD.
 * <p/>
 * Remember to call {@link #getConfiguration()} after having configured all the
 * properties.
 *
 * @author Luca Rosellini <luca@strat.io>
 */
public final class DeepJobConfig<T extends IDeepType> implements IDeepJobConfig<T> {

    private transient Logger logger = Logger.getLogger(getClass());

    private static final long serialVersionUID = 4490719746563473495L;

    public DeepJobConfig(Class<T> entityClass) {
        super();
        this.entityClass = entityClass;
    }

    public DeepJobConfig() {
        super();
    }

    private String keyspace;
    private String columnFamily;
    private String host = Constants.DEFAULT_CASSANDRA_HOST;
    private Integer port = Constants.DEFAULT_CASSANDRA_CQL_PORT;
    private String username;
    private String password;
    private String defaultFilter;
    private String[] inputColumns;

    private String outputKeyspace;
    private String outputColumnFamily;

    private Integer thriftFramedTransportSizeMB = 256;

    private String partitionerClassName = "org.apache.cassandra.dht.Murmur3Partitioner";
    private String serializerClassName = "com.stratio.deep.serializer.impl.DefaultDeepSerializer";

    private Class<T> entityClass;

    private transient Job hadoopJob;

    private transient Configuration configuration;

    private void checkInitialized() {
        if (configuration == null) {
            throw new DeepIllegalAccessException("DeepJobConfig has not been initialized!");
        }
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#validate()
     */
    @Override
    public void validate() {
        if (StringUtils.isEmpty(host)) {
            throw new IllegalArgumentException("host cannot be null");
        }

        if (port == null) {
            throw new IllegalArgumentException("port cannot be null");
        }

        if (StringUtils.isEmpty(keyspace)) {
            throw new IllegalArgumentException("keyspace cannot be null");
        }

        if (StringUtils.isEmpty(columnFamily)) {
            throw new IllegalArgumentException("columnFamily cannot be null");
        }

        if (entityClass == null) {
            throw new IllegalArgumentException("entity class cannot be null");
        }

        if (!entityClass.isAnnotationPresent(DeepEntity.class)) {
            throw new AnnotationTypeMismatchException(null, entityClass.getCanonicalName());
        }
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#initialize()
     */
    @Override
    public Configuration getConfiguration() {
        if (configuration != null) {
            return configuration;
        }

        initialize();

        return configuration;
    }

    /**
     * Factory method.
     *
     * @return
     */
    public static <T extends IDeepType> IDeepJobConfig<T> create(Class<T> entityClass) {
        IDeepJobConfig<T> res = DeepJobConfigFactory.create(entityClass);
        return res;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#host(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> host(String hostname) {
        this.host = hostname;

        return this;
    }

    /*
     * (non-Jvadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#inputColumns(java.lang.String[])
     */
    @Override
    public IDeepJobConfig<T> inputColumns(String... columns) {
        this.inputColumns = columns;

        return this;
    }

    /*

     */
    @Override
    public IDeepJobConfig<T> outputColumnFamily(String outputColumnFamily) {
        this.outputColumnFamily = outputColumnFamily;
        return this;
    }

    @Override
    public IDeepJobConfig<T> outputKeyspace(String outputKeyspace) {
        this.outputKeyspace = outputKeyspace;
        return this;
    }


    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#keyspace(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> keyspace(String keyspace) {
        this.keyspace = keyspace;
        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#port(java.lang.Integer)
     */
    @Override
    public IDeepJobConfig<T> port(Integer port) {
        this.port = port;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#columnFamily(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> columnFamily(String columnFamily) {
        this.columnFamily = columnFamily;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#username(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> username(String username) {
        this.username = username;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#password(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> password(String password) {
        this.password = password;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#defaultFilter(java.lang.String)
     */
    @Override
    public IDeepJobConfig<T> defaultFilter(String defaultFilter) {
        this.defaultFilter = defaultFilter;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#serializer(com.stratio.deep.serializer.IDeepSerializer)
     */
    @Override
    public IDeepJobConfig<T> serializer(String serializerClassName) {
        this.serializerClassName = serializerClassName;

        return this;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#partitioner(org.apache.cassandra.dht.IPartitioner)
     */
    @Override
    public <P extends IPartitioner<?>> IDeepJobConfig<T> partitioner(String partitionerClassName) {
        this.partitionerClassName = partitionerClassName;
        return this;
    }

    @Override
    public IDeepJobConfig<T> framedTransportSize(Integer thriftFramedTransportSizeMB) {
        this.thriftFramedTransportSizeMB = thriftFramedTransportSizeMB;

        return this;
    }


    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getDefaultFilter()
     */
    @Override
    public String getDefaultFilter() {
        checkInitialized();
        return defaultFilter;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getKeyspace()
     */
    @Override
    public String getKeyspace() {
        checkInitialized();
        return keyspace;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getEntityClass()
     */
    @Override
    public Class<T> getEntityClass() {
        checkInitialized();
        return entityClass;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getColumnFamily()
     */
    @Override
    public String getColumnFamily() {
        checkInitialized();
        return columnFamily;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getHost()
     */
    @Override
    public String getHost() {
        checkInitialized();
        return host;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getPort()
     */
    @Override
    public Integer getPort() {
        checkInitialized();
        return port;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getUsername()
     */
    @Override
    public String getUsername() {
        checkInitialized();
        return username;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getPassword()
     */
    @Override
    public String getPassword() {
        checkInitialized();
        return password;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getHadoopJob()
     */
    @Override
    public Job getHadoopJob() {
        checkInitialized();
        return hadoopJob;
    }

    /* (non-Javadoc)
     * @see com.stratio.deep.config.IDeepJobConfig#getSerializer()
     */
    @SuppressWarnings("unchecked")
    @Override
    public IDeepSerializer<T> getSerializer() {

        try {
            return (IDeepSerializer<T>) Class.forName(serializerClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            logger.error(e.getMessage(), e);

            throw new DeepGenericException(e);
        }
    }

    @Override
    public IDeepJobConfig<T> initialize() {
        if (configuration != null) {
            return this;
        }
        validate();

        try {
            hadoopJob = new Job();
            Configuration c = hadoopJob.getConfiguration();

            ConfigHelper.setInputColumnFamily(c, keyspace, columnFamily, false);
            ConfigHelper.setInputInitialAddress(c, host);
            ConfigHelper.setInputRpcPort(c, String.valueOf(port));
            ConfigHelper.setInputPartitioner(c, partitionerClassName);

            if (StringUtils.isNotEmpty(defaultFilter)) {
                CqlConfigHelper.setInputWhereClauses(c, defaultFilter);
            }

            if (!ArrayUtils.isEmpty(inputColumns)) {
                CqlConfigHelper.setInputColumns(c, StringUtils.join(inputColumns, ","));
            }

            if (StringUtils.isNotEmpty(outputKeyspace)) {
                ConfigHelper.setOutputKeyspace(c, outputKeyspace);
            } else {
                ConfigHelper.setOutputKeyspace(c, keyspace);
            }

            if (StringUtils.isNotEmpty(outputColumnFamily)) {
                CqlConfigHelper.setOutputCql(c, generateOutputQuery());
            }

            //DeepConfigHelper.setOutputBatchSize(c, 10);

            ConfigHelper.setOutputColumnFamily(c, keyspace, columnFamily);
            ConfigHelper.setOutputInitialAddress(c, host);
            ConfigHelper.setOutputRpcPort(c, String.valueOf(port));
            ConfigHelper.setOutputPartitioner(c, partitionerClassName);

            ConfigHelper.setThriftFramedTransportSizeInMb(c, thriftFramedTransportSizeMB);

            configuration = c;
        } catch (IOException e) {
            throw new DeepIOException(e);
        }
        return this;
    }


    @Override
    public Integer getThriftFramedTransportSizeMB() {
        return thriftFramedTransportSizeMB;
    }

    @Override
    public String getOutputColumnFamily() {
        return outputColumnFamily;
    }

    @Override
    public String getOutputKeyspace() {
        return outputKeyspace;
    }

    @Override
    public String generateOutputQuery() {
        return outputColumnFamily;
    }
}
