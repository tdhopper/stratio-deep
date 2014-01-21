package com.stratio.deep.util;

import static com.stratio.deep.util.CassandraRDDUtils.*;
import static org.testng.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.db.marshal.Int32Type;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.utils.Pair;
import org.testng.annotations.Test;

import scala.Tuple2;

import com.stratio.deep.entity.DeepByteBuffer;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.exception.DeepGenericException;
import com.stratio.deep.serializer.IDeepSerializer;
import com.stratio.deep.serializer.impl.DefaultDeepSerializer;

public class CassandraRDDUtilsTest {
	
	class NotInstantiable implements IDeepType{

		private static final long serialVersionUID = -3311345712290429412L;
	}

	@Test
	public void testFilterDeepFields() {
		Field[] fields = TestEntity.class.getDeclaredFields();

		assertTrue(fields.length > 6);

		fields = filterDeepFields(fields);

		assertEquals(fields.length, 6);
	}

	@Test
	public void testNewTypeInstance() {
		try {
			newTypeInstance(NotInstantiable.class);

			fail();
		} catch (DeepGenericException e) {
			// OK
		} catch (Exception e) {
			fail();
		}

		CassandraRDDUtils.newTypeInstance(TestEntity.class);
	}

	@Test
	public void testCreateTargetObject() throws SyntaxException,
			ConfigurationException, InvocationTargetException,
			IllegalAccessException, NoSuchFieldException, SecurityException {

		
		Map<String, ByteBuffer> left = new HashMap<String, ByteBuffer>();
		left.put("id", UTF8Type.instance.decompose("myTestId"));
		left.put("url", UTF8Type.instance.decompose("myLongURL"));
		left.put("response_code", Int32Type.instance.decompose(200));
		
		Map<String, ByteBuffer> right = new HashMap<String, ByteBuffer>();
		Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> pair = 
				Pair.create(left, right);
		
		IDeepSerializer<TestEntity> serializer = new DefaultDeepSerializer<>();
		
		/* serialize and convert */
		Tuple2<Map<String, DeepByteBuffer<?>>,Map<String, DeepByteBuffer<?>>>
			tuple = createTupleFromByteBufferPair(pair, TestEntity.class, serializer);

		/* deserialize, create target object and perform tests */
		TestEntity targetObject = createTargetObject(tuple, TestEntity.class, serializer );
		
		assertEquals(targetObject.getId(), "myTestId");

		assertEquals(targetObject.getUrl(), "myLongURL");

		assertEquals(targetObject.getResponseCode(), new Integer(200));

	}

  @Test
  public void testFilterKeyFields(){
    Field[] fields = TestEntity.class.getDeclaredFields();

    Pair<Field[], Field[]> keyFields = filterKeyFields(filterDeepFields(fields));

    assertNotNull(keyFields);
    assertNotNull(keyFields.left);
    assertNotNull(keyFields.right);
    assertTrue(keyFields.left.length == 1);
    assertTrue(keyFields.right.length == 5);

    assertTrue(keyFields.left[0].getName().equals("id"));
  }

  @Test
  public void testCassandraMarshaller(){

    Field[] fields = filterDeepFields(TestEntity.class.getDeclaredFields());

    for (Field f : fields){
      if (f.getName().equals("LongType")){
        assertEquals(cassandraMarshaller(f).getClass(), LongType.class);
      }

      if (f.getName().equals("responseCode")){
        assertEquals(cassandraMarshaller(f).getClass(), Int32Type.class);
      }

      if (f.getName().equals("id")){
        assertEquals(cassandraMarshaller(f).getClass(), UTF8Type.class);
      }
    }
  }

  @Test
  public void testDeepType2Pair(){

    TestEntity te = new TestEntity();
    te.setDomain("abcd");
    te.setId("48297148932");
    te.setResponseCode(312);

    Tuple2<Map<String, ByteBuffer>, List<ByteBuffer>> pair =
        deepType2tuple(te);

    assertNotNull(pair);
    assertNotNull(pair._1());
    assertNotNull(pair._2());
    assertEquals(pair._1().size(),1);
    assertEquals(pair._2().size(),5);
  }
}
