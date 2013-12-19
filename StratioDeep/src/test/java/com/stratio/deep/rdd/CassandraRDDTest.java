package com.stratio.deep.rdd;

import static org.testng.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.CharacterCodingException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.rdd.RDD;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.collection.Seq;

import com.stratio.deep.config.DeepJobConfigFactory;
import com.stratio.deep.context.AbstractDeepSparkContextTest;
import com.stratio.deep.embedded.CassandraServer;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.util.Constants;

public class CassandraRDDTest extends AbstractDeepSparkContextTest {
	private Logger logger = Logger.getLogger(getClass());

	private RDD<TestEntity> rdd;

	@BeforeClass
	protected void initServerAndRDD() throws IOException, URISyntaxException,
			ConfigurationException, InterruptedException {
		rddConfig = DeepJobConfigFactory.create(TestEntity.class)
				.host(Constants.DEFAULT_CASSANDRA_HOST)
				.port(CassandraServer.CASSANDRA_THRIFT_PORT)
				.keyspace(KEYSPACE_NAME)
				.columnFamily(COLUMN_FAMILY);

		rddConfig.getConfiguration();

		logger.info("Constructed configuration object: " + rddConfig);
		logger.info("Constructiong cassandraRDD");

		rdd = context.cassandraRDD(rddConfig);
	}

	@Test
	public void testRDDInstantiation() {
		logger.info("testRDDInstantiation()");
		assertNotNull(rdd);
	}

	@Test(dependsOnMethods = "testRDDInstantiation")
	public void testGetPartitions() {
		logger.info("testGetPartitions()");
		Partition[] partitions = rdd.partitions();

		assertNotNull(partitions);
		assertEquals(partitions.length, 8 + 1);
	}

	@Test(dependsOnMethods = "testGetPartitions")
	public void testGetPreferredLocations() {
		logger.info("testGetPreferredLocations()");
		Partition[] partitions = rdd.partitions();

		Seq<String> locations = rdd.getPreferredLocations(partitions[0]);

		assertNotNull(locations);
	}

	@Test(dependsOnMethods = "testGetPreferredLocations")
	public void testCompute() throws CharacterCodingException {
		logger.info("testCompute()");
		long count = rdd.count();
		assertEquals(count, testDataSize);
		Object obj = rdd.collect();

		assertNotNull(obj);

		TestEntity[] entities = (TestEntity[]) obj;

		boolean found = false;
		
		for (TestEntity e : entities) {
			if (e.getId().equals("e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7")) {
				assertEquals(e.getUrl(), "http://11870.com/k/es/de");
				assertEquals(e.getResponseTime(), new Integer(421));
				assertEquals(e.getDownloadTime(), new Long(1380802049275L));
				found = true;
				break;
			}
		}
		
		if (!found){
			fail();
		}
	}

}
