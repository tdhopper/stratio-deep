package com.stratio.deep.cql;

import org.apache.hadoop.conf.Configuration;

/**
 * Created by luca on 13/01/14.
 */
public class DeepConfigHelper {

    public static final String OUTPUT_BATCH_SIZE = "output.batch.size";
    private static final int DEFAULT_OUTPUT_BATCH_SIZE = 100;


    public static int getOutputBatchSize(Configuration conf){
        return conf.getInt(OUTPUT_BATCH_SIZE, DEFAULT_OUTPUT_BATCH_SIZE);
    }


    public static void setOutputBatchSize(Configuration conf, int batchSize){
        if (batchSize > 0){
            conf.setInt(OUTPUT_BATCH_SIZE, batchSize);
        }
    }

}
