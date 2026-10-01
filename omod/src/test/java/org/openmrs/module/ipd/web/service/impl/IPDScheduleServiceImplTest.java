package org.openmrs.module.ipd.web.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.web.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.web.factory.ScheduleFactory;
import org.openmrs.module.ipd.web.factory.SlotFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class IPDScheduleServiceImplTest {

    @InjectMocks
    private IPDScheduleServiceImpl ipdScheduleService;

    @Mock
    private ScheduleService scheduleService;
    @Mock
    private ScheduleFactory scheduleFactory;
    @Mock
    private SlotFactory slotFactory;
    @Mock
    private SlotService slotService;
    @Mock
    private SlotTimeCreationService slotTimeCreationService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private ReferenceService referenceService;
    @Mock
    private VisitService visitService;
    @Mock
    private PatientService patientService;
    @Mock
    private OrderService orderService;

    // -------- getMedicationSlots(patientUuid, serviceType, forDate) --------

    @Test
    public void shouldReturnEmptyListWhenReferenceNotFoundForGetMedicationSlotsByDate() {
        String patientUuid = "patient-uuid-1";
        LocalDate forDate = LocalDate.now();
        Concept concept = new Concept();

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.empty());

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST, forDate);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIdAndForDateAndServiceType(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldReturnSlotsWhenReferenceExistsForGetMedicationSlotsByDate() {
        String patientUuid = "patient-uuid-1";
        LocalDate forDate = LocalDate.now();
        Concept concept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        List<Slot> expectedSlots = Arrays.asList(new Slot(), new Slot());

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndForDateAndServiceType(reference, forDate, concept))
                .thenReturn(expectedSlots);

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST, forDate);

        Assert.assertEquals(2, result.size());
        Mockito.verify(slotService, Mockito.times(1))
                .getSlotsBySubjectReferenceIdAndForDateAndServiceType(reference, forDate, concept);
    }

    // -------- getMedicationSlots(patientUuid, serviceType) --------

    @Test
    public void shouldReturnEmptyListWhenReferenceNotFoundForGetMedicationSlots() {
        String patientUuid = "patient-uuid-1";
        Concept concept = new Concept();

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.empty());

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIdAndServiceType(Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldReturnSlotsWhenReferenceExistsForGetMedicationSlots() {
        String patientUuid = "patient-uuid-1";
        Concept concept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        List<Slot> expectedSlots = Collections.singletonList(new Slot());

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceType(reference, concept))
                .thenReturn(expectedSlots);

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST);

        Assert.assertEquals(1, result.size());
        Mockito.verify(slotService, Mockito.times(1)).getSlotsBySubjectReferenceIdAndServiceType(reference, concept);
    }

    // -------- getMedicationSlots(patientUuid, serviceType, orderUuids) --------

    @Test
    public void shouldReturnEmptyListWhenReferenceNotFoundForGetMedicationSlotsByOrderUuids() {
        String patientUuid = "patient-uuid-1";
        List<String> orderUuids = Arrays.asList("order-uuid-1", "order-uuid-2");
        Concept concept = new Concept();

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.empty());

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST, orderUuids);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldReturnSlotsWhenReferenceExistsForGetMedicationSlotsByOrderUuids() {
        String patientUuid = "patient-uuid-1";
        List<String> orderUuids = Arrays.asList("order-uuid-1");
        Concept concept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        List<Slot> expectedSlots = Arrays.asList(new Slot(), new Slot(), new Slot());

        Mockito.when(conceptService.getConceptByName(ServiceType.MEDICATION_REQUEST.conceptName())).thenReturn(concept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, concept, orderUuids))
                .thenReturn(expectedSlots);

        List<Slot> result = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST, orderUuids);

        Assert.assertEquals(3, result.size());
        Mockito.verify(slotService, Mockito.times(1))
                .getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, concept, orderUuids);
    }

    // -------- getMedicationSlotsForTheGivenTimeFrame --------

    @Test
    public void shouldReturnEmptyListWhenReferenceNotFoundForTimeFrame() {
        String patientUuid = "patient-uuid-1";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(8);
        Visit visit = new Visit(1);

        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.empty());

        List<Slot> result = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(
                patientUuid, startTime, endTime, false, visit);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldUseAdministeredTimeFrameQueryWhenConsiderAdministeredTimeIsTrue() {
        String patientUuid = "patient-uuid-1";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(8);
        Visit visit = new Visit(1);
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        List<Slot> expectedSlots = Arrays.asList(new Slot());

        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(reference, startTime, endTime, visit))
                .thenReturn(expectedSlots);

        List<Slot> result = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(
                patientUuid, startTime, endTime, true, visit);

        Assert.assertEquals(1, result.size());
        Mockito.verify(slotService, Mockito.times(1))
                .getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(reference, startTime, endTime, visit);
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldUseScheduledTimeFrameQueryWhenConsiderAdministeredTimeIsFalse() {
        String patientUuid = "patient-uuid-1";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(8);
        Visit visit = new Visit(1);
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        List<Slot> expectedSlots = Arrays.asList(new Slot(), new Slot());

        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(reference, startTime, endTime, visit))
                .thenReturn(expectedSlots);

        List<Slot> result = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(
                patientUuid, startTime, endTime, false, visit);

        Assert.assertEquals(2, result.size());
        Mockito.verify(slotService, Mockito.times(1))
                .getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(reference, startTime, endTime, visit);
        Mockito.verify(slotService, Mockito.never())
                .getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldNotCreatePlaceholderSlot_WhenScheduledPlaceholderWithNoAdminAlreadyExists() {
        String patientUuid = "patient-uuid-1";
        String orderUuid = "order-uuid-1";
        Patient patient = new Patient();
        Visit visit = new Visit(1);
        Schedule schedule = new Schedule();
        schedule.setId(1);
        DrugOrder order = new DrugOrder();
        order.setUuid(orderUuid);
        Concept prnConcept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        Slot existingPlaceholder = new Slot();
        existingPlaceholder.setStatus(Slot.SlotStatus.SCHEDULED);

        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));
        Mockito.when(scheduleService.getScheduleByVisit(visit)).thenReturn(schedule);
        Mockito.when(orderService.getOrderByUuid(orderUuid)).thenReturn(order);
        Mockito.when(conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName())).thenReturn(prnConcept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, prnConcept, Arrays.asList(orderUuid)))
                .thenReturn(Arrays.asList(existingPlaceholder));

        ScheduleMedicationRequest request = ScheduleMedicationRequest.builder()
                .patientUuid(patientUuid)
                .orderUuid(orderUuid)
                .serviceType(ServiceType.AS_NEEDED_PLACEHOLDER)
                .build();

        ipdScheduleService.saveMedicationSchedule(request);

        Mockito.verify(slotFactory, Mockito.never()).createAsNeededPlaceholderSlot(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(slotService, Mockito.never()).saveSlot(Mockito.any());
    }

    @Test
    public void shouldCreatePlaceholderSlot_WhenNoExistingPlaceholderFound() {
        String patientUuid = "patient-uuid-1";
        String orderUuid = "order-uuid-1";
        Patient patient = new Patient();
        Visit visit = new Visit(1);
        Schedule schedule = new Schedule();
        schedule.setId(1);
        DrugOrder order = new DrugOrder();
        order.setUuid(orderUuid);
        Concept prnConcept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        Slot newPlaceholder = new Slot();

        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));
        Mockito.when(scheduleService.getScheduleByVisit(visit)).thenReturn(schedule);
        Mockito.when(orderService.getOrderByUuid(orderUuid)).thenReturn(order);
        Mockito.when(conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName())).thenReturn(prnConcept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, prnConcept, Arrays.asList(orderUuid)))
                .thenReturn(Collections.emptyList());
        Mockito.when(slotFactory.createAsNeededPlaceholderSlot(schedule, order, null)).thenReturn(newPlaceholder);

        ScheduleMedicationRequest request = ScheduleMedicationRequest.builder()
                .patientUuid(patientUuid)
                .orderUuid(orderUuid)
                .serviceType(ServiceType.AS_NEEDED_PLACEHOLDER)
                .build();

        ipdScheduleService.saveMedicationSchedule(request);

        Mockito.verify(slotFactory).createAsNeededPlaceholderSlot(schedule, order, null);
        Mockito.verify(slotService).saveSlot(newPlaceholder);
    }

    @Test
    public void shouldCreateNewPlaceholderSlot_WhenExistingPlaceholderHasBeenAdministered() {
        String patientUuid = "patient-uuid-1";
        String orderUuid = "order-uuid-1";
        Patient patient = new Patient();
        Visit visit = new Visit(1);
        Schedule schedule = new Schedule();
        schedule.setId(1);
        DrugOrder order = new DrugOrder();
        order.setUuid(orderUuid);
        Concept prnConcept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        Slot administeredPlaceholder = new Slot();
        administeredPlaceholder.setStatus(Slot.SlotStatus.COMPLETED);
        administeredPlaceholder.setMedicationAdministration(new MedicationAdministration());
        Slot newPlaceholder = new Slot();

        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));
        Mockito.when(scheduleService.getScheduleByVisit(visit)).thenReturn(schedule);
        Mockito.when(orderService.getOrderByUuid(orderUuid)).thenReturn(order);
        Mockito.when(conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName())).thenReturn(prnConcept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, prnConcept, Arrays.asList(orderUuid)))
                .thenReturn(Arrays.asList(administeredPlaceholder));
        Mockito.when(slotFactory.createAsNeededPlaceholderSlot(schedule, order, null)).thenReturn(newPlaceholder);

        ScheduleMedicationRequest request = ScheduleMedicationRequest.builder()
                .patientUuid(patientUuid)
                .orderUuid(orderUuid)
                .serviceType(ServiceType.AS_NEEDED_PLACEHOLDER)
                .build();

        ipdScheduleService.saveMedicationSchedule(request);

        Mockito.verify(slotFactory).createAsNeededPlaceholderSlot(schedule, order, null);
        Mockito.verify(slotService).saveSlot(newPlaceholder);
    }

    @Test
    public void shouldNotCreatePlaceholderSlot_WhenScheduledPlaceholderExistsAlongsideAdministeredOnes() {
        String patientUuid = "patient-uuid-1";
        String orderUuid = "order-uuid-1";
        Patient patient = new Patient();
        Visit visit = new Visit(1);
        Schedule schedule = new Schedule();
        schedule.setId(1);
        DrugOrder order = new DrugOrder();
        order.setUuid(orderUuid);
        Concept prnConcept = new Concept();
        Reference reference = new Reference(Patient.class.getTypeName(), patientUuid);
        Slot administeredPlaceholder = new Slot();
        administeredPlaceholder.setStatus(Slot.SlotStatus.COMPLETED);
        administeredPlaceholder.setMedicationAdministration(new MedicationAdministration());
        Slot scheduledPlaceholder = new Slot();
        scheduledPlaceholder.setStatus(Slot.SlotStatus.SCHEDULED);

        Mockito.when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));
        Mockito.when(scheduleService.getScheduleByVisit(visit)).thenReturn(schedule);
        Mockito.when(orderService.getOrderByUuid(orderUuid)).thenReturn(order);
        Mockito.when(conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName())).thenReturn(prnConcept);
        Mockito.when(referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid))
                .thenReturn(Optional.of(reference));
        Mockito.when(slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(reference, prnConcept, Arrays.asList(orderUuid)))
                .thenReturn(Arrays.asList(administeredPlaceholder, scheduledPlaceholder));

        ScheduleMedicationRequest request = ScheduleMedicationRequest.builder()
                .patientUuid(patientUuid)
                .orderUuid(orderUuid)
                .serviceType(ServiceType.AS_NEEDED_PLACEHOLDER)
                .build();

        ipdScheduleService.saveMedicationSchedule(request);

        Mockito.verify(slotFactory, Mockito.never()).createAsNeededPlaceholderSlot(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(slotService, Mockito.never()).saveSlot(Mockito.any());
    }
}
