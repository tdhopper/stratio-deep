package com.stratio.deep.partition.impl;

import org.apache.cassandra.hadoop.ColumnFamilySplit;
import org.apache.hadoop.io.Writable;
import org.apache.spark.Partition;
import org.apache.spark.SerializableWritable;

public class DeepPartition implements Partition {

	private static final int MAGIC_NUMBER = 41;

	private static final long serialVersionUID = 4822039463206513988L;

	private final int rddId;
	private final int idx;
	private final SerializableWritable<ColumnFamilySplit> splitWrapper;

	public SerializableWritable<ColumnFamilySplit> splitWrapper() {
		return this.splitWrapper;
	}

	public int hashCode() {
		return (MAGIC_NUMBER * (MAGIC_NUMBER + this.rddId) + this.idx);
	}

	public int index() {
		return this.idx;
	}

	public DeepPartition(int rddId, int idx, Writable s) {

		this.splitWrapper = new SerializableWritable<ColumnFamilySplit>(
				(ColumnFamilySplit) s);
		this.rddId = rddId;
		this.idx = idx;
	}

}
