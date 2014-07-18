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

import java.nio.ByteBuffer;
import java.util.Map;

import com.stratio.deep.config.IAerospikeDeepJobConfig;
import com.stratio.deep.utils.Pair;
import org.apache.spark.Dependency;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.reflect.ClassTag;

/**
 * Created by luca on 17/07/14.
 */
public abstract class AerospikeRDD<T> extends RDD<T> {

	Broadcast<IAerospikeDeepJobConfig<T>> config;

	protected abstract T transformElement(Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> elem);

	public AerospikeRDD(SparkContext sc, Seq<Dependency<?>> deps, ClassTag<T> evidence$1) {
		super(sc, deps, evidence$1);
	}

	public AerospikeRDD(RDD<?> oneParent, ClassTag<T> evidence$2) {
		super(oneParent, evidence$2);
	}

	@Override
	public Iterator<T> compute(Partition split, TaskContext context) {
		return null;
	}

	@Override
	public Partition[] getPartitions() {
		return new Partition[0];
	}

	@Override
	public Seq<String> getPreferredLocations(Partition split) {
		return null;
	}
}