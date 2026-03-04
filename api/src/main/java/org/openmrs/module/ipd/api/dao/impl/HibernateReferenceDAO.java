/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.ReferenceDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public class HibernateReferenceDAO implements ReferenceDAO {

	private static final Logger log = LoggerFactory.getLogger(HibernateReferenceDAO.class);

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Optional<Reference> getReferenceByTypeAndTargetUUID(String type, String targetUuid) throws DAOException {

		Query query = sessionFactory.getCurrentSession()
			.createQuery("FROM Reference ref WHERE ref.type=:type and ref.targetUuid=:targetUuid");

		query.setParameter("type", type);
		query.setParameter("targetUuid", targetUuid);

		return query.uniqueResultOptional();
	}

	@Override
	public Reference saveReference(Reference reference) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(reference);
		return reference;
	}
}
