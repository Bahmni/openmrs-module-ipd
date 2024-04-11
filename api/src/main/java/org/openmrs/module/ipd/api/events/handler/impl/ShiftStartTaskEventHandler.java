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
            String visituuid = admittedPatient.getBedPatientAssignment().getEncounter().getVisit().getUuid();

            IPDEvent ipdEvent = new IPDEvent(null, patientUuid, visituuid, event.getIpdEventType());
            for(TaskDetail taskDetail : eventConfig.getTasks()) {
                TaskRequest taskRequest = IPDEventUtils.createNonMedicationTaskRequest(ipdEvent, taskDetail.getName(), "nursing_activity_system");
                Task task = taskMapper.fromRequest(taskRequest);
                tasks.add(task);
            }
        }
        if(tasks.size() > 0){
            taskService.bulkSaveTasks(tasks);
        }
    }

    private ConfigDetail getEventConfig(IPDEvent event){
        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getType().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);
        return eventConfig;
    }


}