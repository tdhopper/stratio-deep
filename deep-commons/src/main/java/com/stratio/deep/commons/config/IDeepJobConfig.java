/**
 * 
 */
package com.stratio.deep.commons.config;

import java.io.Serializable;
import java.util.Map;

import com.stratio.deep.commons.entity.Cell;
import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.commons.utils.Pair;

/**
 *
 */
public interface IDeepJobConfig<T> {

    public Map<String, Cell> getColumnDefinitions();

    public void setColumnDefinitions(Map<String, Cell> columnDefinitions);

    public void setPageSize(int pageSize);

    public void setInputColumns(String[] inputColumns);

    public void setCatalogName(String catalogName);

    public void setTableName(String tableName);

    public IDeepJobConfig<T> host(String host);

    public IDeepJobConfig<T> port(Integer port);

    public IDeepJobConfig<T> username(String username);

    public IDeepJobConfig<T> password(String password);

    /**
     * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is the column name,
     * and the value is the {@link com.stratio.deep.commons.entity.Cell} containing column's metadata.
     * 
     * @return the map of column names and the corresponding Cell object containing its metadata.
     */
    public Map<String, Cell> columnDefinitions();

    /**
     * Sets the number of rows to retrieve for each page of data fetched from Cassandra.<br/>
     * Defaults to 1000 rows.
     * 
     * @param pageSize
     *            the number of rows per page
     * @return this configuration object.
     */
    public IDeepJobConfig<T> pageSize(int pageSize);

    /* Getters */

    /**
     * Returns the list of column names that will be fetched from the underlying datastore.
     * 
     * @return the array of column names that will be retrieved from the data store.
     */
    public String[] getInputColumns();

    /**
     * Returns the table name.
     * 
     * @return table name
     */
    public String getTableName();

    /**
     * Returns the catalog name.
     * 
     * @return catalog name
     */
    public String getCatalogName();

    /**
     * Defines a projection over the CF columns. <br/>
     * Key columns will always be returned, even if not specified in the columns input array.
     * 
     * @param columns
     *            list of columns we want to retrieve from the datastore.
     * @return this object.
     */
    public IDeepJobConfig<T> inputColumns(String... columns);

    /**
     * Defines the table name.
     * 
     * @param tableName
     *            Name of the table
     * @return this object
     */
    public IDeepJobConfig<T> tableName(String tableName);

    /**
     * Defines the catalog name.
     * 
     * @param catalogName
     *            Name of the catalog
     * @return this object
     */
    public IDeepJobConfig<T> catalogName(String catalogName);

    /**
     * Returns the maximum number of rows that will be retrieved when fetching data pages from Cassandra.
     * 
     * @return the page size
     */
    public int getPageSize();

    public String getString(String key);

    public Integer getInteger(String key);

    public Boolean getBoolean(String key);

    public String[] getStringArray(String key);

    public Double getDouble(String key);

    public Float getFloat(String key);

    public Long getLong(String key);

    public Short getShort(String key);

    public Byte[] getByteArray(String key);

    public <K, V> Pair<K, V> getPair(String key, Class<K> keyClass, Class<V> valueClass);

    public DeepJobConfig<T> putValue(String key, Serializable value);

    public Class<? extends IExtractor> getExtractorImplClass();

    /**
     * Returns the cell value casted to the specified class.
     * 
     * @param clazz
     *            the expected class
     * @param <T>
     *            the return type
     * @return the cell value casted to the specified class
     */
    public <S> S getValue(Class<S> clazz, String key);

    /**
     * @return
     */

    public String getExtractorImplClassName();

    /**
     * @return
     */
    Class<T> getEntityClass();
}
