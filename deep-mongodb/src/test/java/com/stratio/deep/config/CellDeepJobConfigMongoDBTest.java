package com.stratio.deep.config;

import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.mongodb.config.CellDeepJobConfigMongoDB;
import com.stratio.deep.mongodb.config.DeepJobConfigMongoDB;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by rcrespo on 16/07/14.
 */
@Test
public class CellDeepJobConfigMongoDBTest {

    @Test
    public void createTest() {

        DeepJobConfigMongoDB<Cells> cellDeepJobConfigMongoDB = new CellDeepJobConfigMongoDB();

        assertNotNull(cellDeepJobConfigMongoDB);

        assertEquals(cellDeepJobConfigMongoDB.getEntityClass(), Cells.class);

    }

}
