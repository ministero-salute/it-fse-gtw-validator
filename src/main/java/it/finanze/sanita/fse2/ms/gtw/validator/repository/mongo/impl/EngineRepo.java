/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IEngineRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Repository;

import static it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY.FIELD_LAST_SYNC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Repository
@Slf4j
public class EngineRepo implements IEngineRepo {

	@Autowired
	private MongoTemplate mongo;
 
	@Override
	public EngineETY getLatestEngine() {

		EngineETY out;

		TypedAggregation<EngineETY> q = new TypedAggregation<>(
			EngineETY.class,
			sort(DESC, FIELD_LAST_SYNC),
			limit(1)
		);

		try {
			out = mongo.aggregate(q, EngineETY.class).getUniqueMappedResult();
		} catch(Exception ex) {
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}

}