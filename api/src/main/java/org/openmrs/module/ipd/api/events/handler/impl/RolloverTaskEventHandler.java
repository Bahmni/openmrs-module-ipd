package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.ipd.api.events.ConfigLoader;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.ConfigDetail;
import org.openmrs.module.ipd.api.events.model.TaskDetail;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.fhirExtension.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolloverTaskEventHandler implements IPDEventHandler {

    @Autowired
    ConfigLoader configLoader;

    private TaskService taskService;

    private static final String TASK_STATUS = "REQUESTED";

    @Override
    public void handleEvent(IPDEvent event) {
        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getType().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);
        System.out.println("eventConfig type RolloverTaskEventHandler " + eventConfig.getType());
        System.out.println("eventConfig tasks RolloverTaskEventHandler " + eventConfig.getTasks() + " size --- " + eventConfig.getTasks().size());

        List<String> taskNames = eventConfig.getTasks().stream()
                .map(TaskDetail::getName)
                .collect(Collectors.toList());

        List<Task> rolloverTasks = taskService.getTasksByNameAndStatus(taskNames, TASK_STATUS);

        for (Task task : rolloverTasks) {
            System.out.println("Task : " + task.getFhirTask());

        }
    }
}