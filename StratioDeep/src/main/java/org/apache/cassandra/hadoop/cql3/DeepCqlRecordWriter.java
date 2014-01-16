package org.apache.cassandra.hadoop.cql3;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.hadoop.AbstractColumnFamilyRecordWriter;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.hadoop.Progressable;
import org.apache.cassandra.hadoop.cql3.CqlConfigHelper;
import org.apache.cassandra.hadoop.cql3.CqlOutputFormat;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom copy of not-extensible org.apache.cassandra.hadoop.cql3.CqlRecordWriter.
 *
 */
public final class DeepCqlRecordWriter extends CqlRecordWriter {

  DeepCqlRecordWriter(TaskAttemptContext context) throws IOException {
    super(context);
  }

  DeepCqlRecordWriter(Configuration conf, Progressable progressable) throws IOException {
    super(conf, progressable);
  }

  DeepCqlRecordWriter(Configuration conf) {
    super(conf);
  }
}
