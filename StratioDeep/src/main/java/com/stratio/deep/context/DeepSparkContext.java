package com.stratio.deep.context;

import java.util.Map;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.rdd.CassandraJavaRDD;
import com.stratio.deep.rdd.CassandraRDD;
import org.apache.spark.rdd.RDD;

/**
 * Entry point to the Cassandra-aware Spark context.
 * 
 * @author Luca Rosellini <luca@strat.io>
 *
 */
public class DeepSparkContext extends JavaSparkContext {
	
	/**
	 * Overridden superclass constructor.
	 * 
	 * @param master
	 * @param appName
	 * @param sparkHome
	 * @param jarFile
	 */
	public DeepSparkContext(String master, String appName, String sparkHome,
			String jarFile) {
		super(master, appName, sparkHome, jarFile);
	}

	/**
	 * Overridden superclass constructor.
	 * 
	 * @param master
	 * @param appName
	 * @param sparkHome
	 * @param jars
	 * @param environment
	 */
	public DeepSparkContext(String master, String appName, String sparkHome,
			String[] jars, Map<String, String> environment) {
		super(master, appName, sparkHome, jars, environment);
	}

	/**
	 * Overridden superclass constructor.
	 * 
	 * @param master
	 * @param appName
	 * @param sparkHome
	 * @param jars
	 */
	public DeepSparkContext(String master, String appName, String sparkHome,
			String[] jars) {
		super(master, appName, sparkHome, jars);
	}

	/**
	 * Overridden superclass constructor.
	 * 
	 * @param master
	 * @param appName
	 */
	public DeepSparkContext(String master, String appName) {
		super(master, appName);
	}

	/**
	 * Overridden superclass constructor.
	 * 
	 * @param sc
	 */
	public DeepSparkContext(SparkContext sc) {
		super(sc);
	}

  /**
   * Builds a new CassandraRDD.
   *
   * @param config
   * @return
   */
  public <T extends IDeepType> CassandraRDD<T> cassandraRDD(RDD<T> oneParent, IDeepJobConfig<T> config){
    return new CassandraRDD<T>(oneParent,config);
  }

	/**
	 * Builds a new CassandraRDD.
	 * 
	 * @param config
	 * @return
	 */
	public <T extends IDeepType> CassandraRDD<T> cassandraRDD(IDeepJobConfig<T> config){
		return new CassandraRDD<T>(sc(),config);
	}
	
	/**
	 * Builds a java-friendly CassandraRDD.
	 * 
	 * @param config
	 * @return
	 */
	public <T extends IDeepType> CassandraJavaRDD<T> cassandraJavaRDD(IDeepJobConfig<T> config){
		return new CassandraJavaRDD<T>(cassandraRDD(config));
	}
}
