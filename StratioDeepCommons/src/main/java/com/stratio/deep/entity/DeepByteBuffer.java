package com.stratio.deep.entity;

import java.io.Serializable;

public interface DeepByteBuffer<F> {

	public abstract Class<F> getType();

	public abstract String getFieldName();

	public abstract Serializable getObject();

}