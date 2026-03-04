/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.service;

import org.openmrs.Visit;
import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.web.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.web.model.PatientMedicationSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IPDScheduleService {
    Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, LocalDate forDate);
    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType);
    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, List<String> orderUuids);
    Schedule updateMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
    List<Slot> getMedicationSlotsForTheGivenTimeFrame(String patientUuid, LocalDateTime localStartDate, LocalDateTime localEndDate,Boolean considerAdministeredTime, Visit visit);
    void handlePostProcessEncounterTransaction(Encounter encounter, EncounterTransaction encounterTransaction);
    List<PatientMedicationSummary> getSlotsForPatientListByTime(List<String> patientUuidList, LocalDateTime localStartDate,
                                                                LocalDateTime localEndDate, Boolean includePreviousSlot, Boolean includeSlotDuration);
}
