/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.deep.context;

import java.util.Map;

import com.stratio.deep.config.IAerospikeDeepJobConfig;
import com.stratio.deep.rdd.AerospikeJavaRDD;
import com.stratio.deep.rdd.AerospikeRDD;
import org.apache.spark.SparkContext;

/**
 * Created by luca on 17/07/14.
 */
public class AerospikeDeepSparkContext extends DeepSparkContext  {
	AerospikeDeepSparkContext(SparkContext sc) {
		super(sc);
	}

	AerospikeDeepSparkContext(String master, String appName) {
		super(master, appName);
	}

	AerospikeDeepSparkContext(String master, String appName, String sparkHome, String jarFile) {
		super(master, appName, sparkHome, jarFile);
	}

	AerospikeDeepSparkContext(String master, String appName, String sparkHome, String[] jars) {
		super(master, appName, sparkHome, jars);
	}

	AerospikeDeepSparkContext(String master, String appName, String sparkHome, String[] jars, Map<String, String> environment) {
		super(master, appName, sparkHome, jars, environment);
	}

	/**
	 * Builds a new AerospikeJavaRDD.
	 *
	 * @param config the deep configuration object to use to create the new RDD.
	 * @return a new AerospikeJavaRDD
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> AerospikeJavaRDD<T> aerospikeJavaRDD(IAerospikeDeepJobConfig<T> config) {
		return null;
	}

	/**
	 * Builds a new generic AerospikeRDD.
	 *
	 * @param config the deep configuration object to use to create the new RDD.
	 * @return a new generic AerospikeRDD.
	 */
	@SuppressWarnings("unchecked")
	public <T> AerospikeRDD<T> aerospikeRDD(IAerospikeDeepJobConfig<T> config) {

		return null;
	}
}
