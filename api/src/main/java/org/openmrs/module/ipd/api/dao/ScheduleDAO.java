package org.openmrs.module.ipd.api.dao;

import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.api.db.DAOException;
import org.springframework.stereotype.Repository;
import org.openmrs.Concept;
import org.openmrs.module.ipd.api.model.Reference;

import java.util.List;

@Repository
public interface ScheduleDAO {
	
	Schedule getSchedule(Integer scheduleId) throws DAOException;
	
	Schedule saveSchedule(Schedule schedule) throws DAOException;

	List<Schedule> getSchedulesBySubjectReferenceIdAndServiceType(Reference subject, Concept serviceType);
}
