package org.openmrs.module.ipd.postprocessor;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IPDTransactionHandler implements EncounterTransactionHandler {

    @Autowired
    IPDScheduleService ipdScheduleService;

    @Override
    public void forRead(Encounter encounter, EncounterTransaction encounterTransaction) {
        // No Implementation needed as of now
    }

    @Override
    public void forSave(Encounter encounter, EncounterTransaction encounterTransaction) {
        ipdScheduleService.handlePostProcessEncounterTransaction(encounter,encounterTransaction);
    }
}
