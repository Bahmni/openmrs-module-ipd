package org.openmrs.module.ipd.events.handler.impl;

import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.ActiveEncounterParameters;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.fhirExtension.web.mapper.TaskMapper;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.service.WardService;
import org.openmrs.module.ipd.events.ConfigLoader;
import org.openmrs.module.ipd.events.IPDEventUtils;
import org.openmrs.module.ipd.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.events.model.ConfigDetail;
import org.openmrs.module.ipd.events.model.IPDEvent;
import org.openmrs.module.ipd.events.model.TaskDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShiftStartTaskEventHandler implements IPDEventHandler {

    private static final String CONSULTATION_ENCOUNTER_TYPE = "Consultation";
    @Autowired
    ConfigLoader configLoader;
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void handleEvent(IPDEvent event) {

        WardService wardService = Context.getService(WardService.class);
        // - getAdmit - wardDAO.getAdmittedPatients
        List<AdmittedPatient> admittedPatients = wardService.getAdmittedPatients();

//        encounterService.setEncounterType();

        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getType().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);

        List<Task> tasks = new ArrayList<Task>();
        for(AdmittedPatient admittedPatient: admittedPatients){
            ActiveEncounterParameters activeEncounterParameters = new ActiveEncounterParameters();
            String patientUuid = admittedPatient.getBedPatientAssignment().getPatient().getUuid();
            activeEncounterParameters.setPatientUuid(patientUuid);
            activeEncounterParameters.setEncounterTypeUuid(encounterService.getEncounterType(CONSULTATION_ENCOUNTER_TYPE).getUuid());
            EncounterTransaction encounterTransaction = emrEncounterService.getActiveEncounter(activeEncounterParameters);
            IPDEvent ipdEvent = new IPDEvent(encounterTransaction.getEncounterUuid(), patientUuid, event.getIpdEventType());
            for(TaskDetail taskDetail : eventConfig.getTasks()) {
                TaskRequest taskRequest = IPDEventUtils.createNonMedicationTaskRequest(ipdEvent, taskDetail.getName(), "nursing_activity_system");
                Task task = taskMapper.fromRequest(taskRequest);
                tasks.add(task);
            }
        }

//        bulkSaveTasks(List<Task> tasks);
    }
}