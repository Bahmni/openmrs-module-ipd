package org.openmrs.module.ipd.service.impl;

import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.mapper.ScheduleMapperService;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.service.ScheduleMedicationService;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleMedicationServiceImpl implements ScheduleMedicationService {

    private final ScheduleService scheduleService;
    private final ScheduleMapperService scheduleMapperService;

    @Autowired
    public ScheduleMedicationServiceImpl(ScheduleService scheduleService, ScheduleMapperService scheduleMapperService) {
        this.scheduleService = scheduleService;
        this.scheduleMapperService = scheduleMapperService;
    }

    @Override
    public Schedule createSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {

        Schedule schedule = scheduleMapperService.mapScheduleMedicationRequestToSchedule(scheduleMedicationRequest);
        return scheduleService.saveSchedule(schedule);
    }
}