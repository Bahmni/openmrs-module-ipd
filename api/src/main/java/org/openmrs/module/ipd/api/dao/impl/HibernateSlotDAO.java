package org.openmrs.module.ipd.api.dao.impl;

import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Slot;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HibernateSlotDAO implements SlotDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateSlotDAO.class);
	
	private final SessionFactory sessionFactory;

	@Autowired
	public HibernateSlotDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Slot getSlot(Integer slotId) throws DAOException {
		return sessionFactory.getCurrentSession().get(Slot.class, slotId);
	}
	
	@Override
	public Slot saveSlot(Slot slot) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(slot);
		return slot;
	}

	@Override
	public void purgeSlot(Slot slot) throws DAOException {
		sessionFactory.getCurrentSession().delete(slot);
	}
}
