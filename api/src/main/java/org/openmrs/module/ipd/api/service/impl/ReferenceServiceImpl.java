/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.ReferenceDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public class ReferenceServiceImpl extends BaseOpenmrsService implements ReferenceService {

	private static final Logger log = LoggerFactory.getLogger(ReferenceServiceImpl.class);

	private ReferenceDAO referenceDAO;

	public void setReferenceDAO(ReferenceDAO referenceDAO) {
		this.referenceDAO = referenceDAO;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Reference> getReferenceByTypeAndTargetUUID(String type, String targetUuid) throws APIException {
		return referenceDAO.getReferenceByTypeAndTargetUUID(type, targetUuid);
	}

	@Override
	public Reference saveReference(Reference reference) throws APIException {
		return referenceDAO.saveReference(reference);
	}
}
