package it.finanze.sanita.fse2.ms.gtw.validator;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.AuditRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.DictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.TerminologyRepo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq; 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given; 


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepositoryTest extends AbstractTest {

    public static final String TEST_TYPE_ID_EXTENSION = "1.3";
    public static final String TEST_ROOT_NAME_FILE = "CDA.xsd";
    public static final int TEST_FILES_SIZE = 10;

    @Autowired
    private ISchemaRepo repository; 
    
    @SpyBean
    private ISchematronRepo schematronRepository; 

    @Autowired
    private TerminologyRepo terminologyRepo; 
    
    @Autowired
    private DictionaryRepo dictionaryRepo; 
    
    @Autowired
    private AuditRepo auditRepo; 
    
    @SpyBean
    private MongoTemplate mongo;

    @BeforeAll
    void setup() {
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
    	List<SchemaETY> ety =repository.findByExtensionAndLastUpdateDate("1.3", new Date()); 
    	
    	assertEquals(ArrayList.class, ety.getClass()); 
    } 
    
    @Test
    void findBySystemAndVersionTest() {
    	SchematronETY ety = schematronRepository.findBySystemAndVersion("2.16.840.1.113883.2.9.10.1.11.1.2", "0.0"); 
    
    	assertEquals(ety.getClass(), SchematronETY.class);   	
    	assertEquals(ety.getId().getClass(), String.class); 
    	
    	assertEquals(ety.getTemplateIdRoot(), "2.16.840.1.113883.2.9.10.1.11.1.2"); 
    	
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
    
    @Test
    void findByFilenameTest() {         
         assertDoesNotThrow(() -> dictionaryRepo.findByFilename("test"));  
         
         when(mongo).thenThrow(new MongoException("Test")); 
         
         assertThrows(BusinessException.class, 
        		 () -> dictionaryRepo.findByFilename("test")); 
                 
     }
    
    @Test
    void saveExceptionTest() {                
         assertDoesNotThrow(() -> dictionaryRepo.findByFilename("test"));  

         when(mongo).thenThrow(new MongoException("Test")); 
         
         assertThrows(BusinessException.class, () -> auditRepo.save(null));  
               
     }
    

}
