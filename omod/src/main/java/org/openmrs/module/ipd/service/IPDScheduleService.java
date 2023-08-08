package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;

import java.time.LocalDate;
import java.util.List;

public interface IPDScheduleService {
    Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, LocalDate forDate);
    List<Schedule> getMedicationSchedules(String patientUuid, ServiceType serviceType);
}