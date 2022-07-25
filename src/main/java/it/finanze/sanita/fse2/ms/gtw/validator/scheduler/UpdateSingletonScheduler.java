package it.finanze.sanita.fse2.ms.gtw.validator.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpdateSingletonScheduler {

	@Autowired
	private IUpdateSingletonSRV updateSingletonSRV;
	
	@Autowired
	@Qualifier("baseUrl")
	private String baseUrl;
	
	/**
	 * Scheduler.
	 */
	@Scheduled(cron = "${scheduler.update-singleton.run}")   
	public void schedulingTask() {
		log.info("Update singleton scheduler - START");
		updateSingletonSRV.updateSingletonInstance(baseUrl);
		log.info("Update singleton scheduler - END");
	}
}
