package org.openmrs.module.ipd.fhir.task;

import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationAcknowledgementRequest;
import org.openmrs.module.ipd.web.service.AmendmentConflictException;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Routes a FHIR {@code Task} whose focus is a {@code MedicationAdministration} through the existing
 * {@link IPDMedicationAdministrationService#acknowledge} rules (per-administration lock, latest note,
 * approver = logged-in provider, task type from global property) and returns the FHIR Task describing
 * the acknowledgement that was stored.
 */
@Component
public class MedicationAdministrationAcknowledgementTaskHandler {

    static final String MEDICATION_ADMINISTRATION = "MedicationAdministration";

    @Autowired
    private IPDMedicationAdministrationService ipdMedicationAdministrationService;

    // for tests
    void setIpdMedicationAdministrationService(IPDMedicationAdministrationService service) {
        this.ipdMedicationAdministrationService = service;
    }

    public boolean isAcknowledgement(Task task) {
        return task != null && task.hasFocus() && task.getFocus().hasReference()
                && MEDICATION_ADMINISTRATION.equals(task.getFocus().getReferenceElement().getResourceType());
    }

    public Task acknowledge(Task task) {
        if (!hasPrivilege(PrivilegeConstants.APPROVE_AMEND_NOTE)) {
            throw new ForbiddenOperationException("User doesn't have the following privilege: " + PrivilegeConstants.APPROVE_AMEND_NOTE);
        }
        if (task.getStatus() != Task.TaskStatus.COMPLETED) {
            throw new InvalidRequestException("An acknowledgement Task must have status=completed");
        }
        if (task.getIntent() != Task.TaskIntent.ORDER) {
            throw new InvalidRequestException("An acknowledgement Task must have intent=order");
        }
        String maUuid = StringUtils.trimToNull(task.getFocus().getReferenceElement().getIdPart());
        if (maUuid == null) {
            throw new InvalidRequestException("Task.focus must be MedicationAdministration/<uuid>");
        }
        String remarks = task.hasNote() ? task.getNoteFirstRep().getText() : null;

        org.openmrs.module.fhirExtension.model.Task stored;
        try {
            stored = ipdMedicationAdministrationService.acknowledge(maUuid,
                    MedicationAdministrationAcknowledgementRequest.builder().remarks(remarks).build());
        }
        catch (AmendmentConflictException e) {
            throw new ResourceVersionConflictException(e.getMessage());
        }
        catch (APIException e) {
            throw new InvalidRequestException(e.getMessage());
        }
        return toFhirTask(stored, maUuid, remarks);
    }

    boolean hasPrivilege(String privilege) {
        return Context.getUserContext().hasPrivilege(privilege);
    }

    private Task toFhirTask(org.openmrs.module.fhirExtension.model.Task stored, String maUuid, String remarks) {
        org.openmrs.module.fhir2.model.FhirTask fhirTask = stored.getFhirTask();
        Task result = new Task();
        result.setId(fhirTask.getUuid());
        result.setStatus(Task.TaskStatus.COMPLETED);
        result.setIntent(Task.TaskIntent.ORDER);
        result.setFocus(new Reference("MedicationAdministration/" + maUuid));
        if (fhirTask.getForReference() != null && fhirTask.getForReference().getTargetUuid() != null) {
            result.setFor(new Reference("Patient/" + fhirTask.getForReference().getTargetUuid()));
        }
        if (fhirTask.getOwnerReference() != null && fhirTask.getOwnerReference().getTargetUuid() != null) {
            result.setOwner(new Reference("Practitioner/" + fhirTask.getOwnerReference().getTargetUuid()));
        }
        if (StringUtils.isNotBlank(remarks)) {
            result.addNote().setText(remarks);
        }
        return result;
    }
}
