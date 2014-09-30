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

package com.stratio.deep.commons.config;

import org.apache.hadoop.conf.Configuration;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 * 
 * @param <T>
 *            the generic type associated to this configuration object.
 */
public class HadoopDeepJobConfig<T> extends DeepJobConfig<T> {

    private static final long serialVersionUID = -4341314399359604922L;

    /**
     * @param t
     */
    public HadoopDeepJobConfig(Class<T> t) {
        super(t);
    }

    private Configuration hadoopConfiguration;

    public Configuration getHadoopConfiguration() {
        return hadoopConfiguration;
    }

    public void setHadoopConfiguration(Configuration hadoopConfiguration) {
        this.hadoopConfiguration = hadoopConfiguration;
    }
}
