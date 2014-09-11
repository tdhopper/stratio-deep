package com.stratio.deep.commons.extractor.server;

import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.extractor.actions.*;
import com.stratio.deep.commons.rdd.IExtractor;
import org.apache.spark.Partition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by darroyo on 8/09/14.
 */
public class ExtractorServerFake<T> implements IExtractor<T>{


    private IExtractor<T> extractor;

    @Override
    public Partition[] getPartitions(ExtractorConfig<T> config) {
        if (extractor == null) {
            this.initExtractor(config);
        }

        return extractor.getPartitions(config);
    }

    @Override
    public boolean hasNext() {
        return extractor.hasNext();
    }

    @Override
    public T next() {
        return extractor.next();
    }

    @Override
    public void close() {
        extractor.close();
    }

    @Override
    public void initIterator(Partition dp, ExtractorConfig<T> config) {
        if (extractor == null) {
            this.initExtractor(config);
        }
        extractor.initIterator(dp, config);
        return;
    }

    @Override
    public IExtractor<T> getExtractorInstance(ExtractorConfig<T> config) {
        return null;
    }

    @Override
    public void saveRDD(T t) {

    }

    @Override
    public void initSave(ExtractorConfig<T> config, T first) {
        if (extractor == null) {
            this.initExtractor(config);
        }

        extractor.initSave(config, first);
        return;
    }




    /**
     * @param config
     */
    @SuppressWarnings("unchecked")
    private void initExtractor(ExtractorConfig<T> config) {

        Class<T> rdd = (Class<T>) config.getExtractorImplClass();
        try {
            Constructor<T> c = null;
            if (config.getEntityClass().isAssignableFrom(Cells.class)){
                c = rdd.getConstructor();
                this.extractor = (IExtractor<T>) c.newInstance();
            }else{
                c = rdd.getConstructor(Class.class);
                this.extractor = (IExtractor<T>) c.newInstance(config.getEntityClass());
            }


        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    protected void save(SaveAction<T> saveAction) {
        extractor.saveRDD(saveAction.getRecord());
        return;

    }
}
