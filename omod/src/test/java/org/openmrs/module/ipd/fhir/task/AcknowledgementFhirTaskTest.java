package org.openmrs.module.ipd.fhir.task;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationAcknowledgementRequest;
import org.openmrs.module.ipd.web.service.AmendmentConflictException;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.springframework.context.ApplicationContext;

public class AcknowledgementFhirTaskTest {

    private IPDMedicationAdministrationService ipdService;

    private MedicationAdministrationAcknowledgementTaskHandler handler;

    @Before
    public void setUp() {
        ipdService = mock(IPDMedicationAdministrationService.class);
        handler = spy(new MedicationAdministrationAcknowledgementTaskHandler());
        handler.setIpdMedicationAdministrationService(ipdService);
        doReturn(true).when(handler).hasPrivilege(PrivilegeConstants.APPROVE_AMEND_NOTE);
    }

    private Task ackTask() {
        Task task = new Task();
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setIntent(Task.TaskIntent.ORDER);
        task.setFocus(new Reference("MedicationAdministration/ma-1"));
        task.addNote().setText("looks fine");
        return task;
    }

    private org.openmrs.module.fhirExtension.model.Task storedTask() {
        FhirTask fhirTask = new FhirTask();
        fhirTask.setUuid("task-uuid");
        FhirReference forRef = new FhirReference();
        forRef.setTargetUuid("patient-1");
        fhirTask.setForReference(forRef);
        FhirReference owner = new FhirReference();
        owner.setTargetUuid("provider-1");
        fhirTask.setOwnerReference(owner);
        org.openmrs.module.fhirExtension.model.Task task = new org.openmrs.module.fhirExtension.model.Task();
        task.setFhirTask(fhirTask);
        return task;
    }

    @Test
    public void isAcknowledgement_onlyForMedicationAdministrationFocus() {
        assertThat(handler.isAcknowledgement(ackTask()), is(true));
        Task other = new Task();
        other.setFocus(new Reference("ServiceRequest/sr-1"));
        assertThat(handler.isAcknowledgement(other), is(false));
        assertThat(handler.isAcknowledgement(new Task()), is(false));
    }

    @Test
    public void acknowledge_delegatesToExistingAcknowledgeLogicAndReturnsFhirTask() {
        when(ipdService.acknowledge(eq("ma-1"), any())).thenReturn(storedTask());

        Task result = handler.acknowledge(ackTask());

        org.mockito.ArgumentCaptor<MedicationAdministrationAcknowledgementRequest> captor =
                org.mockito.ArgumentCaptor.forClass(MedicationAdministrationAcknowledgementRequest.class);
        verify(ipdService).acknowledge(eq("ma-1"), captor.capture());
        assertThat(captor.getValue().getRemarks(), equalTo("looks fine"));
        assertThat(result.getIdElement().getIdPart(), equalTo("task-uuid"));
        assertThat(result.getStatus(), equalTo(Task.TaskStatus.COMPLETED));
        assertThat(result.getIntent(), equalTo(Task.TaskIntent.ORDER));
        assertThat(result.getFocus().getReference(), equalTo("MedicationAdministration/ma-1"));
        assertThat(result.getFor().getReference(), equalTo("Patient/patient-1"));
        assertThat(result.getOwner().getReference(), equalTo("Practitioner/provider-1"));
        assertThat(result.getNoteFirstRep().getText(), equalTo("looks fine"));
    }

    @Test(expected = ForbiddenOperationException.class)
    public void acknowledge_withoutApprovePrivilege_isForbidden() {
        doReturn(false).when(handler).hasPrivilege(PrivilegeConstants.APPROVE_AMEND_NOTE);

        handler.acknowledge(ackTask());
    }

    @Test
    public void acknowledge_nonCompletedStatus_isRejected() {
        Task task = ackTask();
        task.setStatus(Task.TaskStatus.REQUESTED);

        try {
            handler.acknowledge(task);
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            verify(ipdService, never()).acknowledge(any(), any());
        }
    }

