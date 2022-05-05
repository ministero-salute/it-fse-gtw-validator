package it.finanze.sanita.fse2.ms.gtw.validator.config;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Gateway Validator", version = "v1"))
public class OpenApiCFG {


  public OpenApiCFG() {
  }

  @Bean
	public OpenApiCustomiser openApiCustomiser() {
		return openApi -> openApi.getComponents().getSchemas().values().forEach( s -> s.setAdditionalProperties(false));
	}
	
	@Bean
	public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
		return openApi -> {
			openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
				ApiResponses apiResponses = operation.getResponses();
				
				Schema<Object> errorResponseSchema = new Schema<>();
				errorResponseSchema.setName("Error");
				errorResponseSchema.set$ref("#/components/schemas/ErrorResponseDTO");
				MediaType media =new MediaType();
				media.schema(errorResponseSchema);
				ApiResponse apiResponse = new ApiResponse().description("default")
				        .content(new Content()
	                                .addMediaType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE, media));
				apiResponses.addApiResponse("default", apiResponse);
			}));
		};
	}
}