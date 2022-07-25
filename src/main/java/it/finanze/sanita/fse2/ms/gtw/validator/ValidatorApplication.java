package it.finanze.sanita.fse2.ms.gtw.validator;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ValidatorApplication {

	@Autowired
    private ServletWebServerApplicationContext server;
	
	public static void main(String[] args) {
		SpringApplication.run(ValidatorApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
	
	@Bean 
    @Qualifier("baseUrl")
	public String baseUrlServer(){
		String out = "";
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			Integer port = server.getWebServer().getPort();
			out = String.format("%s:%d", ip, port);
		} catch (Exception ex) {
			log.error("Error wile retrieve base url server : " , ex);
			throw new BusinessException("Error wile retrieve base url server : " , ex);
		}
		return out;
	}
	
}
