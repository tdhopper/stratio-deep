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

import java.util.Map;

import com.stratio.deep.commons.entity.Cell;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 * 
 * @param <T>
 *            the generic type associated to this configuration object.
 */
public class DeepJobConfig<T> extends ExtractorConfig<T> {

    /**
     * @param t
     */
    public DeepJobConfig(Class<T> t) {
        super(t);
    }

    private static final long serialVersionUID = 442160874628247418L;

    protected Map<String, Cell> columnDefinitions;

    protected int pageSize;

    protected String[] inputColumns;

    protected String catalogName;

    protected String tableName;

    public Map<String, Cell> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(Map<String, Cell> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setInputColumns(String[] inputColumns) {
        this.inputColumns = inputColumns;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DeepJobConfig host(String host) {

        this.setHost(host);
        return this;
    }

    public DeepJobConfig port(Integer port) {

        this.setPort(port);
        return this;
    }

    public DeepJobConfig username(String username) {

        this.setUsername(username);
        return this;
    }

    public DeepJobConfig password(String password) {

        this.setPassword(password);
        return this;
    }

    /**
     * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is the column name,
     * and the value is the {@link com.stratio.deep.commons.entity.Cell} containing column's metadata.
     * 
     * @return the map of column names and the corresponding Cell object containing its metadata.
     */
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
    public String[] getInputColumns() {
        return this.inputColumns;
    }

    /**
     * Returns the table name.
     * 
     * @return table name
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Returns the catalog name.
     * 
     * @return catalog name
     */
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
    public DeepJobConfig<T> catalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    /**
     * Returns the maximum number of rows that will be retrieved when fetching data pages from Cassandra.
     * 
     * @return the page size
     */
    public int getPageSize() {
        return this.pageSize;
    }

    public <W extends DeepJobConfig<T>> W initialize() {

        return (W) this;
    }
}
