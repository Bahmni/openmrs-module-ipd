/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.ipd.dao.impl;

import org.bahmni.module.ipd.dao.SlotDAO;
import org.bahmni.module.ipd.model.Slot;
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
