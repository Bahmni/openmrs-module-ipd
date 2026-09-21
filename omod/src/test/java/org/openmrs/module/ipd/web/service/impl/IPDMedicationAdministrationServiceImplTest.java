package org.openmrs.module.ipd.web.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.dao.FhirMedicationAdministrationDao;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.fhir2.model.FhirReference;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.fhirExtension.model.TaskSearchRequest;
import org.openmrs.module.fhirExtension.service.TaskService;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.translators.MedicationAdministrationToSlotStatusTranslator;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationAcknowledgementRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.web.factory.ScheduleFactory;
import org.openmrs.module.ipd.web.factory.SlotFactory;
import org.openmrs.module.ipd.web.mapper.AcknowledgementTaskMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IPDMedicationAdministrationServiceImplTest {

    @Mock private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    @Mock private MedicationAdministrationTranslator medicationAdministrationTranslator;
    @Mock private MedicationAdministrationFactory medicationAdministrationFactory;
    @Mock private SlotFactory slotFactory;
    @Mock private SlotService slotService;
    @Mock private ScheduleService scheduleService;
    @Mock private FhirMedicationAdministrationDao fhirMedicationAdministrationDao;
    @Mock private MedicationAdministrationToSlotStatusTranslator medicationAdministrationToSlotStatusTranslator;
    @Mock private ScheduleFactory scheduleFactory;
    @Mock private TaskService taskService;
    @Mock private AcknowledgementTaskMapper acknowledgementTaskMapper;
    @Mock private PatientService patientService;
    @Mock private VisitService visitService;
    @Mock private ConceptService conceptService;
    @Mock private ProviderService providerService;
    @Mock private AdministrationService administrationService;

    private IPDMedicationAdministrationServiceImpl service;

    private String medicationAdminUuid;
    private String providerUuid;
    private String encounterUuid;
    private String statusReasonUuid;
    private MedicationAdministration medicationAdministration;
    private Provider provider;
    private Encounter encounter;
    private Visit visit;
    private User authenticatedUser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        service = new IPDMedicationAdministrationServiceImpl(
                fhirMedicationAdministrationService, medicationAdministrationTranslator,
                medicationAdministrationFactory, slotFactory, slotService, scheduleService,
                fhirMedicationAdministrationDao, medicationAdministrationToSlotStatusTranslator,
                scheduleFactory, taskService, acknowledgementTaskMapper,
                patientService, visitService, conceptService, providerService, administrationService);

        authenticatedUser = mock(User.class);
        service.setAuthenticatedUserSupplier(() -> authenticatedUser);

        medicationAdminUuid = "med-admin-uuid-123";
        providerUuid = "provider-uuid-456";
        encounterUuid = "encounter-uuid-789";
        statusReasonUuid = "concept-uuid-incorrect-dose";

        provider = new Provider();
        provider.setUuid(providerUuid);

        encounter = new Encounter();
        encounter.setUuid(encounterUuid);

        visit = new Visit();
        visit.setUuid("visit-uuid-123");
        encounter.setVisit(visit);

        medicationAdministration = new MedicationAdministration();
        medicationAdministration.setUuid(medicationAdminUuid);
        medicationAdministration.setEncounter(encounter);
        medicationAdministration.setNotes(new HashSet<>());
    }

    @Test
    public void shouldAddAmendmentNoteSuccessfully() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Dosage corrected from 500mg to 250mg")
                .statusReasonUuid(statusReasonUuid)
                .recordedTime(System.currentTimeMillis() / 1000)
                .build();

        MedicationAdministrationNote previousNote = new MedicationAdministrationNote();
        previousNote.setUuid("previous-note-uuid");
        previousNote.setText("Original note");
        previousNote.setVoided(false);
        medicationAdministration.getNotes().add(previousNote);

        Concept statusReasonConcept = new Concept();
        statusReasonConcept.setConceptId(123);
        statusReasonConcept.setUuid(statusReasonUuid);

        when(providerService.getProviderByUuid(providerUuid)).thenReturn(provider);
        when(conceptService.getConceptByUuid(statusReasonUuid)).thenReturn(statusReasonConcept);
        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Collections.emptyList());

        MedicationAdministrationNote result = service.amendNote(medicationAdminUuid, noteRequest);

        assertNotNull("Amendment note should be created", result);
        assertEquals("Note text should match request", noteRequest.getText(), result.getText());
        assertEquals("Author should be the provider", provider, result.getAuthor());
        assertEquals("Amendment reason concept should match", statusReasonConcept, result.getStatusReason());
        assertNotNull("Note UUID should be generated", result.getUuid());
        assertNotNull("Previous note should be linked", result.getPreviousNote());
        assertEquals("Previous note UUID should match", previousNote.getUuid(), result.getPreviousNote().getUuid());
        verify(fhirMedicationAdministrationDao, times(1)).createOrUpdate(medicationAdministration);
        verify(conceptService, times(1)).getConceptByUuid(statusReasonUuid);
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenMedicationAdminNotFound_OnAmend() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Test note")
                .build();

        when(fhirMedicationAdministrationDao.get(anyString())).thenReturn(null);

        service.amendNote(medicationAdminUuid, noteRequest);
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenAmendmentReasonNotFound() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Dosage corrected")
                .statusReasonUuid("invalid-concept-uuid")
                .build();

        when(conceptService.getConceptByUuid("invalid-concept-uuid")).thenReturn(null);
        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);

        service.amendNote(medicationAdminUuid, noteRequest);
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenMedicationAdminIsLocked_OnAmend() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Attempted amendment")
                .build();

        MedicationAdministrationNote existingNote = new MedicationAdministrationNote();
        existingNote.setUuid("note-uuid-001");
        existingNote.setVoided(false);
        medicationAdministration.getNotes().add(existingNote);

        Task acknowledgedTask = new Task();
        FhirTask fhirTask = new FhirTask();
        fhirTask.setUuid("task-uuid-001");
        fhirTask.setStatus(FhirTask.TaskStatus.COMPLETED);
        fhirTask.setName("ACKNOWLEDGE_MEDICATION_NOTE");
        FhirReference focusReference = new FhirReference();
        focusReference.setTargetUuid("note-uuid-001");
        fhirTask.setFocusReference(focusReference);
        acknowledgedTask.setFhirTask(fhirTask);

        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Arrays.asList(acknowledgedTask));

        service.amendNote(medicationAdminUuid, noteRequest);
    }

    @Test
    public void shouldAcknowledgeSuccessfully() {
        MedicationAdministrationNote noteToAcknowledge = new MedicationAdministrationNote();
        noteToAcknowledge.setUuid("note-uuid-for-ack");
        noteToAcknowledge.setText("Amendment to acknowledge");
        noteToAcknowledge.setVoided(false);
        noteToAcknowledge.setDateCreated(new Date());
        medicationAdministration.getNotes().add(noteToAcknowledge);

        MedicationAdministrationAcknowledgementRequest ackRequest = MedicationAdministrationAcknowledgementRequest.builder()
                .approvedByUuid(providerUuid)
                .remarks("Acknowledged and approved")
                .build();

        Task taskToReturn = new Task();
        FhirTask fhirTask = new FhirTask();
        fhirTask.setUuid("task-uuid-ack");
        fhirTask.setStatus(FhirTask.TaskStatus.COMPLETED);
        taskToReturn.setFhirTask(fhirTask);

        Person authenticatedPerson = mock(Person.class);
        Concept taskTypeConcept = mock(Concept.class);
        ConceptName taskTypeConceptName = mock(ConceptName.class);
        when(taskTypeConceptName.getName()).thenReturn("acknowledge_amend_note");
        when(taskTypeConcept.getName()).thenReturn(taskTypeConceptName);
        when(authenticatedUser.getPerson()).thenReturn(authenticatedPerson);
        when(providerService.getProvidersByPerson(authenticatedPerson)).thenReturn(Collections.singleton(provider));
        when(administrationService.getGlobalProperty("ipd.acknowledgement_task_type")).thenReturn("task-type-uuid");
        when(conceptService.getConceptByUuid("task-type-uuid")).thenReturn(taskTypeConcept);
        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Collections.emptyList());
        when(acknowledgementTaskMapper.createAcknowledgementTask(
                eq("note-uuid-for-ack"), any(), eq("ACKNOWLEDGE_MEDICATION_NOTE"),
                eq("acknowledge_amend_note"), eq("Acknowledged and approved"), any()))
                .thenReturn(taskToReturn);

        Task result = service.acknowledge(medicationAdminUuid, ackRequest);

        assertNotNull("Acknowledgement task should be created", result);
        assertEquals("Task status should be COMPLETED", FhirTask.TaskStatus.COMPLETED, result.getFhirTask().getStatus());
        verify(taskService, times(1)).saveTask(taskToReturn);
        verify(administrationService, times(1)).getGlobalProperty("ipd.acknowledgement_task_type");
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenMedicationAdminNotFound_OnAcknowledge() {
        MedicationAdministrationAcknowledgementRequest ackRequest = MedicationAdministrationAcknowledgementRequest.builder()
                .approvedByUuid(providerUuid)
                .remarks("Test")
                .build();

        when(fhirMedicationAdministrationDao.get(anyString())).thenReturn(null);

        service.acknowledge(medicationAdminUuid, ackRequest);
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenMedicationAdminIsAlreadyLocked_OnAcknowledge() {
        MedicationAdministrationNote noteToAcknowledge = new MedicationAdministrationNote();
        noteToAcknowledge.setUuid("note-uuid-for-ack");
        noteToAcknowledge.setVoided(false);
        noteToAcknowledge.setDateCreated(new Date());
        medicationAdministration.getNotes().add(noteToAcknowledge);

        Task existingAcknowledgementTask = new Task();
        FhirTask fhirTask = new FhirTask();
        fhirTask.setUuid("existing-task-uuid");
        fhirTask.setStatus(FhirTask.TaskStatus.COMPLETED);
        fhirTask.setName("ACKNOWLEDGE_MEDICATION_NOTE");
        FhirReference focusReference = new FhirReference();
        focusReference.setTargetUuid("note-uuid-for-ack");
        fhirTask.setFocusReference(focusReference);
        existingAcknowledgementTask.setFhirTask(fhirTask);

        MedicationAdministrationAcknowledgementRequest ackRequest = MedicationAdministrationAcknowledgementRequest.builder()
                .approvedByUuid(providerUuid)
                .remarks("Should fail")
                .build();

        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Arrays.asList(existingAcknowledgementTask));

        service.acknowledge(medicationAdminUuid, ackRequest);
    }

    @Test
    public void shouldAmendSuccessfully_WhenExistingNoteHasNullVoided() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Amendment over a note with null voided")
                .build();

        MedicationAdministrationNote existingNote = new MedicationAdministrationNote();
        existingNote.setUuid("note-uuid-null-voided");
        existingNote.setText("Existing note");
        existingNote.setDateCreated(new Date());
        medicationAdministration.getNotes().add(existingNote);

        when(providerService.getProviderByUuid(providerUuid)).thenReturn(provider);
        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Collections.emptyList());

        MedicationAdministrationNote result = service.amendNote(medicationAdminUuid, noteRequest);

        assertNotNull("Amendment note should be created", result);
        assertNotNull("Previous note should be linked despite null voided on the existing note", result.getPreviousNote());
        assertEquals("Previous note should be the existing note", existingNote.getUuid(), result.getPreviousNote().getUuid());
    }

    @Test
    public void shouldSelectHeadOfPreviousNoteChain_AsLatestNote_AmongMultipleNonVoidedNotes() {
        MedicationAdministrationNoteRequest noteRequest = MedicationAdministrationNoteRequest.builder()
                .authorUuid(providerUuid)
                .text("Fourth amendment")
                .build();

        MedicationAdministrationNote firstNote = new MedicationAdministrationNote();
        firstNote.setUuid("note-1");
        firstNote.setVoided(false);
        firstNote.setDateCreated(new Date(3000));

        MedicationAdministrationNote secondNote = new MedicationAdministrationNote();
        secondNote.setUuid("note-2");
        secondNote.setVoided(false);
        secondNote.setPreviousNote(firstNote);
        secondNote.setDateCreated(new Date(1000));

        MedicationAdministrationNote thirdNote = new MedicationAdministrationNote();
        thirdNote.setUuid("note-3");
        thirdNote.setVoided(false);
        thirdNote.setPreviousNote(secondNote);
        thirdNote.setDateCreated(new Date(2000));

        medicationAdministration.getNotes().add(firstNote);
        medicationAdministration.getNotes().add(secondNote);
        medicationAdministration.getNotes().add(thirdNote);

        when(providerService.getProviderByUuid(providerUuid)).thenReturn(provider);
        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);
        when(taskService.searchTasks(any(TaskSearchRequest.class))).thenReturn(Collections.emptyList());

        MedicationAdministrationNote result = service.amendNote(medicationAdminUuid, noteRequest);

        assertNotNull("Latest note (head of chain) should be linked as previousNote", result.getPreviousNote());
        assertEquals("Head of the previousNote chain should be selected regardless of dateCreated ordering",
                "note-3", result.getPreviousNote().getUuid());
    }

    @Test(expected = APIException.class)
    public void shouldThrowException_WhenNoNotesExist_OnAcknowledge() {
        medicationAdministration.setNotes(new HashSet<>());

        MedicationAdministrationAcknowledgementRequest ackRequest = MedicationAdministrationAcknowledgementRequest.builder()
                .approvedByUuid(providerUuid)
                .remarks("No notes to acknowledge")
                .build();

        when(fhirMedicationAdministrationDao.get(medicationAdminUuid)).thenReturn(medicationAdministration);

        service.acknowledge(medicationAdminUuid, ackRequest);
    }
}
