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

package com.stratio.deep.core.context;

import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.core.rdd.DeepJavaRDD;
import com.stratio.deep.core.rdd.DeepRDD;

/**
 * Tests DeepSparkContext instantiations.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = { SparkContext.class, DeepJavaRDD.class, DeepSparkContext.class, DeepRDD.class, Method.class,
        AccessibleObject.class, System.class })
public class DeepSparkContextTest {

    @Test
    public void createRDDTest() throws Exception {
        DeepSparkContext deepSparkContext = createDeepSparkContext();
        DeepJobConfig deepJobConfig = createDeepJobConfig();
        DeepRDD deepRDD = createDeepRDD(deepSparkContext.sc(), deepJobConfig);

        DeepRDD rddReturn = (DeepRDD) deepSparkContext.createRDD(deepJobConfig);

        assertSame("The DeepRDD is the same", deepRDD, rddReturn);

    }

    @Test
    public void JavaRDDTest() throws Exception {
        DeepSparkContext deepSparkContext = createDeepSparkContext();
        DeepJobConfig deepJobConfig = createDeepJobConfig();
        DeepRDD deepRDD = createDeepRDD(deepSparkContext.sc(), deepJobConfig);
        DeepJavaRDD javaRdd = createDeepJAvaRDD(deepRDD);

        JavaRDD javaRDdReturn = deepSparkContext.createJavaRDD(deepJobConfig);

        assertSame("The DeepJavaRDD is Same", javaRdd, javaRDdReturn);
    }

    @Test
    public void saveRDDTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        /*
         * DeepSparkContext deepSparkContext = createDeepSparkContext();
         * 
         * IDeepJobConfig<Cell, IDeepJobConfig<?, ?>> ideepJobConfig = mock(IDeepJobConfig.class); RDD<Cell> rdd =
         * mock(RDD.class); Method method = new Method(){ public Object invoke(Object obj, Object... args){
         * 
         * return null; } };
         * 
         * Collection aa = PowerMockito.mock(Collection.class); when(ideepJobConfig.getSaveMethod()).thenReturn(method);
         * 
         * deepSparkContext.saveRDD(rdd,ideepJobConfig);
         * 
         * Mockito.verify(method).invoke(null, rdd, ideepJobConfig);
         */
    }

    private DeepJavaRDD createDeepJAvaRDD(DeepRDD deepRDD) throws Exception {
        DeepJavaRDD javaRdd = mock(DeepJavaRDD.class);
        whenNew(DeepJavaRDD.class).withArguments(deepRDD).thenReturn(javaRdd);
        return javaRdd;
    }

    private DeepSparkContext createDeepSparkContext() {
        PowerMockito.suppress(PowerMockito.constructor(DeepSparkContext.class, SparkContext.class));
        return Whitebox.newInstance(DeepSparkContext.class);
    }

    private DeepJobConfig createDeepJobConfig() {
        DeepJobConfig extractorConfig = mock(DeepJobConfig.class);
        when(extractorConfig.getExtractorImplClass()).thenReturn(new Object().getClass());
        // when(extractorConfig.getInputFormatClass()).thenReturn(null);
        return extractorConfig;
    }

    private DeepRDD createDeepRDD(SparkContext sc, DeepJobConfig deepJobConfig) throws Exception {

        DeepRDD deepRDD = mock(DeepRDD.class);

        whenNew(DeepRDD.class).withArguments(sc, deepJobConfig).thenReturn(deepRDD);
        return deepRDD;
    }
}
