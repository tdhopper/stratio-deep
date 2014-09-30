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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.commons.utils.Pair;

/**
 * Created by rcrespo on 19/08/14.
 */
public class ExtractorConfig<T> implements Serializable {

    private static final long serialVersionUID = -741177816966076337L;

    private String host;

    private Integer port;

    private String username;

    private String password;

    private Map<String, Serializable> values = new HashMap<>();

    private Class<? extends IExtractor> extractorImplClass;

    private String extractorImplClassName;

    protected Class<T> entityClass;

    public ExtractorConfig(Class<T> t) {
        super();
        entityClass = t;
    }

    public Map<String, Serializable> getValues() {
        return values;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setValues(Map<String, Serializable> values) {
        this.values = values;
    }

    public Class<? extends IExtractor> getExtractorImplClass() {
        return extractorImplClass;
    }

    public void setExtractorImplClass(Class<? extends IExtractor> extractorImplClass) {
        this.extractorImplClass = extractorImplClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getExtractorImplClassName() {
        return extractorImplClassName;
    }

    public void setExtractorImplClassName(String extractorImplClassName) {
        this.extractorImplClassName = extractorImplClassName;
    }

    public String getString(String key) {
        return getValue(String.class, key);
    }

    public Integer getInteger(String key) {
        return getValue(Integer.class, key);
    }

    public Boolean getBoolean(String key) {
        return getValue(Boolean.class, key);
    }

    public String[] getStringArray(String key) {
        try {
            return getValue(String[].class, key);
        } catch (ClassCastException e) {
            return new String[] { getString(key) };
        }

    }

    public Double getDouble(String key) {
        return getValue(Double.class, key);
    }

    public Float getFloat(String key) {
        return getValue(Float.class, key);
    }

    public Long getLong(String key) {
        return getValue(Long.class, key);
    }

    public Short getShort(String key) {
        return getValue(Short.class, key);
    }

    public Byte[] getByteArray(String key) {
        return getValue(Byte[].class, key);
    }

    public <K, V> Pair<K, V> getPair(String key, Class<K> keyClass, Class<V> valueClass) {
        return getValue(Pair.class, key);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ExtractorConfig{");
        sb.append("values=").append(values);
        sb.append(", extractorImplClass=").append(extractorImplClass);
        sb.append(", extractorImplClassName='").append(extractorImplClassName).append('\'');
        sb.append(", entityClass=").append(entityClass);
        sb.append('}');
        return sb.toString();
    }

    public ExtractorConfig<T> putValue(String key, Serializable value) {
        values.put(key, value);
        return this;
    }

    /**
     * Returns the cell value casted to the specified class.
     * 
     * @param clazz
     *            the expected class
     * @param <T>
     *            the return type
     * @return the cell value casted to the specified class
     */
    public <S> S getValue(Class<S> clazz, String key) {
        if (values.get(key) == null) {
            return null;
        } else {
            return (S) values.get(key);
        }
    }
}
