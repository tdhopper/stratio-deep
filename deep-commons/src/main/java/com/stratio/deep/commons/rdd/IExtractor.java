package com.stratio.deep.commons.rdd;

import java.io.Serializable;

import org.apache.spark.Partition;

import com.stratio.deep.commons.config.DeepJobConfig;

/**
 * Created by rcrespo on 4/08/14.
 */
public interface IExtractor<T> extends Serializable {

    <W extends DeepJobConfig<T>> Partition[] getPartitions(W config);

    boolean hasNext();

    T next();

    void close();

    <W extends DeepJobConfig<T>> void initIterator(Partition dp, W config);

    <W extends DeepJobConfig<T>> IExtractor<T> getExtractorInstance(W config);

    void saveRDD(T t);

    <W extends DeepJobConfig<T>> void initSave(W config, T first);
}
