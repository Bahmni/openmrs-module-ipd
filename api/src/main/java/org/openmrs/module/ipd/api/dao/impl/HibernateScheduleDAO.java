package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Concept;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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

	@Override
	public List<Schedule> getSchedulesBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType) throws DAOException {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Schedule schedule WHERE schedule.subject = :subject AND schedule.serviceType = :serviceType");


		query.setParameter("subject", subject);
		query.setParameter("serviceType", serviceType);
		return query.getResultList();
	}

	@Override
	public List<Schedule> getSchedulesBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType, List<String> orderUuids) throws DAOException {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Schedule schedule WHERE schedule.subject = :subject AND schedule.serviceType = :serviceType AND schedule.order.uuid IN :orderUuids");

		query.setParameter("subject", subject);
		query.setParameter("serviceType", serviceType);
		query.setParameter("orderUuids", orderUuids);

		return query.getResultList();
	}
}
