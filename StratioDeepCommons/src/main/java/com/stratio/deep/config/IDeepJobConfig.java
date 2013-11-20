package com.stratio.deep.config;

import java.io.Serializable;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.serializer.IDeepSerializer;

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
	
	public abstract IDeepJobConfig<T> initialize();

	/**
	 * Sets the cassandra's hostname
	 * 
	 * @param hostname
	 * @return
	 */
	public abstract IDeepJobConfig<T> host(String hostname);

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
	 * Sets the username to use to login to Cassandra.
	 * 
	 * @param username
	 * @return
	 */
	public abstract IDeepJobConfig<T> username(String username);

	/**
	 * Sets the password to use to login to Cassandra.
	 * 
	 * @param username
	 * @return
	 */
	public abstract IDeepJobConfig<T> password(String password);

	/**
	 * Sets the default "where" filter to use to access ColumnFamily's data.
	 * 
	 * @param username
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
	 * 
	 * 
	 * @param serializer
	 * @return
	 */
	public abstract IDeepJobConfig<T> serializer(String serializerClassName);

	public abstract <P extends IPartitioner<?>> IDeepJobConfig<T> partitioner(String partitionerClassName);

	public abstract String getDefaultFilter();

	public abstract String getKeyspace();

	public abstract Class<T> getEntityClass();

	public abstract String getColumnFamily();

	public abstract String getHost();

	public abstract Integer getPort();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract Job getHadoopJob();

	public abstract IDeepSerializer<T> getSerializer();

	public abstract IDeepJobConfig<T> framedTransportSize(Integer thriftFramedTransportSizeMB);

	public abstract Integer getThriftFramedTransportSizeMB();

	

}