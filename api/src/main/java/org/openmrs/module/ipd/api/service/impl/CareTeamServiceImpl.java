/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.service.impl;


import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.CareTeamDAO;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.service.CareTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CareTeamServiceImpl extends BaseOpenmrsService implements CareTeamService {

    private static final Logger log = LoggerFactory.getLogger(CareTeamServiceImpl.class);

    private CareTeamDAO careTeamDAO;

    public void setCareTeamDAO(CareTeamDAO careTeamDAO) {
        this.careTeamDAO = careTeamDAO;
    }

    @Override
    public CareTeam saveCareTeam(CareTeam careTeam) throws APIException {
        return careTeamDAO.saveCareTeam(careTeam);
    }

    @Override
    public CareTeam getCareTeamByVisit(Visit visit) throws APIException {
        return careTeamDAO.getCareTeamByVisit(visit);
    }
}
