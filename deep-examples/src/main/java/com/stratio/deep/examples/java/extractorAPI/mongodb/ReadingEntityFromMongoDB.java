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

package com.stratio.deep.examples.java.extractorAPI.mongodb;

import static com.stratio.deep.commons.extractor.server.ExtractorServer.initExtractorServer;
import static com.stratio.deep.commons.extractor.server.ExtractorServer.stopExtractorServer;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;

import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.core.entity.MessageTestEntity;
import com.stratio.deep.mongodb.extractor.MongoEntityExtractor;
import com.stratio.deep.utils.ContextProperties;

import scala.Tuple2;

/**
 * Example class to read an entity from mongoDB
 */
public final class ReadingEntityFromMongoDB {
    private static final Logger LOG = Logger.getLogger(ReadingEntityFromMongoDB.class);
    public static List<Tuple2<String, Integer>> results;

    private ReadingEntityFromMongoDB() {
    }

    public static void main(String[] args) {
        doMain(args);
    }

    public static void doMain(String[] args) {
        String job = "java:readingEntityFromMongoDB";

        String host = "localhost:27017";

        String database = "test";
        String inputCollection = "input";

        initExtractorServer();

        // Creating the Deep Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
        DeepSparkContext deepContext = new DeepSparkContext(p.getCluster(), job, p.getSparkHome(),
                p.getJars());

        ExtractorConfig<MessageTestEntity> inputConfigEntity = new ExtractorConfig<>(MessageTestEntity.class);
        inputConfigEntity.putValue(ExtractorConstants.HOST, host).putValue(ExtractorConstants.DATABASE, database)
                .putValue(ExtractorConstants.COLLECTION, inputCollection);
        inputConfigEntity.setExtractorImplClass(MongoEntityExtractor.class);

        RDD<MessageTestEntity> inputRDDEntity = deepContext.createRDD(inputConfigEntity);

        LOG.info("count : " + inputRDDEntity.cache().count());
        LOG.info("count : " + inputRDDEntity.first());

        stopExtractorServer();

        deepContext.stop();

    }
}
