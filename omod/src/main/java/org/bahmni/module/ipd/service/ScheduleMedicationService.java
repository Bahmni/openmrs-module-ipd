package org.bahmni.module.ipd.service;

import org.bahmni.module.ipd.contract.ScheduleMedicationRequest;
import org.bahmni.module.ipd.model.Schedule;

public interface ScheduleMedicationService {
    Schedule createSchedule(ScheduleMedicationRequest scheduleMedicationRequest);
}
