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

package com.stratio.deep.examples.java.extractorAPI.es;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;

import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.extractor.ESCellExtractor;
import com.stratio.deep.utils.ContextProperties;

/**
 * Example class to read a collection from ES
 */
public final class ReadingCellFromES {
    private static final Logger LOG = Logger.getLogger(ReadingCellFromES.class);
    private static Long counts;

    private ReadingCellFromES() {
    }

    public static void main(String[] args) {
        doMain(args);
    }

    public static void doMain(String[] args) {
        String job = "java:ReadingCellWithES";
        String host = "localhost:9200";
        String database = "entity/output";
        String index = "book";
        String type = "test";

        // Creating the Deep Context
        ContextProperties p = new ContextProperties(args);
        DeepSparkContext deepContext = new DeepSparkContext(p.getCluster(), job, p.getSparkHome(), p.getJars());

        // Creating a configuration for the Extractor and initialize it
        ExtractorConfig<Cells> config = new ExtractorConfig();

        Map<String, Serializable> values = new HashMap<>();

        values.put(ExtractorConstants.DATABASE, database);
        values.put(ExtractorConstants.HOST, host);

        config.setExtractorImplClass(ESCellExtractor.class);
        config.setValues(values);

        // Creating the RDD
        RDD<Cells> rdd = deepContext.createRDD(config);

        counts = rdd.count();

        LOG.info("--------------------------------- Num of rows: " + counts);
        LOG.info("--------------------------------- Num of rows: " + rdd.first());

        deepContext.stop();
    }
}
