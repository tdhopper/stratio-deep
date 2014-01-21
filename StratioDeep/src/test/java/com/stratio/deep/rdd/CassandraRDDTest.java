package com.stratio.deep.rdd;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.stratio.deep.config.DeepJobConfigFactory;
import com.stratio.deep.context.AbstractDeepSparkContextTest;
import com.stratio.deep.embedded.CassandraServer;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.functions.AbstractSerializableFunction1;
import com.stratio.deep.util.Constants;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.rdd.RDD;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.Function1;
import scala.collection.Seq;
import scala.reflect.ClassTag$;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.CharacterCodingException;

import static com.stratio.deep.util.CassandraRDDUtils.updateQueryGenerator;
import static org.testng.Assert.*;

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
        .outputKeyspace(OUTPUT_KEYSPACE_NAME)
        .columnFamily(COLUMN_FAMILY)
        .outputColumnFamily(OUTPUT_COLUMN_FAMILY)
    ;

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

    if (!found) {
      fail();
    }
  }

  @Test(dependsOnMethods = "testCompute")
  public void testUpdateQueryGenerator() {

    String sql = updateQueryGenerator(TestEntity.class, rddConfig.getOutputKeyspace(), rddConfig.getOutputColumnFamily());

    assertEquals(sql, "UPDATE " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY + " SET \"domain_name\" = ?, \"url\" = ?, \"response_time\" = ?, \"response_code\" = ?, \"download_time\" = ?");
  }

  @Test(dependsOnMethods = "testUpdateQueryGenerator")
  public void testSimpleSaveToCassandra() {
    CassandraRDD.saveToCassandra(rdd, rddConfig);
    checkSimpleTestData();
  }

  private void checkSimpleTestData() {
    Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
        .addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
    Session session = cluster.connect();

    String command = "select count(*) from " + KEYSPACE_NAME + "." + COLUMN_FAMILY + ";";

    ResultSet rs = session.execute(command);
    assertEquals(rs.one().getLong(0), testDataSize);

    command = "select * from " + KEYSPACE_NAME + "." + COLUMN_FAMILY + " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

    rs = session.execute(command);
    Row row = rs.one();

    assertEquals(row.getString("domain_name"), "11870.com");
    assertEquals(row.getInt("response_time"), 421);
    assertEquals(row.getLong("download_time"), 1380802049275L);
    assertEquals(row.getString("url"), "http://11870.com/k/es/de");
    session.shutdown();
  }

  @Test(dependsOnMethods = "testSimpleSaveToCassandra")
  public void testSaveToCassandra() {
    Function1<TestEntity, TestEntity> mappingFunc = new TestEntityAbstractSerializableFunction1();

    RDD<TestEntity> mappedRDD =
        rdd.map(mappingFunc, ClassTag$.MODULE$.<TestEntity>apply(TestEntity.class));

    executeCustomCQL("TRUNCATE  " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY);

    CassandraRDD.saveToCassandra(mappedRDD, rddConfig);

    checkOutputTestData();
  }

  private void checkOutputTestData() {
    Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
        .addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
    Session session = cluster.connect();

    String command = "select count(*) from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY + ";";

    ResultSet rs = session.execute(command);
    assertEquals(rs.one().getLong(0), testDataSize);

    command = "SELECT * from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY +
        " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

    rs = session.execute(command);
    Row row = rs.one();

    assertEquals(row.getString("domain_name"), "11870.com");
    assertEquals(row.getString("url"), "http://11870.com/k/es/de");
    assertEquals(row.getInt("response_time"), 421 + 1);

    //TODO: cannot delete a column using CQL, forcing it to null converts it to 0!!! see CASSANDRA-5885 and CASSANDRA-6180
    assertEquals(row.getLong("download_time"), 0);
    session.shutdown();
  }

  private static class TestEntityAbstractSerializableFunction1
      extends AbstractSerializableFunction1<TestEntity, TestEntity> {

    @Override
    public TestEntity apply(TestEntity e) {
      return new TestEntity(
          e.getId(),
          e.getDomain(),
          e.getUrl(),
          e.getResponseTime() + 1,
          e.getResponseCode(),
          null,
          e.getNotMappedField());
    }
  }
}
