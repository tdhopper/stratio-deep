package com.stratio.deep.context;

import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.io.Resources;
import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.embedded.CassandraServer;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.util.Constants;

public abstract class AbstractDeepSparkContextTest {

	private Logger logger = Logger.getLogger(getClass());
	protected static DeepSparkContext context;
	protected RDD<TestEntity> rdd;
	protected IDeepJobConfig<TestEntity> rddConfig;
	private static CassandraServer cassandraServer;
	protected static final String KEYSPACE_NAME = "test_keyspace";
	protected static final String COLUMN_FAMILY = "test_page";
  protected static final String OUTPUT_KEYSPACE_NAME = "out_test_keyspace";
  protected static final String OUTPUT_COLUMN_FAMILY = "out_test_page";

  protected String createOutputCF = "CREATE TABLE " + OUTPUT_KEYSPACE_NAME+"."+OUTPUT_COLUMN_FAMILY + " (id text PRIMARY KEY, " +
      "url text, " +
      "domain_name text, " +
      "response_code int, " +
      "charset text," +
      "response_time int," +
      "download_time bigint," +
      "first_download_time bigint," +
      "title text ) ;";

  protected static int testDataSize = 0;

	private String buildTestDataInsertBatch() {
		URL testData = Resources.getResource("testdata.csv");
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(testData.toURI()))))){
			String line = null;
			
			java.util.List<String> inserts = new ArrayList<String>();
			
			while ((line = br.readLine()) != null ){
				String[] fields = (COLUMN_FAMILY+","+line).split(",");
				String rawInsert = "INSERT INTO %s (\"id\", \"charset\", \"domain_name\", \"download_time\", \"response_time\", \"first_download_time\", \"url\") values (\'%s\', \'%s\', \'%s\', %s, %s, %s, \'%s\');";
				String insert = String.format(rawInsert, (Object[])fields);
				inserts.add(insert);
				++testDataSize;
			}
			
			if (inserts.size() > 0){
				String batch = "BEGIN BATCH ";
				
				for (String insert : inserts) {
					batch += insert;
				}
				
				batch += " APPLY BATCH; " ;
				
				return batch;
			} 
		} catch (Exception e){
			logger.error("Error",e);
		}
		
		return "";
	}

	private void checkTestData() {
		Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
				.addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
		Session session = cluster.connect();
		
		String command = "select count(*) from " + KEYSPACE_NAME + "." +COLUMN_FAMILY+";";
		
		ResultSet rs = session.execute(command);
		assertEquals(rs.one().getLong(0), testDataSize);
		
		command = "select * from " + KEYSPACE_NAME + "." +COLUMN_FAMILY+" WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";
		
		rs = session.execute(command);
		Row row = rs.one();
		
		assertEquals(row.getString("domain_name"), "11870.com");
		assertEquals(row.getInt("response_time"), 421);
		assertEquals(row.getLong("download_time"), 1380802049275L);
		assertEquals(row.getString("charset"), "UTF-8");
		assertEquals(row.getLong("first_download_time"), 1380802049276L);
		assertEquals(row.getString("url"), "http://11870.com/k/es/de");
		session.shutdown();
	}

	@BeforeSuite
	protected void initContextAndServer() throws ConfigurationException, IOException, InterruptedException{
		context = new DeepSparkContext("local[4]", "deepSparkContextTest");

    String createKeyspace = "CREATE KEYSPACE "
				+ KEYSPACE_NAME
				+ " WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1 };";

    String createOutputKeyspace = "CREATE KEYSPACE "
        + OUTPUT_KEYSPACE_NAME
        + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1 };";

    String useKeyspace = "USE " + KEYSPACE_NAME + ";";

    String createCF = "CREATE TABLE " + COLUMN_FAMILY + " (id text PRIMARY KEY, " +
				"url text, " +
				"domain_name text, " +
				"response_code int, " +
				"charset text," +
				"response_time int," +
				"download_time bigint," +
				"first_download_time bigint," +
				"title text ) ;";

    String useOutputKeyspace = "USE " + OUTPUT_KEYSPACE_NAME + ";";


		String initialDataset = buildTestDataInsertBatch();
		
		String[] startupCommands = new String[] {
				createKeyspace, createOutputKeyspace, useKeyspace, createCF, initialDataset, useOutputKeyspace, createOutputCF };
		
		cassandraServer = new CassandraServer();
		cassandraServer.setStartupCommands(startupCommands);
		cassandraServer.start();
		
		checkTestData();
		
	}

  protected void executeCustomCQL(String... cqls){

    Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
        .addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
    Session session = cluster.connect();
    for (String cql : cqls) {
      session.execute(cql);
    }
    session.shutdown();
  }

	@AfterSuite
	protected void disposeServerAndRdd() throws IOException {
		if (cassandraServer != null){
			cassandraServer.shutdown();
		}
		
		if (context != null){
			context.stop();
		}
	}

}
