package org.openmrs.module.ipd.api.dao.impl;

import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.model.Schedule;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateScheduleDAO implements ScheduleDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateScheduleDAO.class);
	private final SessionFactory sessionFactory;

	@Autowired
	public HibernateScheduleDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Schedule getSchedule(Integer scheduleId) throws DAOException {
		return sessionFactory.getCurrentSession().get(Schedule.class, scheduleId);
	}
	
	@Override
	public Schedule saveSchedule(Schedule schedule) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(schedule);
		return schedule;
	}
}
