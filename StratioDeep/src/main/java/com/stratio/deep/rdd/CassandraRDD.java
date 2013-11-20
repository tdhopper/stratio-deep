package com.stratio.deep.rdd;

import static com.stratio.deep.util.CassandraRDDUtils.*;
import static scala.collection.JavaConversions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.hadoop.cql3.CqlPagingRecordReader;
import org.apache.cassandra.utils.Pair;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;

import scala.collection.Iterator;
import scala.collection.Seq;
import scala.reflect.ClassManifest$;
import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.cql3.DeepCqlPagingInputFormat;
import com.stratio.deep.cql3.DeepCqlPagingRecordReader;
import com.stratio.deep.entity.IDeepType;
import com.stratio.deep.exception.DeepIOException;
import com.stratio.deep.partition.impl.DeepPartition;
import com.stratio.deep.serializer.IDeepSerializer;

/**
 * Stratio's implementation of an RDD reading and writing
 * data from and to Apache Cassandra. 
 * This implementation uses Cassandra's Hadoop API.
 * 
 * We do not use Map<String,ByteBuffer> as key and value objects,
 * since ByteBuffer is not serializable.
 * 
 * @author Luca Rosellini <luca@strat.io>
 * 
 */
public final class CassandraRDD<T extends IDeepType> extends RDD<T> {
	//private transient Logger logger = 	;

	private static final String STRATIO_DEEP_JOB_PREFIX = "stratio-deep-job-";
	private static final String STRATIO_DEEP_TASK_PREFIX = "stratio-deep-task-";
	
	private static final long serialVersionUID = -3208994171892747470L;

	/*
	 * An Hadoop Job Id is needed by the underlying cassandra's API.
	 * 
	 * We make it transient in order to prevent this to be sent through the wire
	 * to slaves.
	 */
	private final transient JobID hadoopJobId;

	/*
	 * RDD configuration. This config is broadcasted to all the Sparks machines.
	 */
	private final Broadcast<IDeepJobConfig<T>> config;
	
	/**
	 * Constructs a new CassandraRDD taking a Spark context and a configuration
	 * object as arguments.
	 * 
	 * @param sc		Spark context.
	 * @param config	CassandraRDD's configuration object.
	 */
	@SuppressWarnings("unchecked")
	public CassandraRDD(SparkContext sc, IDeepJobConfig<T> config) {
		
		super(sc, scala.collection.immutable.Seq$.MODULE$.empty(), ClassManifest$.MODULE$.fromClass(config.getEntityClass()) );
		
		long timestamp = System.currentTimeMillis();
		hadoopJobId = new JobID(STRATIO_DEEP_JOB_PREFIX + timestamp, id());
		this.config = sc.broadcast(config);
	}
	
