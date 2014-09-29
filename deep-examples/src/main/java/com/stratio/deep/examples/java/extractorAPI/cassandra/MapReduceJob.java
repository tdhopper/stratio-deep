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

package com.stratio.deep.examples.java.extractorAPI.cassandra;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.rdd.RDD;

import com.stratio.deep.cassandra.extractor.CassandraEntityExtractor;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.extractor.server.ExtractorServer;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.testentity.TweetEntity;
import com.stratio.deep.utils.ContextProperties;

import scala.Tuple2;

/**
 * Author: Emmanuelle Raffenne
 * Date..: 13-feb-2014
 */

public final class MapReduceJob {
    private static final Logger LOG = Logger.getLogger(MapReduceJob.class);
    public static List<Tuple2<String, Integer>> results;

    private MapReduceJob() {
    }

    /**
     * Application entry point.
     *
     * @param args the arguments passed to the application.
     */
    public static void main(String[] args) {
        doMain(args);
    }

    /**
     * This is the method called by both main and tests.
     *
     * @param args
     */
    public static void doMain(String[] args) {
        String job = "java:mapReduceJob";

        String KEYSPACENAME = "test";
        String TABLENAME = "tweets";
        String CQLPORT = "9042";
        String RPCPORT = "9160";
        String HOST = "127.0.0.1";

        //Call async the Extractor netty Server
        ExtractorServer.initExtractorServer();

        // Creating the Deep Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
        DeepSparkContext deepContext = new DeepSparkContext(p.getCluster(), job, p.getSparkHome(), p.getJars());

        // Creating a configuration for the RDD and initialize it
        ExtractorConfig<TweetEntity> config = new ExtractorConfig(TweetEntity.class);
        config.setExtractorImplClass(CassandraEntityExtractor.class);
        config.setEntityClass(TweetEntity.class);

        Map<String, Serializable> values = new HashMap<>();
        values.put(ExtractorConstants.KEYSPACE, KEYSPACENAME);
        values.put(ExtractorConstants.TABLE, TABLENAME);
        values.put(ExtractorConstants.CQLPORT, CQLPORT);
        values.put(ExtractorConstants.RPCPORT, RPCPORT);
        values.put(ExtractorConstants.HOST, HOST);

        config.setValues(values);
        // Creating the RDD
        RDD<TweetEntity> rdd = deepContext.createRDD(config);

        // Map stage: Getting key-value pairs from the RDD
        JavaPairRDD<String, Integer> pairsRDD = rdd.toJavaRDD()
                .mapToPair(new PairFunction<TweetEntity, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(TweetEntity t) {
                        return new Tuple2<String, Integer>(t.getAuthor(), 1);
                    }
                });

        // Reduce stage: counting rows
        JavaPairRDD<String, Integer> counts = pairsRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer a, Integer b) {
                return a + b;
            }
        });

        // Fetching the results
        results = counts.collect();

        for (Tuple2<String, Integer> t : results) {
            LOG.info(t._1() + ": " + t._2().toString());
        }

        ExtractorServer.close();
        deepContext.stop();
    }
}
