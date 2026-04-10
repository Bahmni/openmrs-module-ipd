/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.postprocessor;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.api.events.IPDEventManager;

@Component
public class IPDTransactionHandler implements EncounterTransactionHandler {

    @Autowired
    IPDScheduleService ipdScheduleService;

    @Autowired
    IPDEventManager eventManager;

    @Override
    public void forRead(Encounter encounter, EncounterTransaction encounterTransaction) {
        // No Implementation needed as of now
    }

    @Override
    public void forSave(Encounter encounter, EncounterTransaction encounterTransaction) {
        IPDEventType eventType = eventManager.getEventTypeForEncounter(encounter.getEncounterType().getName());
        if (eventType != null) {
            IPDEvent ipdEvent = new IPDEvent(encounter.getUuid(), encounter.getPatient().getUuid(), eventType);
            eventManager.processEvent(ipdEvent);
        }
        ipdScheduleService.handlePostProcessEncounterTransaction(encounter,encounterTransaction);
    }
}
