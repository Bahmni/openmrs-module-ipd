package org.openmrs.module.ipd.api.dao;

import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.api.db.DAOException;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleDAO {
	
	Schedule getSchedule(Integer scheduleId) throws DAOException;
	
	Schedule saveSchedule(Schedule schedule) throws DAOException;
}
