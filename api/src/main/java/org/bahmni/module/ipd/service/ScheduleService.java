package org.bahmni.module.ipd.service;

import org.bahmni.module.ipd.model.Schedule;
import org.bahmni.module.ipd.util.PrivilegeConstants;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

@Service
public interface ScheduleService extends OpenmrsService {
	
//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Schedule getSchedule(Integer scheduleId) throws APIException;
	
//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Schedule saveSchedule(Schedule schedule) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	void purgeSchedule(Schedule schedule) throws APIException;
}
