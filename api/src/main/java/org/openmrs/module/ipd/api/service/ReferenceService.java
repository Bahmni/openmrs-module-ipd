/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.service;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface ReferenceService extends OpenmrsService {

	//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Optional<Reference> getReferenceByTypeAndTargetUUID(String type, String targetUuid) throws APIException;

	//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Reference saveReference(Reference reference) throws APIException;
}
