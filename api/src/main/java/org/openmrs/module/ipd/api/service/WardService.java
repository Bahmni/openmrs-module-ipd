/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;


public interface WardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid, String providerUuid);

    List<AdmittedPatient> getWardPatientsByUuid(String wardUuid, String sortBy);

    List<AdmittedPatient>  searchWardPatients(String wardUuid, List<String> searchKeys, String searchValue, String sortBy);

    List<AdmittedPatient> getPatientsByWardAndProvider(String wardUuid, String providerUuid, String sortBy);

    List<AdmittedPatient> getAdmittedPatients();
}
