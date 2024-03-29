package org.openmrs.module.ipd.postprocessor;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.openmrs.module.ipd.events.model.IPDEvent;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.ipd.events.model.IPDEventType;
import org.openmrs.module.ipd.events.IPDEventManager;

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
        IPDEvent ipdEvent = new IPDEvent(encounter.getUuid(), encounter.getPatient().getUuid(), eventType);
        eventManager.processEvent(ipdEvent);
        ipdScheduleService.handlePostProcessEncounterTransaction(encounter,encounterTransaction);
    }
}
