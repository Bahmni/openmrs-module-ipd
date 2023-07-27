package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.api.model.Schedule;

public interface ScheduleMedicationService {
    Schedule createSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
}
