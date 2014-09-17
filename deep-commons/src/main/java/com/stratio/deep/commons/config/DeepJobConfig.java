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

import com.stratio.deep.commons.entity.Cell;
import org.apache.hadoop.conf.Configuration;

import java.io.Serializable;
import java.util.Map;

/**
 * Defines the public methods that each Stratio Deep configuration object should implement.
 * 
 * @param <T> the generic type associated to this configuration object.
 */
public class DeepJobConfig<T> implements Serializable {

  private static final long serialVersionUID = 601688014965437244L;

  private String password;
  
  private Map<String, Cell> columnDefinitions;
  
  private int pageSize;
  
  private Class<T> entityClass;
  
  private String host;
  
  private String[] inputColumns;
  
  private String userName;
  
  private ExtractorConfig<T> extractorConfiguration;
  
  private Configuration hadoopConfiguration;

  /**
   * Returns the password needed to authenticate to the remote datastore cluster.
   * 
   * @return the password used to login to the remote cluster.
   */
  String getPassword() {
    return this.password;
  }

  /**
   * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is
   * the column name, and the value is the {@link com.stratio.deep.commons.entity.Cell} containing
   * column's metadata.
   * 
   * @return the map of column names and the corresponding Cell object containing its metadata.
   */
  Map<String, Cell> columnDefinitions() {
    return this.columnDefinitions;
  }

  /**
   * Sets the number of rows to retrieve for each page of data fetched from Cassandra.<br/>
   * Defaults to 1000 rows.
   * 
   * @param pageSize the number of rows per page
   * @return this configuration object.
   */
  DeepJobConfig<T> pageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }


  /* Getters */

  /**
   * Returns the underlying testentity class used to map the Cassandra Column family.
   * 
   * @return the entity class object associated to this configuration object.
   */
  Class<T> getEntityClass() {
    return this.entityClass;
  }

  /**
   * Returns the hostname of the cassandra server.
   * 
   * @return the endpoint of the cassandra server.
   */
  String getHost() {
    return this.host;
  }

  /**
   * Returns the list of column names that will be fetched from the underlying datastore.
   * 
   * @return the array of column names that will be retrieved from the data store.
   */
  String[] getInputColumns() {
    return this.inputColumns;
  }

  /**
   * Returns the username used to authenticate to the cassandra server. Defaults to the empty
   * string.
   * 
   * @return the username to use to login to the remote server.
   */
  String getUsername() {
    return this.userName;
  }

  /**
   * Sets the datastore hostname
   * 
   * @param hostname the cassandra server endpoint.
   * @return this object.
   */
  DeepJobConfig<T> host(String hostname) {
    this.host = hostname;
    return this;
  }

  /**
   * Initialized the current configuration object.
   * 
   * @return this object.
   */
  DeepJobConfig<T> initialize() {
    return this;
  }

  DeepJobConfig<T> initialize(ExtractorConfig<T> extractorConfig) {
    
    this.extractorConfiguration = extractorConfig;
    return this;
  }

  /**
   * Defines a projection over the CF columns. <br/>
   * Key columns will always be returned, even if not specified in the columns input array.
   * 
   * @param columns list of columns we want to retrieve from the datastore.
   * @return this object.
   */
  DeepJobConfig<T> inputColumns(String... columns) {
    
    this.inputColumns = columns;
    return this;
  }

  /**
   * Sets the password to use to login to Cassandra. Leave empty if you do not need authentication.
   * 
   * @return this object.
   */
  DeepJobConfig<T> password(String password) {
   
    this.password = password;
    return this;
  }  

  /**
   * /** Sets the username to use to login to Cassandra. Leave empty if you do not need
   * authentication.
   * 
   * @return this object.
   */
  DeepJobConfig<T> username(String username) {
    
    this.userName = username;
    return this;
  }


  /**
   * Returns the maximum number of rows that will be retrieved when fetching data pages from
   * Cassandra.
   * 
   * @return the page size
   */
  int getPageSize() {
    return this.pageSize;
  }

  /**
   * Returns the extractor specific configuration
   *  
   * @return the extractor configuration
   */
  ExtractorConfig<T> getExtractorConfiguration() {
    return this.extractorConfiguration;
  }
  
  // /**
  // * Just in case you have a hadoopInputFormat
  // * @return
  // */
  Configuration getHadoopConfiguration() {
    
    return this.hadoopConfiguration;
  }


}
