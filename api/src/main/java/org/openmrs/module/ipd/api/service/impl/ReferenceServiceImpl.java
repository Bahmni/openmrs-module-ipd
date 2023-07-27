package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.impl.HibernateReferenceDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ReferenceServiceImpl extends BaseOpenmrsService implements ReferenceService {

	private static final Logger log = LoggerFactory.getLogger(ReferenceServiceImpl.class);

	private final HibernateReferenceDAO hibernateReferenceDAO;

	@Autowired
	public ReferenceServiceImpl(HibernateReferenceDAO hibernateReferenceDAO) {
		this.hibernateReferenceDAO = hibernateReferenceDAO;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Reference> getReferenceByTypeAndTargetUUID(String type, String targetUuid) throws APIException {
		return hibernateReferenceDAO.getReferenceByTypeAndTargetUUID(type, targetUuid);
	}

	@Override
	public Reference saveReference(Reference reference) throws APIException {
		return hibernateReferenceDAO.saveReference(reference);
	}
}
