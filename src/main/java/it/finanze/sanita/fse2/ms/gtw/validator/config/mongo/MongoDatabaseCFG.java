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
package it.finanze.sanita.fse2.ms.gtw.validator.config.mongo;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;

import it.finanze.sanita.fse2.ms.gtw.validator.config.AzureCfg;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;


/**
 * Factory to create database instances
 */
@Configuration
public class MongoDatabaseCFG {

	@Autowired
	private MongoPropertiesCFG props;
	
	@Autowired
	private AzureCfg azureCfg;

    /**
     * Creates a new factory instance with the given connection string (properties.yml)
     * @return The new {@link SimpleMongoClientDatabaseFactory} instance
     */ 
    @Bean
	public MongoDatabaseFactory createFactory(MongoPropertiesCFG props){
    	String mongoUri = props.getUri();
    	if(!StringUtility.isNullOrEmpty(azureCfg.getTenantId())) {
    		SecretClient secretClient = getCosmosSecretClientFromKeyVault();
    		Map<String,String> credential = getSecret(secretClient);
    		String user = credential.keySet().iterator().next();
    		String pwd = credential.values().iterator().next();
    		mongoUri = String.format(props.getUri(),user, pwd);
    	}
		ConnectionString connectionString = new ConnectionString(mongoUri);
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();
		return new SimpleMongoClientDatabaseFactory(MongoClients.create(mongoClientSettings), props.getSchemaName());
	}

    /**
     * Creates a new template instance used to perform operations on the schema
     * @return The new {@link MongoTemplate} instance
     */
    @Bean
    @Primary
    public MongoTemplate createTemplate(ApplicationContext appContext) {
        MongoDatabaseFactory factory = createFactory(props);
        final MongoMappingContext mongoMappingContext = new MongoMappingContext();
        mongoMappingContext.setApplicationContext(appContext);
        MappingMongoConverter converter = new MappingMongoConverter(
            new DefaultDbRefResolver(factory),
                mongoMappingContext
        );
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }
    
    private SecretClient getCosmosSecretClientFromKeyVault() {
		TokenCredential credential = new ClientCertificateCredentialBuilder()
				.clientId(azureCfg.getClientId()).pfxCertificate(azureCfg.getKeyVaultCertificatePath(), azureCfg.getKeyVaultCertificatePass()).
				tenantId(azureCfg.getTenantId()) 
				.build();

		return new SecretClientBuilder()
				.vaultUrl(azureCfg.getKeyVaultEndpoint())
				.credential(credential)
				.buildClient();
	}
    
    private Map<String,String> getSecret(SecretClient secretClient){
		Map<String,String> out = new HashMap<>();
		KeyVaultSecret secretUser = secretClient.getSecret(azureCfg.getSecretUser());
		KeyVaultSecret secretPass = secretClient.getSecret(azureCfg.getSecretPass());
		out.put(secretUser.getValue(), secretPass.getValue());
		return out;
	}
}