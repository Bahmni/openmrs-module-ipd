/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.service;

import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;

import java.util.Date;
import java.util.List;

public interface IPDVisitService {

    List<IPDDrugOrder> getPrescribedOrders(String visitUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);
    List<Slot> getMedicationSlots(String visitUuid, ServiceType serviceType);
}
