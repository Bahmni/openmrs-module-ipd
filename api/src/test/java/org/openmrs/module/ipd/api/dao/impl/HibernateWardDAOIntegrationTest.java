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
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class HibernateWardDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WardDAO wardDAO;

    @Autowired
    private SessionFactory sessionFactory;


    @Test
    public void shouldGetAdmittedPatientsByLocation() {

        Location location= Context.getService(LocationService.class).getLocationByUuid("7779d653-393b-4118-9c83-a3715b82d4ac");
        List<AdmittedPatient> admittedPatients= wardDAO.getAdmittedPatients(location, null, null, null);

        Assertions.assertEquals(0, admittedPatients.size());

    }
}
