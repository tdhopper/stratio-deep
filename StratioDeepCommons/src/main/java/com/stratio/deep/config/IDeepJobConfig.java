package com.stratio.deep.config;

import java.io.Serializable;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.serializer.IDeepSerializer;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 *
 * @param <T>
 */
public interface IDeepJobConfig<T extends IDeepType> extends Serializable {

	/**
	 * Validates if any of the mandatory fields have been configured or not.
	 * Throws an {@link IllegalArgumentException} if any of the mandatory
	 * properties have not been configured.
	 */
	public abstract void validate();

	/**
	 * Validates and initializes this configuration object.
	 */
	public abstract Configuration getConfiguration();

  /**
   * Initialized the current configuration object.
   *
   * @return
   */
	public abstract IDeepJobConfig<T> initialize();

	/**
	 * Sets the cassandra's hostname
	 * 
	 * @param hostname
	 * @return
	 */
	public abstract IDeepJobConfig<T> host(String hostname);

    /**
     * Sets the name of the output keyspace. If not specified we assume
     * it's the same as the input keyspace.
     *
     * @param outputKeyspace
     * @return
     */
    public abstract IDeepJobConfig<T> outputKeyspace(String outputKeyspace);

    /**
	 * Sets Cassandra Keyspace. 
	 * 
	 * @param keyspace
	 * @return
	 */
	public abstract IDeepJobConfig<T> keyspace(String keyspace);

	/**
	 * Sets cassandra host port.
	 * 
	 * @param port
	 * @return
	 */
	public abstract IDeepJobConfig<T> port(Integer port);

	/**
	 * Sets the cassandra CF from which data will be read from.
	 * 
	 * @param columnFamily
	 * @return
	 */
	public abstract IDeepJobConfig<T> columnFamily(String columnFamily);

	/**
	 * Sets the username to use to login to Cassandra. Leave empty if you do not need authentication.
	 * 
	 * @param username
	 * @return
	 */
	public abstract IDeepJobConfig<T> username(String username);

	/**
	 * Sets the password to use to login to Cassandra. Leave empty if you do not need authentication.
	 * 
	 * @param password
	 * @return
	 */
	public abstract IDeepJobConfig<T> password(String password);

	/**
	 * Sets the default "where" filter to use to access ColumnFamily's data.
	 * 
	 * @param defaultFilter
	 * @return
	 */
	public abstract IDeepJobConfig<T> defaultFilter(String defaultFilter);

	/**
	 * Defines a projection over the CF columns.
	 * 
	 * @param columns
	 * @return
	 */
	public abstract IDeepJobConfig<T> inputColumns(String... columns);

    /**
     * Sets the name of the output column family.
     * This implementation assumes the CF already exists and column names
     * match the names of the fields contained in the RDD that we want to write.
     *
     * @param outputColumnFamily
     * @return
     */
    public abstract IDeepJobConfig<T> outputColumnFamily(String outputColumnFamily);

	/**
	 * Let's the user specify an alternative serializer. The default one is
   * com.stratio.deep.serializer.impl.DefaultDeepSerializer.
	 * 
	 * @param serializerClassName
	 * @return
	 */
	public abstract IDeepJobConfig<T> serializer(String serializerClassName);

  /**
   * Let's the user specify an alternative partitioner class. The default partitioner is
   * org.apache.cassandra.dht.Murmur3Partitioner.
   *
   * @param partitionerClassName
   * @param <P>
   * @return
   */
	public abstract <P extends IPartitioner<?>> IDeepJobConfig<T> partitioner(String partitionerClassName);

  /**
   * Sets the default thrift frame size.
   * @param thriftFramedTransportSizeMB
   * @return
   */
  public abstract IDeepJobConfig<T> framedTransportSize(Integer thriftFramedTransportSizeMB);

  /* Getters */

  public abstract String getDefaultFilter();

	public abstract String getKeyspace();

	public abstract Class<T> getEntityClass();

	public abstract String getColumnFamily();

	public abstract String getHost();

	public abstract Integer getPort();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract Job getHadoopJob();

  public abstract String getPartitionerClassName();

  public abstract String getSerializerClassName();

  public abstract IDeepSerializer<T> getSerializer();

  public abstract String[] getInputColumns();

  public abstract Integer getThriftFramedTransportSizeMB();

	public abstract String getOutputColumnFamily();

  public abstract String getOutputKeyspace();
}