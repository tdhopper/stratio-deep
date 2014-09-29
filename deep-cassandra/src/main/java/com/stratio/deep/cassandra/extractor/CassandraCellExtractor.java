/*
 * Copyright 2014, Stratio.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.stratio.deep.cassandra.extractor;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.spark.Partition;

import com.stratio.deep.cassandra.config.CellDeepJobConfig;
import com.stratio.deep.cassandra.config.ICassandraDeepJobConfig;
import com.stratio.deep.cassandra.entity.CassandraCell;
import com.stratio.deep.cassandra.functions.CellList2TupleFunction;
import com.stratio.deep.commons.config.DeepJobConfig;
import com.stratio.deep.commons.config.IDeepJobConfig;
import com.stratio.deep.commons.entity.Cell;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.rdd.IExtractor;
import com.stratio.deep.commons.utils.Pair;

/**
 * Concrete implementation of a CassandraRDD representing an RDD of {@link com.stratio.deep.commons.entity.Cells}
 * element.<br/>
 */
public class CassandraCellExtractor extends CassandraExtractor<Cells> {

    private static final long serialVersionUID = -738528971629963221L;

    public CassandraCellExtractor() {
        super();
        this.cassandraJobConfig = new CellDeepJobConfig();
        this.transformer = new CellList2TupleFunction();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Cells transformElement(Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> elem,
            IDeepJobConfig<Cells, ? extends IDeepJobConfig<?, ?>> config) {

        Cells cells = new Cells(((ICassandraDeepJobConfig) config).getTable());
        Map<String, Cell> columnDefinitions = config.columnDefinitions();

        for (Map.Entry<String, ByteBuffer> entry : elem.left.entrySet()) {
            Cell cd = columnDefinitions.get(entry.getKey());
            cells.add(CassandraCell.create(cd, entry.getValue()));
        }

        for (Map.Entry<String, ByteBuffer> entry : elem.right.entrySet()) {
            Cell cd = columnDefinitions.get(entry.getKey());
            if (cd == null) {
                continue;
            }

            cells.add(CassandraCell.create(cd, entry.getValue()));
        }

        return cells;
    }

    @Override
    public Class getConfigClass() {
        return CellDeepJobConfig.class;
    }

    @Override
    public IExtractor<Cells> getExtractorInstance(DeepJobConfig<Cells> config) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stratio.deep.commons.rdd.IExtractor#getPartitions(com.stratio.deep.commons.config.DeepJobConfig)
     */
    @Override
    public Partition[] getPartitions(DeepJobConfig<Cells> config) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stratio.deep.commons.rdd.IExtractor#initIterator(org.apache.spark.Partition,
     * com.stratio.deep.commons.config.DeepJobConfig)
     */
    @Override
    public void initIterator(Partition dp, DeepJobConfig<Cells> config) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.stratio.deep.commons.rdd.IExtractor#initSave(com.stratio.deep.commons.config.DeepJobConfig,
     * java.lang.Object)
     */
    @Override
    public void initSave(DeepJobConfig<Cells> config, Cells first) {
        // TODO Auto-generated method stub

    }

}
