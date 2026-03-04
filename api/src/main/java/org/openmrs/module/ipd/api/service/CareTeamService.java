/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.service;

import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.Schedule;
import org.springframework.stereotype.Service;
import org.openmrs.api.OpenmrsService;



public interface CareTeamService extends OpenmrsService {

    CareTeam saveCareTeam(CareTeam careTeam) throws APIException;

    CareTeam getCareTeamByVisit(Visit visit) throws APIException;

}
