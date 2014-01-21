package com.stratio.deep.util;

import com.stratio.deep.annotations.DeepField;
import com.stratio.deep.entity.DeepByteBuffer;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.exception.DeepGenericException;
import com.stratio.deep.exception.DeepIOException;
import com.stratio.deep.serializer.IDeepSerializer;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.utils.Pair;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import scala.Function1;
import scala.Tuple2;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing useful methods to manipulate the conversion
 * between ByteBuffers maps coming from the underlying Cassandra API to
 * instances of a concrete javabean.
 *
 * @author Luca Rosellini <luca@strat.io>
 */
public final class CassandraRDDUtils {
  private static Logger logger = Logger.getLogger("CassandraRDDUtils");

  /**
   * Creates a new instance of the given class.
   *
   * @param clazz the class object for which a new instance should be created.
   * @return the new instance of class clazz.
   */
  public static <T extends IDeepType> T newTypeInstance(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      logger.error("Cannot instanciate " + clazz, e);
      throw new DeepGenericException(e);
    }
  }

  /**
   * Utility method that:
   * <ol>
   * <li>Uses reflection to obtain the {@link Field} object corresponding<br/>
   * to the field with name <i>buffer.getFieldNam()</i> </li>
   * <li></li>
   * </ol>
   *
   * @param buffer
   * @param instance
   * @param serializer
   * @param beanType
   */
  public static <T extends IDeepType> void setBeanField(DeepByteBuffer<?> buffer,
                                                        T instance, IDeepSerializer<T> serializer, Class<T> beanType) {

    if (buffer == null) {
      return;
    }

    Field f = deepField(buffer.getFieldName(), beanType);

    Object value = serializer.deserialize(buffer);

    try {
      BeanUtils.setProperty(instance, f.getName(), value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new DeepGenericException(e);
    }

  }

  /**
   * Returns the value of the fields <i>f</i> in the instance <i>e</i> of type T.
   *
   * @param e
   * @param f
   * @param <T>
   * @return
   */
  public static <T extends IDeepType> Serializable getBeanFieldValue(T e, Field f) {
    try {
      return (Serializable) PropertyUtils.getProperty(e, f.getName());

    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
      throw new DeepIOException(e1);
    }

  }


  /**
   * Utility method that filters out all the fields _not_ annotated
   * with the {@link DeepField} annotation.
   *
   * @param fields
   * @return
   */
  public static Field[] filterDeepFields(Field[] fields) {
    List<Field> filtered = new ArrayList<>();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];
      if (f.isAnnotationPresent(DeepField.class)) {
        filtered.add(f);
      }
    }
    return filtered.toArray(new Field[0]);
  }

  /**
   * Returns true is given field is part of the table key.
   *
   * @param field
   * @return
   */
  public static boolean isKey(DeepField field){
    return field.isPartOfClusterKey() || field.isPartOfPartitionKey();
  }

  /**
   * Return a pair of Field[] whose left element is
   * the array of keys fields.
   * The right element contains the array of all other non-key fields.
   *
   * @param fields
   * @return
   */
  public static Pair<Field[], Field[]> filterKeyFields(Field[] fields) {
    Field[] filtered = filterDeepFields(fields);
    List<Field> keys = new ArrayList<>();
    List<Field> others = new ArrayList<>();


    for (Field field : filtered) {
      if (isKey(field.getAnnotation(DeepField.class))) {
        keys.add(field);
      } else {
        others.add(field);
      }
    }

    return Pair.create(keys.toArray(new Field[0]), others.toArray(new Field[0]));
  }

  /**
   * Returns the field name as known by the datastore.
   *
   * @param field
   * @return
   */
  public static String deepFieldName(Field field) {

    DeepField annotation = field.getAnnotation(DeepField.class);
    if (StringUtils.isNotEmpty(annotation.fieldName())) {
      return annotation.fieldName();
    } else {
      return field.getName();
    }

  }

  /**
   * Returns a {@link Field} object corresponding to the
   * field of class <i>clazz</i> whose name is <i>id</i>.
   *
   * @param id
   * @param clazz
   * @return
   */
  public static <T extends IDeepType> Field deepField(String id, Class<T> clazz) {
    Field[] fields = filterDeepFields(clazz.getDeclaredFields());

    for (Field field : fields) {
      DeepField annotation = field.getAnnotation(DeepField.class);

      if (id.equals(field.getName()) || id.equals(annotation.fieldName())) {
        return field;
      }
    }

    return null;
  }

  /**
   * Constructs a new object of type T using the serialized
   * tuple coming from Spark.
   *
   * @param tuple
   * @param deepType
   * @param serializer
   * @return
   */
  public static <T extends IDeepType> T createTargetObject(
      Tuple2<Map<String, DeepByteBuffer<?>>, Map<String, DeepByteBuffer<?>>> tuple,
      Class<T> deepType,
      IDeepSerializer<T> serializer) {

    Map<String, DeepByteBuffer<?>> left = tuple._1();
    Map<String, DeepByteBuffer<?>> right = tuple._2();

    T instance = newTypeInstance(deepType);

    for (Map.Entry<String, DeepByteBuffer<?>> entry : left.entrySet()) {
      setBeanField(entry.getValue(), instance, serializer, deepType);
    }

    for (Map.Entry<String, DeepByteBuffer<?>> entry : right.entrySet()) {
      setBeanField(entry.getValue(), instance, serializer, deepType);
    }

    return instance;
  }

  /**
   * Converts a <i>Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>></i>
   * coming from the underlying Cassandra API to a serializable
   * <i>Tuple2<Map<String, DeepByteBuffer<?, T>>,Map<String, DeepByteBuffer<?,T>>></i>
   *
   * @param pair
   * @param deepType
   * @param serializer
   * @return
   */
  public static <T extends IDeepType> Tuple2<Map<String, DeepByteBuffer<?>>, Map<String, DeepByteBuffer<?>>>
  createTupleFromByteBufferPair(
      Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> pair,
      Class<T> deepType,
      IDeepSerializer<T> serializer) {

    Map<String, ByteBuffer> left = pair.left;
    Map<String, ByteBuffer> right = pair.right;

    Map<String, DeepByteBuffer<?>> oLeft = new HashMap<String, DeepByteBuffer<?>>();
    Map<String, DeepByteBuffer<?>> oRight = new HashMap<String, DeepByteBuffer<?>>();

    for (Map.Entry<String, ByteBuffer> entry : left.entrySet()) {
      oLeft.put(entry.getKey(), serializer.serialize(entry, deepType));
    }

    for (Map.Entry<String, ByteBuffer> entry : right.entrySet()) {
      oRight.put(entry.getKey(), serializer.serialize(entry, deepType));
    }

    return new Tuple2<Map<String, DeepByteBuffer<?>>, Map<String, DeepByteBuffer<?>>>(oLeft, oRight);
  }

  /**
   * Utility method that converts pair if Maps coming from the underlying Cassandra API
   * to the declared user type.
   *
   * @param pair
   * @param deepType
   * @param serializer
   * @return
   */
  public static <T extends IDeepType> T pair2DeepType(
      Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> pair,
      Class<T> deepType,
      IDeepSerializer<T> serializer) {

    return createTargetObject(createTupleFromByteBufferPair(pair, deepType, serializer), deepType, serializer);
  }

  /**
   * Convers an instance of type <T> to a tuple of ( Map<String, ByteBuffer>, List<ByteBuffer> ).
   * The first map contains the key column names and the corresponding values.
   * The ByteBuffer list contains the value of the columns that will be bounded to CQL query parameters.
   *
   * @param e
   * @param <T>
   * @return
   */
  public static <T extends IDeepType> Tuple2<Map<String, ByteBuffer>, List<ByteBuffer>> deepType2tuple(T e) {

    Pair<Field[], Field[]> fields = filterKeyFields(e.getClass().getDeclaredFields());

    Field[] keyFields = fields.left;
    Field[] otherFields = fields.right;

    Map<String, ByteBuffer> keys = new HashMap<>();
    List<ByteBuffer> values = new ArrayList<>();

    for (Field keyField : keyFields) {
      AbstractType type = cassandraMarshaller(keyField);

      Serializable value = getBeanFieldValue(e, keyField);

      if (value != null) {
        ByteBuffer bb = type.decompose(value);

        keys.put(deepFieldName(keyField), bb);
      }
    }

    for (Field valueField : otherFields) {
      AbstractType type = cassandraMarshaller(valueField);

      Serializable value = getBeanFieldValue(e, valueField);

      ByteBuffer bb;
      if (value != null) {
        bb = type.decompose(value);
      } else {
        /* if null we propagate an empty array, see CASSANDRA-5885 and CASSANDRA-6180 */
        bb = ByteBuffer.wrap(new byte[0]);
      }

      values.add(bb);
    }

    return new Tuple2<>(keys, values);
  }

  /**
   * Generates the update query for the provided IDeepType.
   * The UPDATE query takes into account all the columns of the entity, even those containing the null value.
   * We do not generate the key part of the update query. The provided query will be concatenated with the key part
   * by CqlRecordWriter.
   *
   * @param tClass
   * @param outputKeyspace
   * @param outputColumnFamily
   * @param <T>
   * @return
   */
  public static <T extends IDeepType> String updateQueryGenerator(Class<T> tClass, String outputKeyspace, String outputColumnFamily) {
    StringBuffer sb = new StringBuffer("UPDATE ")
        .append(outputKeyspace)
        .append(".").append(outputColumnFamily)
        .append(" SET ");

    Pair<Field[], Field[]> fields = filterKeyFields(tClass.getDeclaredFields());

    Field[] otherFields = fields.right;

    int idx = 0;

    for (Field v : otherFields) {

      if (idx > 0) {
        sb.append(", ");
      }

      sb.append("\"" + deepFieldName(v) + "\"").append(" = ?");

      ++idx;

    }

    return sb.toString();
  }

  /**
   * Returns the cassandra's marshalled for the given field.
   *
   * @param f
   * @return
   */
  public static AbstractType<?> cassandraMarshaller(Field f) {

    DeepField annotation = f.getAnnotation(DeepField.class);
    Class<? extends AbstractType<?>> type = annotation.validationClass();

    AbstractType<?> typeInstance = null;
    try {
      typeInstance = TypeParser.parse(type.getName());
    } catch (SyntaxException | ConfigurationException e) {
      throw new DeepGenericException(e);
    }

    return typeInstance;
  }

}
