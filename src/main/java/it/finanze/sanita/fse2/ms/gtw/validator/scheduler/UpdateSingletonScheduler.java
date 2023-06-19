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
package it.finanze.sanita.fse2.ms.gtw.validator.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpdateSingletonScheduler {

	@Autowired
	private IUpdateSingletonSRV updateSingletonSRV;
	 
	
	/**
	 * Scheduler.
	 */
	@Scheduled(cron = "${scheduler.update-singleton.run}")   
	public void schedulingTask() {
		log.debug("Update singleton scheduler - START");
		updateSingletonSRV.updateSingletonInstance();
		log.debug("Update singleton scheduler - END");
	}
}
