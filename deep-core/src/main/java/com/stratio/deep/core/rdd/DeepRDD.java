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
package com.stratio.deep.core.rdd;

import static com.stratio.deep.commons.utils.Constants.SPARK_RDD_ID;
import static com.stratio.deep.commons.utils.Utils.getExtractorInstance;
import static com.stratio.deep.core.util.ExtractorClientUtil.getExtractorClient;
import static scala.collection.JavaConversions.asScalaIterator;

import java.io.Serializable;

import org.apache.spark.InterruptibleIterator;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;

import scala.collection.Iterator;
import scala.reflect.ClassTag$;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.exception.DeepExtractorinitializationException;
import com.stratio.deep.commons.exception.DeepIOException;
import com.stratio.deep.commons.rdd.IExtractor;

/**
 * Created by rcrespo on 11/08/14.
 */
public class DeepRDD<T> extends RDD<T> implements Serializable {

    private static final long serialVersionUID = -5360986039609466526L;

    private transient IExtractor<T> extractorClient;

    protected Broadcast<DeepJobConfig<T>> config;

    public Broadcast<DeepJobConfig<T>> getConfig() {
        return config;
    }

    public DeepRDD(SparkContext sc, DeepJobConfig<T> config) {
        super(sc, scala.collection.Seq$.MODULE$.empty(), ClassTag$.MODULE$.<T> apply(config
                .getEntityClass()));
        config.putValue(SPARK_RDD_ID, String.valueOf(id()));
        this.config =
                sc.broadcast(config, ClassTag$.MODULE$
                        .<DeepJobConfig<T>> apply(config.getClass()));

        initExtractorClient();

    }

    public DeepRDD(SparkContext sc, Class entityClass) {
        super(sc, scala.collection.Seq$.MODULE$.empty(), ClassTag$.MODULE$.<T> apply(entityClass));
    }

    @Override
    public Iterator<T> compute(Partition split, TaskContext context) {

        this.
                initExtractorClient();

        context.addOnCompleteCallback(new OnComputedRDDCallback(extractorClient));

        extractorClient.initIterator(split, config.getValue());
        java.util.Iterator<T> iterator = new java.util.Iterator<T>() {

            @Override
            public boolean hasNext() {
                return extractorClient.hasNext();
            }

            @Override
            public T next() {
                return extractorClient.next();
            }

            @Override
            public void remove() {
                throw new DeepIOException(
                        "Method not implemented (and won't be implemented anytime soon!!!)");
            }
        };

        return new InterruptibleIterator<>(context, asScalaIterator(iterator));
    }

    @Override
    public Partition[] getPartitions() {
        initExtractorClient();
        return extractorClient.getPartitions(config.getValue());
    }

    /**
     * It tries to get an Extractor Instance, if there is any problem try to instance an extractorClient
     */
    private void initExtractorClient() {
        try {
            if (extractorClient == null) {
                extractorClient = getExtractorInstance(config.getValue());
            }
        } catch (DeepExtractorinitializationException e) {
            extractorClient = getExtractorClient();
        }
    }
}
