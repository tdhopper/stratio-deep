package com.stratio.deep.config;

import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.config.impl.DeepJobConfig;
import com.stratio.deep.entity.IDeepType;

public class DeepJobConfigFactory {
	/**
	 * Factory method.
	 * 
	 * @return
	 */
	public static <T extends IDeepType> IDeepJobConfig<T> create(Class<T> entityClass){
		IDeepJobConfig<T> res = new DeepJobConfig<T>(entityClass);
		return res;
	}
}
