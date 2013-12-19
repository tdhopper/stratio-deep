package com.stratio.deep.rdd;

import static scala.reflect.ClassManifest$.*;

import java.lang.reflect.ParameterizedType;

import org.apache.spark.api.java.JavaRDD;

import com.stratio.deep.entity.IDeepType;

/**
 * Commodity RDD implementation that should be used as a
 * Java Wrapper for {@link CassandraRDD}.
 * 
 * @author Luca Rosellini <luca@strat.io>
 *
 * @param <T>
 */
public final class CassandraJavaRDD<T extends IDeepType> extends JavaRDD<T> {
    private static final int FIRST_FIELD = 0;
    private static final long serialVersionUID = -3208994171892747470L;

    /**
     * Default constructor. Constructs a new Java-friendly Cassandra RDD
     * 
     * @param rdd
     */
    @SuppressWarnings({ "unchecked" })
    public CassandraJavaRDD(CassandraRDD<T> rdd) {
	super(rdd, MODULE$
		.fromClass((Class<T>) ((java.lang.reflect.TypeVariable<?>) ((ParameterizedType) CassandraJavaRDD.class
			.getGenericSuperclass()).getActualTypeArguments()[FIRST_FIELD]).getGenericDeclaration()));
    }
}