	/**
	 * Computes the current RDD over the given data partition.
	 * Returns an iterator of Scala tuples.
	 */
	@Override
	public Iterator<T> compute(Partition split, TaskContext ctx) {

		final DeepCqlPagingInputFormat inputFormat = new DeepCqlPagingInputFormat();
		DeepPartition deepPartition = (DeepPartition) split;
		
		log().debug("Executing compute for split: "+ deepPartition);

		final DeepCqlPagingRecordReader recordReader = initRecordReader(ctx, inputFormat, deepPartition);

		/*
		 * Creates a new anonymous iterator inner class and returns it as a scala iterator.
		 */
		java.util.Iterator<T> recordReaderIterator = new java.util.Iterator<T>() {

			/**
			 * (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return recordReader.hasNext();				
			}

			/**
			 * (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public T next() {
				Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> kv = recordReader.next();
				
				/*
				 * I Need to convert a pair of Map<String, ByteBuffer> to a tuple of
				 * Map<String, DeepByteBuffer<?>>. I need to serialize each field and then build 
				 * a DeepByteBuffer with it.
				 */
				Class<T> entityClass = config.value().getEntityClass();
				IDeepSerializer<T> serializer = config.value().getSerializer();
				return pair2DeepType(kv, entityClass, serializer);
			}

			/**
			 * (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				throw new DeepIOException("Method not implemented (and won't be implemented anytime soon!!!)");
			}
		};

		return asScalaIterator(recordReaderIterator);
	}
	

	/**
	 * Initializes a {@link CqlPagingRecordReader} using Cassandra's Hadoop API.
	 * 
	 * 1. Constructs a {@link TaskAttemptID}
	 * 2. Constructs a {@link TaskAttemptContext} using the newly constructed {@link TaskAttemptID}
	 * 		and the hadoop configuration contained inside this RDD configuration object.
	 * 3. Creates a new {@link DeepCqlPagingRecordReader}.
	 * 4. Initialized the newly created {@link DeepCqlPagingRecordReader}.
	 * 5. Registers a new instance of {@link OnComputedRDDCallback} as  spark's onCompleteCallback.
	 * 
	 * @param ctx
	 * @param inputFormat
	 * @param dp
	 * @return
	 */
	private DeepCqlPagingRecordReader initRecordReader(
			TaskContext ctx, final DeepCqlPagingInputFormat inputFormat,
			final DeepPartition dp) {
		try {
			
			TaskAttemptID attemptId = new TaskAttemptID(STRATIO_DEEP_TASK_PREFIX
					+ System.currentTimeMillis(), id(), true, dp.index(), 0);

			TaskAttemptContext taskCtx = new TaskAttemptContext(config.value()
					.getConfiguration(), attemptId);
			
			final DeepCqlPagingRecordReader recordReader = (DeepCqlPagingRecordReader) inputFormat
					.createRecordReader(dp.splitWrapper().value(), taskCtx);
			
			log().debug("Initializing recordReader for split: "+ dp);
			recordReader.initialize(dp.splitWrapper().value(), taskCtx);

			ctx.addOnCompleteCallback(new OnComputedRDDCallback<BoxedUnit>(
					recordReader, dp));

			return recordReader;
		} catch (IOException | InterruptedException e) {
			throw new DeepIOException(e);
		}
	}

	/**
	 * Returns the partitions on which this RDD depends on.
	 * 
	 * Uses the underlying CqlPagingInputFormat in order to retreive the splits.
	 * 
	 * The number of splits, and hence the number of partitions equals to the
	 * number of tokens configured in cassandra.yaml + 1.
	 */
	@Override
	public Partition[] getPartitions() {
		final JobContext hadoopJobContext = new JobContext(config.value()
				.getConfiguration(), hadoopJobId);
		
		final DeepCqlPagingInputFormat cqlInputFormat = new DeepCqlPagingInputFormat();

		List<InputSplit> underlyingInputSplits;
		try {
			underlyingInputSplits = cqlInputFormat.getSplits(hadoopJobContext);
		} catch (IOException e) {
			throw new DeepIOException(e);
		}

		Partition[] partitions = new DeepPartition[underlyingInputSplits.size()];

		for (int i = 0; i < underlyingInputSplits.size(); i++) {
			InputSplit split = underlyingInputSplits.get(i);
			partitions[i] = new DeepPartition(id(), i, (Writable) split);
			
			log().debug("Detected partition: "+partitions[i]);
		}
		return partitions;
	}

	/**
	 * Returns a list of hosts on which the given split resides.
	 * 
	 * TODO: check what happens in an environment where the split is replicated on N machines.
	 * It would be optimum if the RDD were computed only on the machine(s) where the split resides.
	 */
	@Override
	public Seq<String> getPreferredLocations(Partition split) {
		DeepPartition p = (DeepPartition) split;

		String[] locations = p.splitWrapper().value().getLocations();
		log().debug("getPreferredLocations: " + p);
		
		return asScalaBuffer(Arrays.asList(locations));

	}

	/**
	 * Helper callback class called by Spark when the current RDD is computed successfully.
	 * This class simply closes the {@link CqlPagingRecordReader} passed as an argument.
	 * 
	 * @author Luca Rosellini <luca@strat.io>
	 *
	 * @param <R>
	 */
	class OnComputedRDDCallback<R> extends AbstractFunction0<R> {
		private final RecordReader<Map<String, ByteBuffer>, Map<String, ByteBuffer>> recordReader;
		private final DeepPartition deepPartition;
		
		public OnComputedRDDCallback(
				RecordReader<Map<String, ByteBuffer>, Map<String, ByteBuffer>> recordReader, DeepPartition dp) {
			super();
			this.recordReader = recordReader;
			this.deepPartition = dp;
		}

		@Override
		public R apply() {
			try {
				log().debug("Closing context for partition " + deepPartition);
				
				recordReader.close();
			} catch (IOException e) {
				throw new DeepIOException(e);
			}
			return null;
		}

	}
}
