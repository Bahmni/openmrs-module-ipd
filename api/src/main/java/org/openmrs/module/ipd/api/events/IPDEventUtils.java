package org.openmrs.module.ipd.api.events;

import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.ipd.api.events.model.IPDEvent;

import java.util.Date;

public class IPDEventUtils {

    public static TaskRequest createNonMedicationTaskRequest(IPDEvent ipdEvent, String name, String taskType, Boolean isSystemGenerated) {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setName(name);
        taskRequest.setTaskType(taskType);
        taskRequest.setEncounterUuid(ipdEvent.getEncounterUuid());
        taskRequest.setPatientUuid(ipdEvent.getPatientUuid());
        taskRequest.setRequestedStartTime(new Date());
        taskRequest.setIntent(FhirTask.TaskIntent.ORDER);
        taskRequest.setStatus(FhirTask.TaskStatus.REQUESTED);
        taskRequest.setIsSystemGeneratedTask(isSystemGenerated);
        return taskRequest;
    }
}
