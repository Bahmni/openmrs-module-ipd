/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.Date;
import java.util.List;

public interface WardDAO {

    List<AdmittedPatient> searchAdmittedPatients(Location location, List<String> searchKeys, String searchValue, String sortBy);

    List<AdmittedPatient> getAdmittedPatients(Location location, Provider provider, Date currentDateTime, String sortBy);

    WardPatientsSummary getWardPatientSummary(Location location, Provider provider, Date currentDateTime);

}
