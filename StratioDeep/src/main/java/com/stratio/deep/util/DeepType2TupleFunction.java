package com.stratio.deep.util;

import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.functions.AbstractSerializableFunction1;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
* Created by luca on 20/01/14.
*/
public class DeepType2TupleFunction<T extends IDeepType>
    extends AbstractSerializableFunction1<T, Tuple2<Map<String,ByteBuffer>,List<ByteBuffer>>> {

  @Override
  public Tuple2<Map<String, ByteBuffer>, List<ByteBuffer>> apply(T e) {
    return CassandraRDDUtils.deepType2tuple(e);
  }
}
