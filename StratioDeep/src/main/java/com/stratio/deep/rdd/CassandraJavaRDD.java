package com.stratio.deep.rdd;

import com.stratio.deep.entity.IDeepType;
import org.apache.spark.api.java.JavaRDD;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;
import scala.reflect.api.TypeTags;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

/**
 * Commodity RDD implementation that should be used as a
 * Java Wrapper for {@link CassandraRDD}.
 *
 * @param <W>
 * @author Luca Rosellini <luca@strat.io>
 */
public final class CassandraJavaRDD<W extends IDeepType> extends JavaRDD<W> {
  private static final int FIRST_FIELD = 0;
  private static final long serialVersionUID = -3208994171892747470L;

  /**
   * Default constructor. Constructs a new Java-friendly Cassandra RDD
   *
   * @param rdd
   */
  @SuppressWarnings({"unchecked"})
  public CassandraJavaRDD(CassandraRDD<W> rdd) {
    super(rdd, ClassTag$.MODULE$.<W>apply(rdd.config.value().getEntityClass()));
  }

  @Override
  public ClassTag<W> classTag() {
    return ClassTag$.MODULE$.<W>apply(((CassandraRDD<W>)this.rdd()).config.value().getEntityClass());
  }
}
