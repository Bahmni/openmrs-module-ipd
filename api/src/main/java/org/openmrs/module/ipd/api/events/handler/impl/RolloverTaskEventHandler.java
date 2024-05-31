package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.dao.TaskRequestedPeriodDao;
import org.openmrs.module.fhirExtension.model.FhirTaskRequestedPeriod;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.model.TaskSearchRequest;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.ipd.api.events.ConfigLoader;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.ConfigDetail;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.TaskDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolloverTaskEventHandler implements IPDEventHandler {

    @Autowired
    ConfigLoader configLoader;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRequestedPeriodDao taskRequestedPeriodDao;
    private static final String TASK_STATUS = "REQUESTED";

    @Override
    public void handleEvent(IPDEvent event) {
        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getType().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);

        List<String> taskNames = eventConfig.getTasks().stream()
                .map(TaskDetail::getName)
                .collect(Collectors.toList());
        List<FhirTask.TaskStatus> taskStatuses= Arrays.asList(FhirTask.TaskStatus.REQUESTED);
        List<Task> rolloverTasks = taskService.searchTasks(new TaskSearchRequest(taskNames,taskStatuses));
        List<FhirTaskRequestedPeriod> fhirTaskRequestedPeriods = new ArrayList<FhirTaskRequestedPeriod>();
        for (Task task : rolloverTasks) {
            if (task.getFhirTaskRequestedPeriod() != null) {
                FhirTaskRequestedPeriod fhirTaskRequestedPeriod = task.getFhirTaskRequestedPeriod();
                fhirTaskRequestedPeriod.setRequestedStartTime(new Date());
                fhirTaskRequestedPeriods.add(fhirTaskRequestedPeriod);
            }
        }
        taskRequestedPeriodDao.update(fhirTaskRequestedPeriods);
    }
}