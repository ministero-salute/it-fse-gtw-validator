package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;


/**
 *  @author vincenzoingenito
 *  Abstract mongo repository. 
 */
@Component
@Slf4j
public abstract class AbstractMongoRepo<T, K> {

	/**
	 * Template to access at MongoDB.
	 */
    @Autowired
    private MongoTemplate mongoTemplate;

	/**
	 * Insert or update.
	 * 
	 * @param entity	ety
	 * @return			updated ety
	 */
	protected T upsert(final T entity) {
		return mongoTemplate.save(entity);
	}

	/**
	 * Insert.
	 * 
	 * @param entity	ety
	 * @return			updated ety
	 */
	protected T insert(final T entity) {
		T output = null;
		try {
			output = mongoTemplate.insert(entity);
		} catch(Exception ex) {
			log.error("Error inserting ety " + entity , ex);
			throw new BusinessException("Error inserting ety " + entity , ex);
		}
		return output;
	}
	
	/**
	 * Insert All.
	 * 
	 * @param entity	etys
	 * @return			
	 */
	protected void insertAll(final List<T> entity) {
		try {
			mongoTemplate.insertAll(entity);
		} catch(Exception ex) {
			log.error("Error inserting all etys " + getClass() , ex);
			throw new BusinessException("Error inserting all etys " + getClass() , ex);
		}
	}

	/**
	 * Remove ety.
	 * 
	 * @param entity	entità da rimuovere
	 */
	protected void remove(final T entity) {
		mongoTemplate.remove(entity);
	}

	/**
	 * Find by id.
	 * 
	 * @param primaryKey	id
	 * @return				ety
	 */
	protected T findByID(final K primaryKey) {
		T output = null;
		try {
			ObjectId objId = constructObjectId(primaryKey.toString());
			output = mongoTemplate.findById(objId, getCls());
		} catch(Exception ex) {
			log.error("Error find by id ety " + getCls() , ex);
			throw new BusinessException("Error find by id ety " + getCls() , ex);
		}
		return output;
	}

	/**
	 * Find all etys.
	 * 
	 * @return	etys
	 */
	protected List<T> findAll() {
		return mongoTemplate.findAll(getCls());
	}

	private Class<T> getCls() {
		ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
		return ((Class<T>) pt.getActualTypeArguments()[0]);
		
	}
	
	private ObjectId constructObjectId(final String id) {
		ObjectId objectId = null;
		try {
			objectId = new ObjectId(id);
		} catch(Exception ex) {
			throw new BusinessException("Error during construct object id. :" , ex);
		}
		return objectId;
	}
	 
}
