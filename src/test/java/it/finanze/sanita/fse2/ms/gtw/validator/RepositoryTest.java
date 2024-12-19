
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.mongodb.MongoException;

import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.TerminologyRepo;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryTest extends AbstractTest {

    public static final String TEST_TYPE_ID_EXTENSION = "1.3";
    public static final String TEST_ROOT_NAME_FILE = "CDA.xsd";
    public static final String TEST_ID_DELETED = "test_del";
    public static final String TEST_NAME_SCHEMA_DELETED = "CDAdel.xsd";
    public static final String TEST_TEMPLATE_ID_ROOT_DELETED = "2.16.840.1.113883.2.9.10.1.11.2.5";
    public static final String TEST_NAME_SCHEMATRON_DELETED = "schematron_del";
    public static final String TEST_TYPE_ID_EXTENSION_DELETED = "1.4";
    public static final int TEST_FILES_SIZE = 10;

    @Autowired
    private ISchemaRepo repository; 
    
    @SpyBean
    private ISchematronRepo schematronRepository; 

    @Autowired
    private TerminologyRepo terminologyRepo;
    
    @SpyBean
    private MongoTemplate mongo;

    @MockBean
    private ConfigSRV config;

    @BeforeAll
    void setup() {
        when(config.isAuditEnable()).thenReturn(true);
        clearConfigurationItems();
        insertSchema();
        insertSchematron(); 
    }

    @Test
    void findFatherXsdTest() {
        // Retrieve
        SchemaETY res = repository.findFatherXsd(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertTrue(res.getRootSchema());
        assertEquals(TEST_ROOT_NAME_FILE, res.getNameSchema());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findFatherXsd(TEST_TYPE_ID_EXTENSION));
    } 
    
    @Test
    void findFatherXsdDeletedElement() {
    	SchemaETY ety = new SchemaETY(); 
    	ety.setId(TEST_ID_DELETED);
    	ety.setNameSchema(TEST_NAME_SCHEMA_DELETED); 
    	ety.setRootSchema(true); 
    	ety.setTypeIdExtension(TEST_TYPE_ID_EXTENSION_DELETED); 
    	ety.setDeleted(true); 
    	
    	mongo.insert(ety, Constants.Profile.TEST_PREFIX + "schema");
    	
    	
        SchemaETY res = repository.findFatherXsd(TEST_TYPE_ID_EXTENSION_DELETED);

        assertNull(res); 
    }  

    @Test
    void findChildrenXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findChildrenXsd(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE - 1, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findChildrenXsd(TEST_TYPE_ID_EXTENSION));
    }

    @Test
    void findFatherLastVersionXsdTest() {
        // Retrieve
        SchemaETY res = repository.findFatherLastVersionXsd();
        // Assertions
        assertTrue(res.getRootSchema());
        assertEquals(TEST_ROOT_NAME_FILE, res.getNameSchema());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findFatherLastVersionXsd());
    }

    @Test
    void findByVersionXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findByVersion(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findByVersion(TEST_TYPE_ID_EXTENSION)); 
        
    }
    
    @Test
    void findByExtensionAndLastUpdateTest() {
    	List<SchemaETY> ety = repository.findByExtensionAndLastUpdateDate("1.3", new Date()); 
    	
    	assertEquals(ArrayList.class, ety.getClass()); 
    } 
    
    @Test
    void findBySystemAndVersionTest() {
    	SchematronETY ety = schematronRepository.findGreaterOne("2.16.840.1.113883.2.9.10.1.11.1.2", null, "0.0");
    
    	assertEquals(SchematronETY.class, ety.getClass());   	
    	assertEquals(String.class, ety.getId().getClass()); 
    	
    	assertEquals("2.16.840.1.113883.2.9.10.1.11.1.2", ety.getTemplateIdRoot());
    	
    } 
    
    @Test
    void findBySystemAndVersionDeletedTest() {
    	SchematronETY ety = new SchematronETY(); 
    	ety.setNameSchematron(TEST_NAME_SCHEMATRON_DELETED); 
    	ety.setTemplateIdRoot(TEST_TEMPLATE_ID_ROOT_DELETED); 
    	ety.setVersion(TEST_TYPE_ID_EXTENSION_DELETED); 
    	ety.setDeleted(true); 
    	
    	mongo.insert(ety, Constants.Profile.TEST_PREFIX + "schematron");
    	
    	
    	SchematronETY res = schematronRepository.findGreaterOne(TEST_TEMPLATE_ID_ROOT_DELETED, TEST_TYPE_ID_EXTENSION_DELETED, null);
    	
    	assertNull(res); 
    	
    }
    
    @Test
    void existsBySystemAndCodeTest() {         
         assertFalse(terminologyRepo.existBySystemAndCode("2.16.840.1.113883.2.9.10.1.11.1.2", "0.0")); 
         
         when(mongo).thenThrow(new MongoException("Test")); 
         
         assertThrows(BusinessException.class, 
        		 () -> terminologyRepo.existBySystemAndCode("2.16.840.1.113883.2.9.10.1.11.1.2", "0.0")); 
         
         assertThrows(BusinessException.class, 
        		 () -> terminologyRepo.allCodesExists("test", null)); 
         
         assertThrows(BusinessException.class, 
        		 () -> terminologyRepo.findAllCodesExists("test", null)); 
         

     }   

}
