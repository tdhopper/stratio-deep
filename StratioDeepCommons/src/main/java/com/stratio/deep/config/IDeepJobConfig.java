package com.stratio.deep.config;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.serializer.IDeepSerializer;

public interface IDeepJobConfig<T extends IDeepType> {

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

	public abstract IDeepJobConfig<T> username(String username);

	public abstract IDeepJobConfig<T> password(String password);

	public abstract IDeepJobConfig<T> defaultFilter(String defaultFilter);

	public abstract IDeepJobConfig<T> serializer(IDeepSerializer<T> serializer);

	public abstract IDeepJobConfig<T> partitioner(IPartitioner<?> partitioner);

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

}