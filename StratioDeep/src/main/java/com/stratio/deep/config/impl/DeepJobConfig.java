package com.stratio.deep.config.impl;

import java.io.IOException;
import java.lang.annotation.AnnotationTypeMismatchException;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.stratio.deep.annotations.DeepEntity;
import com.stratio.deep.config.DeepJobConfigFactory;
import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.exception.DeepIOException;
import com.stratio.deep.exception.DeepIllegalAccessException;
import com.stratio.deep.serializer.IDeepSerializer;
import com.stratio.deep.serializer.impl.DefaultDeepSerializer;
import com.stratio.deep.util.Constants;

/**
 * Class containing the appropiate configuration for a CassandraRDD.
 * 
 * Remember to call {@link #getConfiguration()} after having configured all the
 * properties.
 * 
 * @author Luca Rosellini <luca@strat.io>
 * 
 */
public final class DeepJobConfig<T extends IDeepType> implements IDeepJobConfig<T> {
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
	
	private transient IPartitioner<?> partitioner = new Murmur3Partitioner();
	private transient IDeepSerializer<T> serializer = new DefaultDeepSerializer<T>();

	private Class<T> entityClass;

	private transient Job hadoopJob;

	private Configuration configuration;

	private void checkInitialized() {
		if (configuration == null) {
			throw new DeepIllegalAccessException(
					"DeepJobConfig has not been initialized!");
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
	public static <T extends IDeepType> IDeepJobConfig<T> create(Class<T> entityClass){
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
	public IDeepJobConfig<T> serializer(IDeepSerializer<T> serializer){
		this.serializer = serializer;
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.stratio.deep.config.IDeepJobConfig#partitioner(org.apache.cassandra.dht.IPartitioner)
	 */
	@Override
	public IDeepJobConfig<T> partitioner(IPartitioner<?> partitioner){
		this.partitioner = partitioner;
		
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
	@Override
	public IDeepSerializer<T> getSerializer() {
		return serializer;
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
			ConfigHelper.setInputPartitioner(c, partitioner.getClass()
					.getCanonicalName());

			ConfigHelper.setOutputColumnFamily(c, keyspace, columnFamily);
			ConfigHelper.setOutputInitialAddress(c, host);
			ConfigHelper.setOutputRpcPort(c, String.valueOf(port));
			ConfigHelper.setOutputPartitioner(c, partitioner.getClass()
					.getCanonicalName());
			
			/* TODO: make this a little bit more configurable, maybe externalize it in a config file */
			ConfigHelper.setThriftFramedTransportSizeInMb(c, 256);

			configuration = c;
		} catch (IOException e) {
			throw new DeepIOException(e);
		}
		return this;
	}
}
