package org.bahmni.module.ipd.dao;

import org.bahmni.module.ipd.model.Schedule;
import org.openmrs.api.db.DAOException;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleDAO {
	
	Schedule getSchedule(Integer scheduleId) throws DAOException;
	
	Schedule saveSchedule(Schedule schedule) throws DAOException;

	void purgeSchedule(Schedule schedule) throws DAOException;
}
