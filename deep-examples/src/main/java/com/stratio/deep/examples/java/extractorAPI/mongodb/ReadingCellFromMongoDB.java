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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.mongodb.QueryBuilder;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.mongodb.extractor.MongoCellExtractor;
import com.stratio.deep.utils.ContextProperties;

/**
 * Example class to read a collection from mongoDB
 */
public final class ReadingCellFromMongoDB {
    private static final Logger LOG = Logger.getLogger(ReadingCellFromMongoDB.class);

    private ReadingCellFromMongoDB() {
    }

    public static void main(String[] args) {
        doMain(args);
    }

    public static void doMain(String[] args) {
        String job = "java:readingCellFromMongoDB";

        String host = "127.0.0.1:27017";

        String database = "test";
        String inputCollection = "input";

        initExtractorServer();

        // Creating the Deep Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
        DeepSparkContext deepContext = new DeepSparkContext(p.getCluster(), job, p.getSparkHome(),
                p.getJars());

        QueryBuilder query = QueryBuilder.start();
        //        query.and("number").greaterThan(27).lessThan(30);

        BSONObject bsonSort = new BasicBSONObject();
        bsonSort.put("number", 1);

        BSONObject bsonFields = new BasicBSONObject();
        bsonFields.put("number", 1);
        bsonFields.put("text", 1);
        bsonFields.put("_id", 0);
        //TODO review

        ExtractorConfig<Cells> config = new ExtractorConfig();

        config.setExtractorImplClass(MongoCellExtractor.class);
        Map<String, Serializable> values = new HashMap<>();
        values.put("database", database);
        values.put("collection", inputCollection);
        values.put("host", host);

        config.setValues(values);

        RDD<Cells> inputRDDEntity = deepContext.createRDD(config);

        LOG.info("count : " + inputRDDEntity.count());

        LOG.info("prints first cell  : " + inputRDDEntity.first());

        stopExtractorServer();

        deepContext.stop();
    }
}
