package org.openmrs.module.ipd.api.translators;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Slot;
import org.springframework.stereotype.Component;

@Component
public class MedicationAdministrationToSlotStatusTranslator {

    public Slot.SlotStatus toSlotStatus(MedicationAdministration.MedicationAdministrationStatus medicationAdministrationStatus){
        if (medicationAdministrationStatus.equals(MedicationAdministration.MedicationAdministrationStatus.COMPLETED)){
            return Slot.SlotStatus.COMPLETED;
        }
        if (medicationAdministrationStatus.equals(MedicationAdministration.MedicationAdministrationStatus.NOTDONE)){
            return Slot.SlotStatus.NOT_DONE;
        }
        if (medicationAdministrationStatus.equals(MedicationAdministration.MedicationAdministrationStatus.STOPPED)){
            return Slot.SlotStatus.STOPPED;
        }
      return null;
    }
}
