package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.*;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class SlotServiceImplTest {

    @InjectMocks
    private SlotServiceImpl slotService;

    @Mock
    private SlotDAO slotDAO;

    @Test
    public void shouldInvokeSaveSlotWithGivenSlot() {
        Slot slot = new Slot();
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);

        Mockito.when(slotDAO.saveSlot(slot)).thenReturn(expectedSlot);

        slotService.saveSlot(slot);

        Mockito.verify(slotDAO, Mockito.times(1)).saveSlot(slot);
    }

    @Test
    public void shouldInvokeGetSlotWithGivenSlotId() {
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);

        Mockito.when(slotDAO.getSlot(1)).thenReturn(expectedSlot);

        slotService.getSlot(1);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlot(1);
    }

    @Test
    public void shouldInvokeGetSlotsByForReferenceAndForDateAndServiceTypeWithGivenScheduleId() {
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);
        List<Slot> slots = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Concept medicationRequestConcept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("MedicationRequest");
        conceptName.setLocale(Locale.US);
        medicationRequestConcept.setFullySpecifiedName(conceptName);

        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");

        Mockito.when(slotDAO.getSlotsBySubjectReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept)).thenReturn(slots);

        slotService.getSlotsBySubjectReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsBySubjectReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept);
    }

    @Test
    public void shouldInvokeGetSlotsByForReferenceAndServiceTypeWithGivenScheduleId() {
        List<Slot> slots = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Concept medicationRequestConcept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("MedicationRequest");
        conceptName.setLocale(Locale.US);
        medicationRequestConcept.setFullySpecifiedName(conceptName);

        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");

        Mockito.when(slotDAO.getSlotsBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept)).thenReturn(slots);

        slotService.getSlotsBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept);
    }

    @Test
    public void shouldInvokeGetSlotsByForReferenceAndServiceTypeAndOrderUuidsWithGivenScheduleId() {

        Visit visit = new Visit(1);
        visit.setPatient(new Patient(123));
        visit.setStartDatetime(new Date());
        visit.setVisitType(new VisitType(321));

        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setId(1);
        expectedSchedule.setVisit(visit);
        List<Slot> slots = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Concept medicationRequestConcept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("MedicationRequest");
        conceptName.setLocale(Locale.US);
        medicationRequestConcept.setFullySpecifiedName(conceptName);

        List<String> orderUuids = new ArrayList<>();
        orderUuids.add("orderUuid");

        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");

        Mockito.when(slotDAO.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, medicationRequestConcept, orderUuids,visit)).thenReturn(slots);

        slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, medicationRequestConcept, orderUuids,visit);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, medicationRequestConcept, orderUuids,visit);
    }

    @Test
    public void shouldInvokeGetSlotsBySubjectReferenceAndAdministeredTimeWithGivenTimeFrame() {

        List<Slot> slots = new ArrayList<>();
        LocalDateTime startTime= LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(8);
        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");
        Visit visit = new Visit(123);

        Mockito.when(slotDAO.getSlotsBySubjectIncludingAdministeredTimeFrame(patientReference, startTime, endTime, visit)).thenReturn(slots);

        slotService.getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(patientReference,startTime,endTime,visit);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsBySubjectIncludingAdministeredTimeFrame(patientReference, startTime, endTime, visit);
    }

    @Test
    public void shouldInvokeGetSlotsForPatientListByTime() {
        List<Slot> slots = new ArrayList<>();
        LocalDateTime startTime= LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(3);
        List<String> patientUuids = new ArrayList<>();
        patientUuids.add("patientUuid1");
        patientUuids.add("patientUuid2");

        Mockito.when(slotDAO.getSlotsForPatientListByTime(patientUuids, startTime, endTime)).thenReturn(slots);
        slotService.getSlotsForPatientListByTime(patientUuids, startTime, endTime);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsForPatientListByTime(patientUuids, startTime, endTime);
    }

    @Test
    public void shouldInvokeGetImmediatePreviousSlotsForPatientListByTime() {
        List<Slot> slots = new ArrayList<>();
        LocalDateTime startTime= LocalDateTime.now();
        List<String> patientUuids = new ArrayList<>();
        patientUuids.add("patientUuid1");
        patientUuids.add("patientUuid2");

        Mockito.when(slotDAO.getImmediatePreviousSlotsForPatientListByTime(patientUuids, startTime)).thenReturn(slots);
        slotService.getImmediatePreviousSlotsForPatientListByTime(patientUuids, startTime);

        Mockito.verify(slotDAO, Mockito.times(1)).getImmediatePreviousSlotsForPatientListByTime(patientUuids, startTime);
    }

    @Test
    public void shouldInvokeGetSlotDurationForPatientsByOrder() {
        List<Object[]> slotDurationObjects = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.setUuid("orderUuid1");
        orders.add(order);
        order = new Order();
        order.setUuid("orderUuid2");
        orders.add(order);

        List<Concept> serviceTypes = new ArrayList<>();
        Concept concept = new Concept();
        concept.setUuid("serviceType1");
        serviceTypes.add(concept);

        Mockito.when(slotDAO.getSlotDurationForPatientsByOrder(orders, serviceTypes)).thenReturn(slotDurationObjects);
        slotService.getSlotDurationForPatientsByOrder(orders, serviceTypes);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotDurationForPatientsByOrder(orders, serviceTypes);
    }
}