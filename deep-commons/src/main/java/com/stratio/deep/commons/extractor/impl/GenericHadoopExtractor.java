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

package com.stratio.deep.commons.extractor.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.rdd.NewHadoopPartition;

import scala.Tuple2;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.config.HadoopDeepJobConfig;
import com.stratio.deep.commons.exception.DeepGenericException;
import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.commons.utils.Constants;
import com.stratio.deep.commons.utils.DeepSparkHadoopMapReduceUtil;

/**
 * Created by rcrespo on 26/08/14.
 */
public abstract class GenericHadoopExtractor<T, K, V, KOut, VOut> implements IExtractor<T> {

    protected DeepJobConfig<T> deepJobConfig;

    protected transient RecordReader<K, V> reader;

    protected transient RecordWriter<KOut, VOut> writer;

    protected transient InputFormat<K, V> inputFormat;

    protected transient OutputFormat<KOut, VOut> outputFormat;

    protected transient String jobTrackerId;

    protected transient TaskAttemptContext hadoopAttemptContext;

    protected boolean havePair = false;

    protected boolean finished = false;

    protected transient JobID jobId = null;

    private static final Logger LOG = Logger.getLogger(GenericHadoopExtractor.class);

    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        jobTrackerId = formatter.format(new Date());

    }

    @Override
    public <W extends DeepJobConfig<T>> Partition[] getPartitions(W deepJobConfig) {

        int id = deepJobConfig.getInteger(Constants.SPARK_RDD_ID);
        jobId = new JobID(jobTrackerId, id);

        HadoopDeepJobConfig<T> hadoopDeepJobConfig = (HadoopDeepJobConfig<T>) deepJobConfig;
        Configuration conf = hadoopDeepJobConfig.getHadoopConfiguration();

        JobContext jobContext = DeepSparkHadoopMapReduceUtil.newJobContext(conf, jobId);

        try {
            List<InputSplit> splits = inputFormat.getSplits(jobContext);

            Partition[] partitions = new Partition[(splits.size())];
            for (int i = 0; i < splits.size(); i++) {
                partitions[i] = new NewHadoopPartition(id, i, splits.get(i));
            }

            return partitions;

        } catch (IOException | InterruptedException | RuntimeException e) {
            LOG.error("Impossible to calculate partitions " + e.getMessage());
            throw new DeepGenericException("Impossible to calculate partitions " + e.getMessage());
        }

    }

    @Override
    public boolean hasNext() {
        if (!finished && !havePair) {
            try {
                finished = !reader.nextKeyValue();
            } catch (IOException | InterruptedException e) {
                LOG.error("Impossible to get hasNext " + e.getMessage());
                throw new DeepGenericException("Impossible to get hasNext " + e.getMessage());
            }
            havePair = !finished;

        }
        return !finished;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException("End of stream");
        }
        havePair = false;

        Tuple2<K, V> tuple = null;
        try {
            return transformElement(new Tuple2<>(reader.getCurrentKey(), reader.getCurrentValue()),
                    deepJobConfig);
        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to get next value " + e.getMessage());
            throw new DeepGenericException("Impossible to get next value " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close(hadoopAttemptContext);
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to close RecordReader " + e.getMessage());
            throw new DeepGenericException("Impossible to close RecordReader " + e.getMessage());
        }
    }

    @Override
    public <W extends DeepJobConfig<T>> void initIterator(Partition dp, W deepJobConfig) {

        HadoopDeepJobConfig<T> hadoopDeepJobConfig = (HadoopDeepJobConfig<T>) deepJobConfig;

        int id = deepJobConfig.getInteger(Constants.SPARK_RDD_ID);
        NewHadoopPartition split = (NewHadoopPartition) dp;

        TaskAttemptID attemptId = DeepSparkHadoopMapReduceUtil.newTaskAttemptID(jobTrackerId, id, true, split.index(),
                0);

        TaskAttemptContext hadoopAttemptContext = DeepSparkHadoopMapReduceUtil.newTaskAttemptContext(
                hadoopDeepJobConfig.getHadoopConfiguration(), attemptId);

        try {
            reader = inputFormat.createRecordReader(split.serializableHadoopSplit().value(), hadoopAttemptContext);
            reader.initialize(split.serializableHadoopSplit().value(), hadoopAttemptContext);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public abstract <W extends DeepJobConfig<T>> T transformElement(Tuple2<K, V> tuple, W deepJobConfig);

    @Override
    public <W extends DeepJobConfig<T>> IExtractor getExtractorInstance(W config) {
        return this;
    }

    @Override
    public void saveRDD(T t) {
        Tuple2<KOut, VOut> tuple = transformElement(t);
        try {
            writer.write(tuple._1(), tuple._2());

        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to saveRDD " + e.getMessage());
            throw new DeepGenericException("Impossible to saveRDD " + e.getMessage());
        }
        return;
    }

    @Override
    public <W extends DeepJobConfig<T>> void initSave(W deepJobConfig, T first) {

        HadoopDeepJobConfig<T> hadoopDeepJobConfig = (HadoopDeepJobConfig<T>) deepJobConfig;
        int id = deepJobConfig.getInteger(Constants.SPARK_RDD_ID);
        int partitionIndex = deepJobConfig.getInteger(Constants.SPARK_PARTITION_ID);

        TaskAttemptID attemptId = DeepSparkHadoopMapReduceUtil.newTaskAttemptID(jobTrackerId, id, true, partitionIndex,
                0);

        hadoopAttemptContext = DeepSparkHadoopMapReduceUtil.newTaskAttemptContext(
                hadoopDeepJobConfig.getHadoopConfiguration(), attemptId);
        try {
            writer = outputFormat.getRecordWriter(hadoopAttemptContext);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract Tuple2<KOut, VOut> transformElement(T record);
}
