package com.stratio.deep.rdd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.context.AbstractDeepSparkContextTest;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.JavaSerializer;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.SerializerInstance;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.collection.Iterator;
import scala.collection.Seq;

import static org.testng.Assert.*;

public abstract class CassandraGenericRDDTest<W> extends AbstractDeepSparkContextTest {
    private Logger logger = Logger.getLogger(getClass());

    private CassandraGenericRDD<W> rdd;
    private IDeepJobConfig<W> rddConfig;
    private IDeepJobConfig<W> writeConfig;

    protected abstract void checkComputedData(W[] entities);

    protected abstract void checkSimpleTestData();

    protected CassandraGenericRDD<W> getRDD() {
	return this.rdd;
    }

    protected IDeepJobConfig<W> getReadConfig() {
	return rddConfig;
    }

    protected IDeepJobConfig<W> getWriteConfig() {
	return writeConfig;
    }

    protected abstract CassandraGenericRDD<W> initRDD();

    protected abstract IDeepJobConfig<W> initReadConfig();

    @BeforeClass
    protected void initServerAndRDD() throws IOException, URISyntaxException, ConfigurationException,
	    InterruptedException {

	rddConfig = initReadConfig();
	writeConfig = initWriteConfig();
	rdd = initRDD();
    }

    protected abstract IDeepJobConfig<W> initWriteConfig();

    @Test
    public void testJavaSerialization(){
	JavaSerializer ser = new JavaSerializer(context.getConf());

	SerializerInstance instance = ser.newInstance();

	ByteBuffer serializedRDD = instance.serialize(rdd);

	CassandraGenericRDD<W> deserializedRDD = instance.deserialize(serializedRDD);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	SerializationStream serializationStream = instance.serializeStream(baos);
	serializationStream = serializationStream.writeObject(rdd);

	serializationStream.flush();
	serializationStream.close();

	byte[] buffer = new byte[1024];
	ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

	DeserializationStream deserializationStream = instance.deserializeStream(bais);
	Iterator<Object> iter = deserializationStream.asIterator();
	assertTrue(iter.hasNext());

	deserializedRDD = (CassandraGenericRDD)iter.next();
	assertNotNull(deserializedRDD);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = "testGetPreferredLocations")
    public void testCompute() throws CharacterCodingException {

	logger.info("testCompute()");
	Object obj = getRDD().collect();

	assertNotNull(obj);

	W[] entities = (W[]) obj;

	checkComputedData(entities);
    }

    @Test(dependsOnMethods = "testRDDInstantiation")
    public void testGetPartitions() {
	logger.info("testGetPartitions()");
	Partition[] partitions = getRDD().partitions();

	assertNotNull(partitions);
	assertEquals(partitions.length, 8 + 1);
    }

    @Test(dependsOnMethods = "testGetPartitions")
    public void testGetPreferredLocations() {
	logger.info("testGetPreferredLocations()");
	Partition[] partitions = getRDD().partitions();

	Seq<String> locations = getRDD().getPreferredLocations(partitions[0]);

	assertNotNull(locations);
    }

    @Test
    public void testRDDInstantiation() {
	logger.info("testRDDInstantiation()");
	assertNotNull(getRDD());
    }

    @Test(dependsOnMethods = "testSimpleSaveToCassandra")
    public abstract void testSaveToCassandra();

    @Test(dependsOnMethods = "testCompute")
    public abstract void testSimpleSaveToCassandra();

    protected void truncateCf(String keyspace, String cf) {
	executeCustomCQL("TRUNCATE  " + keyspace + "." + cf);

    }
}
