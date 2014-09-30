/**
 *
 */
package com.stratio.deep.commons.extractor.actions;

import org.apache.spark.Partition;

import com.stratio.deep.commons.config.DeepJobConfig;

/**
 * @author Óscar Puertas
 */
public class InitIteratorAction<T> extends Action {

    private static final long serialVersionUID = -1270097974102584045L;

    private DeepJobConfig<T> config;

    private Partition partition;

    public InitIteratorAction() {
        super();
    }

    public InitIteratorAction(Partition partition, DeepJobConfig<T> config) {
        super(ActionType.INIT_ITERATOR);
        this.config = config;
        this.partition = partition;
    }

    public DeepJobConfig<T> getConfig() {
        return config;
    }

    public Partition getPartition() {
        return partition;
    }
}
