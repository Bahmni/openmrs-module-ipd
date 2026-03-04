/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.fhirExtension.web.mapper.TaskMapper;
import org.openmrs.module.ipd.api.events.ConfigLoader;
import org.openmrs.module.ipd.api.events.IPDEventUtils;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.ConfigDetail;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.TaskDetail;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShiftStartTaskEventHandler implements IPDEventHandler {

    @Autowired
    ConfigLoader configLoader;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private WardService wardService;

    @Override
    public void handleEvent(IPDEvent event) {
        List<AdmittedPatient> admittedPatients = wardService.getAdmittedPatients();
        ConfigDetail eventConfig = getEventConfig(event);
        List<Task> tasks = new ArrayList<Task>();
        for(AdmittedPatient admittedPatient: admittedPatients){
            String patientUuid = admittedPatient.getBedPatientAssignment().getPatient().getUuid();
            IPDEvent ipdEvent = new IPDEvent(null, patientUuid, event.getIpdEventType());
            for(TaskDetail taskDetail : eventConfig.getTasks()) {
                TaskRequest taskRequest = IPDEventUtils.createNonMedicationTaskRequest(ipdEvent, taskDetail.getName(), taskDetail.getType(), true);
                Task task = taskMapper.fromRequest(taskRequest);
                tasks.add(task);
            }
        }
        if(tasks.size() > 0){
            taskService.saveTask(tasks);
        }
    }

    private ConfigDetail getEventConfig(IPDEvent event){
        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getEvent().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);
        return eventConfig;
    }
}
