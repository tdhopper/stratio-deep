/*
 * Copyright 2014, Stratio.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.stratio.deep.core.function;

import static com.stratio.deep.commons.utils.Constants.SPARK_PARTITION_ID;
import static com.stratio.deep.commons.utils.Utils.getExtractorInstance;
import static com.stratio.deep.core.util.ExtractorClientUtil.getExtractorClient;

import java.io.Serializable;

import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.exception.DeepExtractorinitializationException;
import com.stratio.deep.commons.rdd.IExtractor;

/**
 * Created by rcrespo on 28/08/14.
 */
public class PrepareSaveFunction<T> extends AbstractFunction1<Iterator<T>, BoxedUnit> implements
        Serializable {

    private final DeepJobConfig<T> deepJobConfig;

    private final T first;

    public PrepareSaveFunction(DeepJobConfig<T> deepJobConfig, T first) {
        this.first = first;
        this.deepJobConfig = deepJobConfig;
    }

    @Override
    public BoxedUnit apply(Iterator<T> v1) {
        IExtractor<T> extractor;
        try {
            extractor = getExtractorInstance(deepJobConfig);
        } catch (DeepExtractorinitializationException e) {
            extractor = getExtractorClient();
        }

        extractor.initSave(deepJobConfig, first);
        while (v1.hasNext()) {
            extractor.saveRDD(v1.next());
        }
        deepJobConfig.putValue(SPARK_PARTITION_ID,
                String.valueOf(Integer.parseInt(deepJobConfig.getValues().get(SPARK_PARTITION_ID).toString()) + 1));
        extractor.close();
        return null;
    }
}
