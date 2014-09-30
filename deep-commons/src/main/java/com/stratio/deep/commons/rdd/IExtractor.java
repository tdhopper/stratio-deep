package com.stratio.deep.commons.rdd;

import java.io.Serializable;

import org.apache.spark.Partition;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.config.ExtractorConfig;

/**
 * Created by rcrespo on 4/08/14.
 */
public interface IExtractor<T> extends Serializable {

    Partition[] getPartitions(DeepJobConfig<T> config);

    boolean hasNext();

    T next();

    void close();

    void initIterator(Partition dp, DeepJobConfig<T> config);

    IExtractor<T> getExtractorInstance(DeepJobConfig<T> config);

    void saveRDD(T t);

    void initSave(DeepJobConfig<T> config, T first);
}
