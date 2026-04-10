/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.service;


import org.openmrs.module.ipd.api.model.IPDPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;

public interface IPDWardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid, String providerUuid);

    IPDPatientDetails getIPDPatientByWard(String wardUuid, Integer offset, Integer limit, String sortBy);

    IPDPatientDetails searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue, Integer offset, Integer limit, String sortBy);

    IPDPatientDetails getIPDPatientsByWardAndProvider(String wardUuid, String providerUuid, Integer offset, Integer limit, String sortBy);
}
