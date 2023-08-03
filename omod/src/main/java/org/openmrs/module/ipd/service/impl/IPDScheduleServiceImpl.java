package org.openmrs.module.ipd.service.impl;

import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.factory.ScheduleFactory;
import org.openmrs.module.ipd.factory.SlotFactory;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IPDScheduleServiceImpl implements IPDScheduleService {

    private final ScheduleService scheduleService;
    private final ScheduleFactory scheduleFactory;
    private final SlotFactory slotFactory;
    private final SlotService slotService;
    private final SlotTimeCreationService slotTimeCreationService;

    @Autowired
    public IPDScheduleServiceImpl(ScheduleService scheduleService, ScheduleFactory scheduleFactory, SlotFactory slotFactory, SlotService slotService, SlotTimeCreationService slotTimeCreationService) {
        this.scheduleService = scheduleService;
        this.scheduleFactory = scheduleFactory;
        this.slotFactory = slotFactory;
        this.slotService = slotService;
        this.slotTimeCreationService = slotTimeCreationService;
    }

    @Override
    public Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {

        Schedule schedule = scheduleFactory.createScheduleForMedicationFrom(scheduleMedicationRequest);
        Schedule savedSchedule = scheduleService.saveSchedule(schedule);
        List<LocalDateTime> slotsStartTime = slotTimeCreationService.createSlotsStartTimeFrom(scheduleMedicationRequest, savedSchedule);
        slotFactory.createSlotsForMedicationFrom(savedSchedule, slotsStartTime)
                .forEach(slotService::saveSlot);

        return savedSchedule;
    }
}