    @Test(expected = InvalidRequestException.class)
    public void acknowledge_nonOrderIntent_isRejected() {
        Task task = ackTask();
        task.setIntent(Task.TaskIntent.PLAN);

        handler.acknowledge(task);
    }

    @Test(expected = ResourceVersionConflictException.class)
    public void acknowledge_alreadyLocked_isConflict() {
        when(ipdService.acknowledge(any(), any())).thenThrow(new AmendmentConflictException("locked"));

        handler.acknowledge(ackTask());
    }

    @Test(expected = InvalidRequestException.class)
    public void acknowledge_businessRuleFailure_isBadRequest() {
        when(ipdService.acknowledge(any(), any())).thenThrow(new APIException("No notes found to acknowledge"));

        handler.acknowledge(ackTask());
    }

    // ---- BeanPostProcessor decorator ----

    private FhirTaskService decorate(FhirTaskService delegate) {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(MedicationAdministrationAcknowledgementTaskHandler.class)).thenReturn(handler);
        AcknowledgementFhirTaskServicePostProcessor processor = new AcknowledgementFhirTaskServicePostProcessor();
        processor.setApplicationContext(context);
        return (FhirTaskService) processor.postProcessAfterInitialization(delegate, "fhirTaskServiceImpl");
    }

    @Test
    public void decorator_routesAcknowledgementTaskCreateToHandler() {
        FhirTaskService delegate = mock(FhirTaskService.class);
        when(ipdService.acknowledge(eq("ma-1"), any())).thenReturn(storedTask());

        Task result = decorate(delegate).create(ackTask());

        assertThat(result.getIdElement().getIdPart(), equalTo("task-uuid"));
        verify(delegate, never()).create(any());
    }

    @Test
    public void decorator_passesOtherTasksThroughUntouched() {
        FhirTaskService delegate = mock(FhirTaskService.class);
        Task other = new Task();
        other.setFocus(new Reference("ServiceRequest/sr-1"));
        Task created = new Task();
        when(delegate.create(other)).thenReturn(created);

        Task result = decorate(delegate).create(other);

        assertThat(result, sameInstance(created));
        verify(ipdService, never()).acknowledge(any(), any());
    }

    @Test
    public void decorator_passesOtherMethodsThrough() {
        FhirTaskService delegate = mock(FhirTaskService.class);
        Task task = new Task();
        when(delegate.get("t1")).thenReturn(task);

        assertThat(decorate(delegate).get("t1"), sameInstance(task));
    }

    @Test
    public void decorator_doesNotWrapNonTaskServiceBeansOrWrapTwice() {
        AcknowledgementFhirTaskServicePostProcessor processor = new AcknowledgementFhirTaskServicePostProcessor();
        Object plain = new Object();
        assertThat(processor.postProcessAfterInitialization(plain, "x"), sameInstance(plain));

        FhirTaskService once = decorate(mock(FhirTaskService.class));
        assertThat(processor.postProcessAfterInitialization(once, "y"), sameInstance((Object) once));
    }

    @Test
    public void decorator_preservesSubInterfaceType() {
        // A bean implementing a sub-interface of FhirTaskService (like BahmniFhirTaskService) must
        // keep that type after decoration, otherwise injection into dependents fails with
        // BeanNotOfRequiredTypeException.
        SubFhirTaskService delegate = mock(SubFhirTaskService.class);
        AcknowledgementFhirTaskServicePostProcessor processor = new AcknowledgementFhirTaskServicePostProcessor();

        Object decorated = processor.postProcessAfterInitialization(delegate, "bahmniFhirTaskServiceImpl");

        assertThat(decorated, instanceOf(SubFhirTaskService.class));
        assertThat(decorated, instanceOf(FhirTaskService.class));
    }

    private interface SubFhirTaskService extends FhirTaskService {}
}
