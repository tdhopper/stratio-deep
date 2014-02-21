package com.stratio.deep.serializer;

import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import org.apache.spark.rdd.RDD;
import org.apache.spark.serializer.KryoRegistrator;

/**
 * Created by luca on 20/02/14.
 */
public class DeepKryoRegistrator implements KryoRegistrator {
    @Override
    public void registerClasses(Kryo kryo) {

	kryo.register(RDD.class);
	kryo.register(String.class);
	kryo.register(Map.class);

    }
}
