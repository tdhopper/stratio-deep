/**
 *
 */
package com.stratio.deep.commons.extractor.actions;

import com.stratio.deep.commons.config.DeepJobConfig;

/**
 * Created by rcrespo on 20/08/14.
 */
public class ExtractorInstanceAction<T> extends Action {

    private static final long serialVersionUID = -1270097974102584045L;

    private DeepJobConfig<T> config;

    public ExtractorInstanceAction(DeepJobConfig<T> config) {
        super(ActionType.EXTRACTOR_INSTANCE);
    }

    public DeepJobConfig<T> getConfig() {
        return config;
    }
}
