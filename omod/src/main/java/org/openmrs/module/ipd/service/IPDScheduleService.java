package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;

public interface IPDScheduleService {
    Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
}