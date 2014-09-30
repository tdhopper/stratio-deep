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

import com.stratio.deep.commons.entity.Cell;
import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.commons.utils.Pair;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 * 
 * @param <T>
 *            the generic type associated to this configuration object.
 */
public class DeepJobConfig<T> implements IDeepJobConfig<T>, Serializable {

    private static final long serialVersionUID = 442160874628247418L;

    /**
     * @param t
     */
    public DeepJobConfig(ExtractorConfig<T> extractorConfig) {
        this.extractorConfig = extractorConfig;
    }

    protected Map<String, Cell> columnDefinitions;

    protected int pageSize;

    protected String[] inputColumns;

    protected String catalogName;

    protected String tableName;

    protected Map<String, Serializable> values = new HashMap<>();

    protected ExtractorConfig<T> extractorConfig;

    @Override
    public Map<String, Cell> getColumnDefinitions() {
        return columnDefinitions;
    }

    @Override
    public void setColumnDefinitions(Map<String, Cell> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public void setInputColumns(String[] inputColumns) {
        this.inputColumns = inputColumns;
    }

    @Override
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public DeepJobConfig<T> host(String host) {

        extractorConfig.setHost(host);
        return this;
    }

    @Override
    public DeepJobConfig<T> port(Integer port) {

        extractorConfig.setPort(port);
        return this;
    }

    @Override
    public DeepJobConfig<T> username(String username) {

        extractorConfig.setUsername(username);
        return this;
    }

    @Override
    public DeepJobConfig<T> password(String password) {

        extractorConfig.setPassword(password);
        return this;
    }

    /**
     * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is the column name,
     * and the value is the {@link com.stratio.deep.commons.entity.Cell} containing column's metadata.
     * 
     * @return the map of column names and the corresponding Cell object containing its metadata.
     */
    @Override
    public Map<String, Cell> columnDefinitions() {
        return this.columnDefinitions;
    }

    /**
     * Sets the number of rows to retrieve for each page of data fetched from Cassandra.<br/>
     * Defaults to 1000 rows.
     * 
     * @param pageSize
     *            the number of rows per page
     * @return this configuration object.
     */
    @Override
    public DeepJobConfig<T> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /* Getters */

    /**
     * Returns the list of column names that will be fetched from the underlying datastore.
     * 
     * @return the array of column names that will be retrieved from the data store.
     */
    @Override
    public String[] getInputColumns() {
        return this.inputColumns;
    }

    /**
     * Returns the table name.
     * 
     * @return table name
     */
    @Override
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Returns the catalog name.
     * 
     * @return catalog name
     */
    @Override
    public String getCatalogName() {
        return this.tableName;
    }

    /**
     * Defines a projection over the CF columns. <br/>
     * Key columns will always be returned, even if not specified in the columns input array.
     * 
     * @param columns
     *            list of columns we want to retrieve from the datastore.
     * @return this object.
     */
    @Override
    public DeepJobConfig<T> inputColumns(String... columns) {

        this.inputColumns = columns;
        return this;
    }

    /**
     * Defines the table name.
     * 
     * @param tableName
     *            Name of the table
     * @return this object
     */
    @Override
    public DeepJobConfig<T> tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * Defines the catalog name.
     * 
     * @param catalogName
     *            Name of the catalog
     * @return this object
     */
    @Override
    public DeepJobConfig<T> catalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    /**
     * Returns the maximum number of rows that will be retrieved when fetching data pages from Cassandra.
     * 
     * @return the page size
     */
    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    public <W extends DeepJobConfig<T>> W initialize() {

        return (W) this;
    }

    @Override
    public String getString(String key) {
        return getValue(String.class, key);
    }

    @Override
    public Integer getInteger(String key) {
        return getValue(Integer.class, key);
    }

    @Override
    public Boolean getBoolean(String key) {
        return getValue(Boolean.class, key);
    }

    @Override
    public String[] getStringArray(String key) {
        try {
            return getValue(String[].class, key);
        } catch (ClassCastException e) {
            return new String[] { getString(key) };
        }
    }

    @Override
    public Double getDouble(String key) {
        return getValue(Double.class, key);
    }

    @Override
    public Float getFloat(String key) {
        return getValue(Float.class, key);
    }

    @Override
    public Long getLong(String key) {
        return getValue(Long.class, key);
    }

    @Override
    public Short getShort(String key) {
        return getValue(Short.class, key);
    }

    @Override
    public Byte[] getByteArray(String key) {
        return getValue(Byte[].class, key);
    }

    @Override
    public <K, V> Pair<K, V> getPair(String key, Class<K> keyClass, Class<V> valueClass) {
        return getValue(Pair.class, key);
    }

    @Override
    public DeepJobConfig<T> putValue(String key, Serializable value) {
        values.put(key, value);
        return this;
    }

    @Override
    public Class<? extends IExtractor> getExtractorImplClass() {
        return extractorConfig.getExtractorImplClass();
    }

    @Override
    public String getExtractorImplClassName() {
        return extractorConfig.getExtractorImplClassName();
    }

    @Override
    public Class<T> getEntityClass() {
        return extractorConfig.getEntityClass();
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
    @Override
    public <S> S getValue(Class<S> clazz, String key) {
        if (values.get(key) == null) {
            return null;
        } else {
            return (S) values.get(key);
        }
    }
}
