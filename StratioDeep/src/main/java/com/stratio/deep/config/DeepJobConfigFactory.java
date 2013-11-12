package com.stratio.deep.config;

import java.io.Serializable;

import com.stratio.deep.config.impl.DeepJobConfig;
import com.stratio.deep.entity.IDeepType;

public class DeepJobConfigFactory implements Serializable {
	
	private static final long serialVersionUID = -4559130919203819088L;

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
