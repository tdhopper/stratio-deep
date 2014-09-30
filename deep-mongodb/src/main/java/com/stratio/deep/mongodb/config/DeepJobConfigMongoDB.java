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

package com.stratio.deep.mongodb.config;

import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.COLLECTION;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.DATABASE;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.FILTER_QUERY;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.HOST;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.IGNORE_ID_FIELD;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.INPUT_COLUMNS;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.INPUT_KEY;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.PASSWORD;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.READ_PREFERENCE;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.REPLICA_SET;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.SORT;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.SPLIT_SIZE;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.USERNAME;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.USE_CHUNKS;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.USE_SHARD;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.USE_SPLITS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.mongodb.QueryBuilder;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.util.MongoConfigUtil;
import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.entity.Cell;

/**
 * @param <T>
 */
public class DeepJobConfigMongoDB<T> extends DeepJobConfig<T> implements IMongoDeepJobConfig<T> {

    private static final long serialVersionUID = -7179376653643603038L;

    /**
     * configuration to be broadcasted to every spark node
     */
    private transient Configuration configHadoop;

    /**
     * A list of mongodb host to connect
     */
    private final List<String> hostList = new ArrayList<>();

    /**
     * MongoDB username
     */
    private String username;

    /**
     * MongoDB password
     */

    private String password;

    /**
     * Indicates the replica set's name
     */
    private String replicaSet;

    /**
     * Collection to get or insert data
     */
    private String collection;

    /**
     * Database to connect
     */
    private String database;

    /**
     * Read Preference primaryPreferred is the recommended read preference. If the primary node go down, can still read
     * from secundaries
     */
    private String readPreference;

    /**
     * Entity class to map BSONObject
     */
    protected Class<T> entityClass;

    /**
     * VIP, this MUST be transient!
     */
    private transient Map<String, Cell> columnDefinitionMap;

    private String[] inputColumns;

    /**
     * OPTIONAL filter query
     */
    private String query;

    /**
     * OPTIONAL fields to be returned
     */
    private BSONObject fields;

    /**
     * OPTIONAL sorting
     */
    private String sort;

    private final Class<? extends InputFormat<?, ?>> inputFormat = MongoInputFormat.class;

    /**
     * Shard key
     */
    private String inputKey;

    private boolean createInputSplit = true;

    private boolean useShards = false;

    private boolean splitsUseChunks = true;

    private Integer splitSize = 8;

    private Map<String, Object> customConfiguration;

    /**
     * Default constructor
     */
    public DeepJobConfigMongoDB(Class<T> entityClass) {
        super();
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Cell> columnDefinitions() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    // TODO : cheking
    @Override
    public DeepJobConfigMongoDB<T> pageSize(int pageSize) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        return !hostList.isEmpty() ? hostList.get(0) : null;
    }

