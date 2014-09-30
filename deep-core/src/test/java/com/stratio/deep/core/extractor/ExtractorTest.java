/*
 * Copyright 2014, Stratio.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.stratio.deep.core.extractor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.Serializable;

import org.apache.spark.rdd.RDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.extractor.utils.ExtractorConstants;
import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.core.entity.BookEntity;
import com.stratio.deep.core.entity.MessageTestEntity;

/**
 * Created by rcrespo on 9/09/14.
 */

/**
 * This is the common test that validate each extractor.
 * 
 * @param <T>
 */
public abstract class ExtractorTest<T> implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractorTest.class);

    Class inputEntity;

    Class outputEntity;

    Class configEntity;

    // protected DeepSparkContext context;

    private final String host;

    private final Integer port;

    protected String database = "test";

    protected String databaseInputColumns = "book";

    protected final String tableRead = "input";

    protected final String filter = "filter";

    private static final long READ_COUNT_EXPECTED = 1l;

    private static final String READ_FIELD_EXPECTED = "new message test";

    protected Class<IExtractor<T>> extractor;

    /**
     * Constructor
     * 
     * @param extractor
     * @param host
     * @param port
     * @param inputEntity
     * @param outputEntity
     */
    public ExtractorTest(Class<IExtractor<T>> extractor, String host, Integer port, Class inputEntity,
            Class outputEntity, Class configEntity) {
        super();
        this.inputEntity = inputEntity;
        this.outputEntity = outputEntity;
        this.configEntity = configEntity;
        this.host = host;
        this.port = port;
        this.extractor = extractor;
    }

    /**
     * Constructor
     * 
     * @param extractor
     * @param host
     * @param port
     * @param database
     * @param inputEntity
     * @param outputEntity
     */
    public ExtractorTest(Class<IExtractor<T>> extractor, String host, Integer port, String database, Class inputEntity,
            Class outputEntity, Class configEntity) {
        super();
        this.inputEntity = inputEntity;
        this.outputEntity = outputEntity;
        this.configEntity = configEntity;
        this.host = host;
        this.port = port;
        this.extractor = extractor;
        this.database = database;
    }

    /**
     * 
     * @param extractor
     * @param host
     * @param port
     */
    public ExtractorTest(Class<IExtractor<T>> extractor, String host, Integer port, boolean isCells) {
        super();
        if (isCells) {
            this.inputEntity = Cells.class;
            this.outputEntity = Cells.class;
            this.configEntity = Cells.class;
        } else {
            this.inputEntity = MessageTestEntity.class;
            this.outputEntity = MessageTestEntity.class;
            this.configEntity = BookEntity.class;
        }

        this.host = host;
        this.port = port;
        this.extractor = extractor;
    }

    /**
     * 
     * @param extractor
     * @param host
     * @param port
     * @param database
     */
    public ExtractorTest(Class<IExtractor<T>> extractor, String host, Integer port, String database, boolean isCells) {
        super();
        if (isCells) {
            this.inputEntity = Cells.class;
            this.outputEntity = Cells.class;
            this.configEntity = Cells.class;
        } else {
            this.inputEntity = MessageTestEntity.class;
            this.outputEntity = MessageTestEntity.class;
            this.configEntity = BookEntity.class;
        }

        this.host = host;
        this.port = port;
        this.extractor = extractor;
        this.database = database;
    }

    /**
     * It initializes spark's context
     */
    @BeforeClass
    public void init() {
        // DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");
    }

    /**
     * It tests if the extractor can read from the data store
     */
    @Test
    public void testRead() {

        DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");

        DeepJobConfig<T> inputConfigEntity = getReadExtractorConfig();

        RDD<T> inputRDDEntity = context.createRDD(inputConfigEntity);

        Assert.assertEquals(READ_COUNT_EXPECTED, inputRDDEntity.count());

        if (inputConfigEntity.getEntityClass().isAssignableFrom(Cells.class)) {
            Assert.assertEquals(READ_FIELD_EXPECTED, ((Cells) inputRDDEntity.first()).getCellByName("message")
                    .getCellValue());
        } else {

            Assert.assertEquals(READ_FIELD_EXPECTED, ((MessageTestEntity) inputRDDEntity.first()).getMessage());
        }
        context.stop();

    }

    /**
     * It tests if the extractor can write to the data store
     */
    @Test
    public void testWrite() {

        DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");

        DeepJobConfig<T> inputConfigEntity = getReadExtractorConfig();

        RDD<T> inputRDDEntity = context.createRDD(inputConfigEntity);

        DeepJobConfig<T> outputConfigEntity;
        if (inputConfigEntity.getEntityClass().isAssignableFrom(Cells.class)) {
            outputConfigEntity = getWriteExtractorConfig("outputCells");
        } else {
            outputConfigEntity = getWriteExtractorConfig("outputEntity");
        }

        // Save RDD in DataSource
        context.saveRDD(inputRDDEntity, outputConfigEntity);

        RDD<T> outputRDDEntity = context.createRDD(outputConfigEntity);

        if (inputConfigEntity.getEntityClass().isAssignableFrom(Cells.class)) {
            Assert.assertEquals(READ_FIELD_EXPECTED, ((Cells) outputRDDEntity.first()).getCellByName("message")
                    .getCellValue());
        } else {

            Assert.assertEquals(READ_FIELD_EXPECTED, ((MessageTestEntity) outputRDDEntity.first()).getMessage());
        }
        context.stop();

    }

    @Test
    public void testInputColumns() {

        DeepSparkContext context = new DeepSparkContext("local", "deepSparkContextTest");

        DeepJobConfig<T> inputConfigEntity = getInputColumnConfig("_id, metadata");

        RDD<T> inputRDDEntity = context.createRDD(inputConfigEntity);

        if (isEntityClassCells(inputConfigEntity)) {
            Cells bookCells = (Cells) inputRDDEntity.first();

            assertNotNull(bookCells.getCellByName("_id").getCellValue());
            assertNotNull(bookCells.getCellByName("metadata").getCellValue());
            assertNull(bookCells.getCellByName("cantos"));
        } else {
            BookEntity bookEntity = (BookEntity) inputRDDEntity.first();

            assertNotNull(bookEntity.getId());
            assertNotNull(bookEntity.getMetadataEntity());
            assertNull(bookEntity.getCantoEntities());
        }

        DeepJobConfig<T> inputConfigEntity2 = getInputColumnConfig("cantos");

        RDD<T> inputRDDEntity2 = context.createRDD(inputConfigEntity2);

        if (isEntityClassCells(inputConfigEntity2)) {
            Cells bookCells = (Cells) inputRDDEntity2.first();

            assertNull(bookCells.getCellByName("_id"));
            assertNull(bookCells.getCellByName("metadata"));
            assertNotNull(bookCells.getCellByName("cantos").getCellValue());
        } else {
            BookEntity bookEntity2 = (BookEntity) inputRDDEntity2.first();

            assertNull(bookEntity2.getId());
            assertNull(bookEntity2.getMetadataEntity());
            assertNotNull(bookEntity2.getCantoEntities());
        }

        DeepJobConfig<T> inputConfigEntity3 = getInputColumnConfig("cantos, metadata");

        RDD<T> inputRDDEntity3 = context.createRDD(inputConfigEntity3);

        if (isEntityClassCells(inputConfigEntity3)) {
            Cells bookCells = (Cells) inputRDDEntity3.first();

            assertNull(bookCells.getCellByName("_id"));
            assertNotNull(bookCells.getCellByName("metadata").getCellValue());
            assertNotNull(bookCells.getCellByName("cantos").getCellValue());
        } else {
            BookEntity bookEntity = (BookEntity) inputRDDEntity3.first();

            assertNull(bookEntity.getId());
            assertNotNull(bookEntity.getMetadataEntity());
            assertNotNull(bookEntity.getCantoEntities());
        }

        context.stop();

    }

    private DeepJobConfig<T> getExtractorConfig(Class<T> clazz) {
        return new DeepJobConfig<>(clazz);
    }

    @Test
    protected void testFilter() {
        assertEquals(true, true);
    }

    public DeepJobConfig<T> getWriteExtractorConfig(String output) {
        DeepJobConfig<T> deepJobConfig = getExtractorConfig(outputEntity);
        deepJobConfig.putValue(ExtractorConstants.HOST, host)
                .putValue(ExtractorConstants.DATABASE, database)
                .putValue(ExtractorConstants.PORT, String.valueOf(port))
                .putValue(ExtractorConstants.COLLECTION, output)
                .putValue(ExtractorConstants.CREATE_ON_WRITE, "true");
        deepJobConfig.setExtractorImplClass(extractor);
        return deepJobConfig;
    }

    public DeepJobConfig<T> getReadExtractorConfig() {

        DeepJobConfig<T> deepJobConfig = getExtractorConfig(inputEntity);
        deepJobConfig.putValue(ExtractorConstants.HOST, host)
                .putValue(ExtractorConstants.DATABASE, database)
                .putValue(ExtractorConstants.PORT, String.valueOf(port))
                .putValue(ExtractorConstants.COLLECTION, tableRead);
        deepJobConfig.setExtractorImplClass(extractor);
        return deepJobConfig;
    }

    public DeepJobConfig<T> getInputColumnConfig(String inputColumns) {

        DeepJobConfig<T> deepJobConfig = getExtractorConfig(configEntity);
        deepJobConfig.putValue(ExtractorConstants.HOST, host)
                .putValue(ExtractorConstants.DATABASE, databaseInputColumns)
                .putValue(ExtractorConstants.PORT, String.valueOf(port))
                .putValue(ExtractorConstants.COLLECTION, tableRead)
                .putValue(ExtractorConstants.INPUT_COLUMNS, inputColumns);
        deepJobConfig.setExtractorImplClass(extractor);
        return deepJobConfig;
    }

    public DeepJobConfig<T> getFilterConfig() {

        DeepJobConfig<T> deepJobConfig = getExtractorConfig(inputEntity);
        deepJobConfig.setEntityClass(inputEntity);
        deepJobConfig.putValue(ExtractorConstants.HOST, host)
                .putValue(ExtractorConstants.DATABASE, database)
                .putValue(ExtractorConstants.COLLECTION, tableRead)
                .putValue(ExtractorConstants.PORT, String.valueOf(port))
                .putValue(ExtractorConstants.FILTER_QUERY, filter);

        deepJobConfig.setExtractorImplClass(extractor);
        return deepJobConfig;
    }

    /**
     * It closes spark's context
     */

    private boolean isEntityClassCells(DeepJobConfig<T> extractorConfig) {
        if (extractorConfig.getEntityClass().isAssignableFrom(Cells.class)) {
            return true;
        }
        return false;
    }

}
