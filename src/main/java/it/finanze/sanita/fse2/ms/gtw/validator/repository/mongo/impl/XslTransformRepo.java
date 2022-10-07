package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IXslTransformRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.XslTransformETY;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository used to retrieve xslt data to execute transformation of cda documents.
 * 
 * @author Simone Lungarella
 */
@Slf4j
@Repository
public class XslTransformRepo implements IXslTransformRepo {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public XslTransformETY getXsltByTemplateId(final String templateId) {
        
        XslTransformETY xslt = null;
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("template_id_root").is(templateId));
            query.with(Sort.by(Direction.DESC, "template_id_extension"));

            xslt = mongoTemplate.findOne(query, XslTransformETY.class);
        } catch (final Exception e) {
            log.error(String.format("Error while retrieving xslt with template id: %s", templateId), e);
            throw new BusinessException(String.format("Error while retrieving xslt with template id: %s", templateId), e);
        }
        return xslt;
    }
    
}