    @Override
    public List<String> getHostList() {
        return hostList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getInputColumns() {
        return fields.keySet().toArray(new String[fields.keySet().size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> host(String host) {
        this.hostList.add(host);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> host(List<String> host) {
        this.hostList.addAll(host);
        return this;
    }

    public IMongoDeepJobConfig<T> host(String[] hosts) {
        this.hostList.addAll(Arrays.asList(hosts));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> filterQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> filterQuery(BSONObject query) {
        this.query = query.toString();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> filterQuery(QueryBuilder query) {
        this.query = query.get().toString();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> replicaSet(String replicaSet) {
        this.replicaSet = replicaSet;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> database(String database) {
        this.database = database;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> collection(String collection) {
        this.collection = collection;
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> fields(BSONObject fields) {
        this.fields = fields;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> sort(String sort) {
        this.sort = sort;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> sort(BSONObject sort) {
        this.sort = sort.toString();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> createInputSplit(boolean createInputSplit) {
        this.createInputSplit = createInputSplit;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> useShards(boolean useShards) {
        this.useShards = useShards;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> splitsUseChunks(boolean splitsUseChunks) {
        this.splitsUseChunks = splitsUseChunks;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> inputKey(String inputKey) {
        this.inputKey = inputKey;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    // TODO: cheking
    @Override
    public int getPageSize() {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> readPreference(String readPreference) {
        this.readPreference = readPreference;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> ignoreIdField() {
        BSONObject bsonFields = fields != null ? fields : new BasicBSONObject();
        bsonFields.put("_id", 0);
        fields = bsonFields;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeepJobConfigMongoDB<T> initialize() {
        validate();
        configHadoop = new Configuration();
        StringBuilder connection = new StringBuilder();

        connection.append("mongodb").append(":").append("//");

        if (username != null && password != null) {
            connection.append(username).append(":").append(password).append("@");
        }

        boolean firstHost = true;
        for (String host : hostList) {
            if (!firstHost) {
                connection.append(",");
            }
            connection.append(host);
            firstHost = false;
        }

        connection.append("/").append(database).append(".").append(collection);

        StringBuilder options = new StringBuilder();
        boolean asignado = false;

        if (readPreference != null) {
            asignado = true;
            options.append("?readPreference=").append(readPreference);
        }

        if (replicaSet != null) {
            if (asignado) {
                options.append("&");
            } else {
                options.append("?");
            }
            options.append("replicaSet=").append(replicaSet);
        }

        connection.append(options);

        configHadoop.set(MongoConfigUtil.INPUT_URI, connection.toString());

        configHadoop.set(MongoConfigUtil.OUTPUT_URI, connection.toString());

        configHadoop.set(MongoConfigUtil.INPUT_SPLIT_SIZE, String.valueOf(splitSize));

        if (inputKey != null) {
            configHadoop.set(MongoConfigUtil.INPUT_KEY, inputKey);
        }

        configHadoop.set(MongoConfigUtil.SPLITS_USE_SHARDS, String.valueOf(useShards));

        configHadoop.set(MongoConfigUtil.CREATE_INPUT_SPLITS, String.valueOf(createInputSplit));

        configHadoop.set(MongoConfigUtil.SPLITS_USE_CHUNKS, String.valueOf(splitsUseChunks));

        if (query != null) {
            configHadoop.set(MongoConfigUtil.INPUT_QUERY, query);
        }

        if (fields != null) {
            configHadoop.set(MongoConfigUtil.INPUT_FIELDS, fields.toString());
        }

        if (sort != null) {
            configHadoop.set(MongoConfigUtil.INPUT_SORT, sort);
        }

        if (username != null && password != null) {
            configHadoop.set(MongoConfigUtil.AUTH_URI, connection.toString());
        }

        if (customConfiguration != null) {
            Set<Map.Entry<String, Object>> set = customConfiguration.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                configHadoop.set(entry.getKey(), entry.getValue().toString());
            }
        }

        return this;
    }

    /**
     * validates connection parameters
     */
    private void validate() {
        if (hostList.isEmpty()) {
            throw new IllegalArgumentException("host cannot be null");
        }
        if (database == null) {
            throw new IllegalArgumentException("database cannot be null");
        }
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMongoDeepJobConfig<T> inputColumns(String... columns) {
        BSONObject bsonFields = fields != null ? fields : new BasicBSONObject();
        boolean isIdPresent = false;
        for (String column : columns) {
            if (column.trim().equalsIgnoreCase("_id")) {
                isIdPresent = true;
            }

            bsonFields.put(column.trim(), 1);
        }
        if (!isIdPresent) {
            bsonFields.put("_id", 0);
        }
        fields = bsonFields;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getHadoopConfiguration() {
        if (configHadoop == null) {
            initialize();
        }
        return configHadoop;
    }

    @Override
    public IMongoDeepJobConfig<T> initialize(DeepJobConfig extractorConfig) {
        Map<String, Serializable> values = extractorConfig.getValues();

        if (values.get(USERNAME) != null) {
            username(extractorConfig.getString(USERNAME));
        }

        if (values.get(PASSWORD) != null) {
            password(extractorConfig.getString(PASSWORD));
        }

        if (values.get(HOST) != null) {
            host((extractorConfig.getStringArray(HOST)));
        }

        if (values.get(COLLECTION) != null) {
            collection(extractorConfig.getString(COLLECTION));
        }

        if (values.get(INPUT_COLUMNS) != null) {
            inputColumns(extractorConfig.getStringArray(INPUT_COLUMNS));
        }

        if (values.get(DATABASE) != null) {
            database(extractorConfig.getString(DATABASE));
        }

        if (values.get(REPLICA_SET) != null) {
            replicaSet(extractorConfig.getString(REPLICA_SET));
        }

        if (values.get(READ_PREFERENCE) != null) {
            readPreference(extractorConfig.getString(READ_PREFERENCE));
        }

        if (values.get(SORT) != null) {
            sort(extractorConfig.getString(SORT));
        }

        if (values.get(FILTER_QUERY) != null) {
            filterQuery(extractorConfig.getString(FILTER_QUERY));
        }

        if (values.get(INPUT_KEY) != null) {
            inputKey(extractorConfig.getString(INPUT_KEY));
        }

        if (values.get(IGNORE_ID_FIELD) != null && extractorConfig.getBoolean(IGNORE_ID_FIELD) == true) {
            ignoreIdField();
        }

        if (values.get(INPUT_KEY) != null) {
            inputKey(extractorConfig.getString(INPUT_KEY));
        }

        if (values.get(USE_SHARD) != null) {
            useShards(extractorConfig.getBoolean(USE_SHARD));
        }

        if (values.get(USE_SPLITS) != null) {
            createInputSplit(extractorConfig.getBoolean(USE_SPLITS));
        }

        if (values.get(USE_CHUNKS) != null) {
            splitsUseChunks(extractorConfig.getBoolean(USE_CHUNKS));
        }
        if (values.get(SPLIT_SIZE) != null) {
            splitSize = extractorConfig.getInteger(SPLIT_SIZE);
        }

        this.initialize();

        return this;
    }

}
