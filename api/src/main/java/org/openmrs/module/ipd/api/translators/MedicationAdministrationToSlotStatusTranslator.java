/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

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
