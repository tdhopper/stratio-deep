/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.rdd


import com.stratio.deep.config.IDeepJobConfig
import com.stratio.deep.core.rdd.DeepRDD
import org.apache.hadoop.io.Writable
import org.apache.hadoop.mapreduce._
import org.apache.spark._
import org.apache.spark.annotation.DeveloperApi

import scala.reflect.ClassTag


class NewHadoopPartition(
                          rddId: Int,
                          val index: Int,
                          @transient rawSplit: InputSplit with Writable)
  extends Partition {

  val serializableHadoopSplit = new SerializableWritable(rawSplit)

  override def hashCode(): Int = 41 * (41 + rddId) + index
}

/**
 * :: DeveloperApi ::
 * An RDD that provides core functionality for reading data stored in Hadoop (e.g., files in HDFS,
 * sources in HBase, or S3), using the new MapReduce API (`org.apache.hadoop.mapreduce`).
 *
 * Note: Instantiating this class directly is not recommended, please use
 * [[org.apache.spark.SparkContext.newAPIHadoopRDD( )]]
 *
 * @param sc The SparkContext to associate the RDD with.
 * @param conf The Deep ElasticSearch configuration.
 */
@DeveloperApi
abstract class DeepHadoopRDD[T: ClassTag, K: ClassTag, V: ClassTag](sc: SparkContext,
                                                                    @transient conf: IDeepJobConfig[T, _ <: IDeepJobConfig[_, _]], inputFormatClass: Class[_ <: InputFormat[K, V]])
  extends DeepRDD[T](sc, conf.getEntityClass)
  with SparkHadoopMapReduceUtil
  with Logging {
  //    IDeepJobConfig[T, _ <:IDeepJobConfig[_,_<:AnyRef]]
  //  A Hadoop Configuration can be about 10 KB, which is pretty big, so broadcast it

  //    this.config = sc.broadcast(conf).asInstanceOf[Broadcast[IDeepJobConfig[T, _ <: IDeepJobConfig[_,_]]]]
  //  //  private val confBroadcast = sc.broadcast(conf)
  //    // private val serializableConf = new SerializableWritable(conf)
  //
  //    private val jobTrackerId: String = {
  //      val formatter = new SimpleDateFormat("yyyyMMddHHmm")
  //      formatter.format(new Date())
  //    }
  //
  //    @transient protected val jobId = new JobID(jobTrackerId, id)
  //
  //    override def getPartitions: Array[Partition] = {
  //      val inputFormat = inputFormatClass.newInstance
  //      inputFormat match {
  //        case configurable: Configurable =>
  //          configurable.setConf(config.getValue.getHadoopConfiguration)
  //        case _ =>
  //      }
  //      val jobContext = newJobContext(config.getValue.getHadoopConfiguration, jobId)
  //      val rawSplits = inputFormat.getSplits(jobContext).toArray
  //      val result = new Array[Partition](rawSplits.size)
  //      for (i <- 0 until rawSplits.size) {
  //        result(i) = new NewHadoopPartition(id, i, rawSplits(i).asInstanceOf[InputSplit with Writable])
  //      }
  //      result
  //    }
  //
  //    def transformElement(tuple: (K, V), config: Broadcast[IDeepJobConfig[T, _ <: IDeepJobConfig[_, _]]]): T
  //
  //    override def compute(theSplit: Partition, context: TaskContext): InterruptibleIterator[T] = {
  //      val iter = new Iterator[T] {
  //        val split = theSplit.asInstanceOf[NewHadoopPartition]
  //        logInfo("Input split: " + split.serializableHadoopSplit)
  //        val conf = config.value.getHadoopConfiguration
  //        val attemptId = newTaskAttemptID(jobTrackerId, id, isMap = true, split.index, 0)
  //        val hadoopAttemptContext = newTaskAttemptContext(conf, attemptId)
  //        val format = inputFormatClass.newInstance
  //        format match {
  //          case configurable: Configurable =>
  //            configurable.setConf(conf)
  //          case _ =>
  //        }
  //        val reader = format.createRecordReader(
  //          split.serializableHadoopSplit.value, hadoopAttemptContext)
  //        reader.initialize(split.serializableHadoopSplit.value, hadoopAttemptContext)
  //
  //        // Register an on-task-completion callback to close the input stream.
  //        context.addOnCompleteCallback(() => close())
  //        var havePair = false
  //        var finished = false
  //
  //        override def hasNext: Boolean = {
  //          if (!finished && !havePair) {
  //            finished = !reader.nextKeyValue
  //            havePair = !finished
  //          }
  //          !finished
  //        }
  //
  //        override def next(): T = {
  //          if (!hasNext) {
  //            throw new java.util.NoSuchElementException("End of stream")
  //          }
  //          havePair = false
  //
  //          val tuple = (reader.getCurrentKey, reader.getCurrentValue)
  //          transformElement(tuple, config)
  //        }
  //
  //        private def close() {
  //          try {
  //            reader.close()
  //          } catch {
  //            case e: Exception => logWarning("Exception in RecordReader.close()", e)
  //          }
  //        }
  //      }
  //      new InterruptibleIterator(context, iter)
  //    }
  //
  //    override def getPreferredLocations(split: Partition): Seq[String] = {
  //      val theSplit = split.asInstanceOf[NewHadoopPartition]
  //      theSplit.serializableHadoopSplit.value.getLocations.filter(_ != "localhost")
  //    }
  //
  //    def getConf: IDeepJobConfig[T, _ <:IDeepJobConfig[_,_]]  = config.value
  //

}
