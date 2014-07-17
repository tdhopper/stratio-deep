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

package com.stratio.deep.rdd;

import org.apache.spark.api.java.JavaRDD;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

/**
 * Created by luca on 17/07/14.
 */
public class AerospikeJavaRDD<W> extends JavaRDD<W> {
	/**
	 * Default constructor. Constructs a new Java-friendly Cassandra RDD
	 *
	 * @param rdd
	 */
	public AerospikeJavaRDD(AerospikeRDD<W> rdd) {
		super(rdd, ClassTag$.MODULE$.<W>apply(rdd.config.value().getEntityClass()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassTag<W> classTag() {
		return ClassTag$.MODULE$.<W>apply(((AerospikeRDD<W>) this.rdd()).config.value().getEntityClass());
	}
}
