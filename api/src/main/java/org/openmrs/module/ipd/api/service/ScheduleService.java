package org.openmrs.module.ipd.api.service;

import org.openmrs.Concept;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScheduleService extends OpenmrsService {
	
//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Schedule getSchedule(Integer scheduleId) throws APIException;
	
//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Schedule saveSchedule(Schedule schedule) throws APIException;

	List<Schedule> getSchedulesBySubjectReferenceIdAndServiceType(Reference subject, Concept serviceType) throws APIException;
	List<Schedule> getSchedulesBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType, List<String> orderUuids) throws APIException;
}
