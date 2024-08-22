package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.dao.FhirMedicationAdministrationDao;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HibernateSlotDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScheduleDAO scheduleDAO;

    @Autowired
    private SlotDAO slotDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private FhirMedicationAdministrationDao<MedicationAdministration> medicationAdministrationDao;

    @Test
    public void shouldSaveTheSlotForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
//        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime slotStartTime = LocalDateTime.now();

        Slot slot = new Slot();
        slot.setSchedule(savedSchedule);
        slot.setServiceType(testConcept);
        slot.setStartDateTime(slotStartTime);

        Slot savedSlot = slotDAO.saveSlot(slot);

        sessionFactory.getCurrentSession().delete(savedSlot);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSlotForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
//        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime slotStartTime = LocalDateTime.now();

        Slot slot = new Slot();
        slot.setSchedule(savedSchedule);
        slot.setServiceType(testConcept);
        slot.setStartDateTime(slotStartTime);

        Slot savedSlot = slotDAO.saveSlot(slot);
        Slot getSlotById = slotDAO.getSlot(savedSlot.getId());

        sessionFactory.getCurrentSession().delete(savedSlot);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSlotsForPatientByForReferenceIdAndForDateAndServiceTypeGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
//        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime slotStartTime = LocalDateTime.now();

        Slot slot1 = new Slot();
        slot1.setSchedule(savedSchedule);
        slot1.setServiceType(testConcept);
        slot1.setStartDateTime(slotStartTime);

        Slot slot2 = new Slot();
        slot2.setSchedule(savedSchedule);
        slot2.setServiceType(testConcept);
        slot2.setStartDateTime(slotStartTime.plusDays(1));

        Slot savedSlot1 = slotDAO.saveSlot(slot1);
        Slot savedSlot2 = slotDAO.saveSlot(slot2);

        List<Slot> slotsAgainstSchedule = slotDAO.getSlotsBySubjectReferenceIdAndForDateAndServiceType(patientReference, slotStartTime.toLocalDate(), testConcept);

        Assertions.assertEquals(1, slotsAgainstSchedule.size());

        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSlotsForPatientByForReferenceIdAndServiceTypeAndOrderUuid() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        DrugOrder drugOrder2 = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9143");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime slotStartTime = LocalDateTime.now();

        Slot slot1 = new Slot();
        slot1.setSchedule(savedSchedule);
        slot1.setServiceType(testConcept);
        slot1.setStartDateTime(slotStartTime);
        slot1.setOrder(drugOrder);

        Slot slot2 = new Slot();
        slot2.setSchedule(savedSchedule);
        slot2.setServiceType(testConcept);
        slot2.setStartDateTime(slotStartTime.plusDays(1));
        slot2.setOrder(drugOrder2);

        Slot savedSlot1 = slotDAO.saveSlot(slot1);
        Slot savedSlot2 = slotDAO.saveSlot(slot2);

        List<String> orderUuidList = new ArrayList<>();
        orderUuidList.add(orderUuid);

        List<Slot> slotsBySubjectReferenceIdAndServiceTypeAndOrderUuids = slotDAO.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, testConcept, orderUuidList);

        Assertions.assertEquals(1, slotsBySubjectReferenceIdAndServiceTypeAndOrderUuids.size());

        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSlotsForPatientByForReferenceIdAndServiceType() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        DrugOrder drugOrder2 = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9143");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime slotStartTime = LocalDateTime.now();

        Slot slot1 = new Slot();
        slot1.setSchedule(savedSchedule);
        slot1.setServiceType(testConcept);
        slot1.setStartDateTime(slotStartTime);
        slot1.setOrder(drugOrder);

        Slot slot2 = new Slot();
        slot2.setSchedule(savedSchedule);
        slot2.setServiceType(testConcept);
        slot2.setStartDateTime(slotStartTime.plusDays(1));
        slot2.setOrder(drugOrder2);

        Slot savedSlot1 = slotDAO.saveSlot(slot1);
        Slot savedSlot2 = slotDAO.saveSlot(slot2);

        List<Slot> slotsBySubjectReferenceIdAndServiceType = slotDAO.getSlotsBySubjectReferenceIdAndServiceType(patientReference, testConcept);

        Assertions.assertEquals(2, slotsBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSlotsForPatientByAdministeredTime() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        DrugOrder drugOrder2 = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9143");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");

        Visit visit = new Visit(1);
        visit.setPatient(new Patient(123));
        visit.setStartDatetime(new Date());
        visit.setVisitType(new VisitType(321));

        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);
        schedule.setVisit(visit);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime slot1StartTime = LocalDateTime.now().plusHours(1);
        LocalDateTime slot2StartTime = LocalDateTime.now().plusDays(-1);
        LocalDateTime medicationAdministeredTime= LocalDateTime.now().plusHours(3);
        LocalDateTime medicationAdministeredTime2= LocalDateTime.now().plusDays(3);


        MedicationAdministration medicationAdministration=new MedicationAdministration();
        medicationAdministration.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        medicationAdministration.setAdministeredDateTime(DateTimeUtil.convertLocalDateTimeDate(medicationAdministeredTime));
        MedicationAdministration savedMedicationAdministration= medicationAdministrationDao.createOrUpdate(medicationAdministration);

        MedicationAdministration medicationAdministration2=new MedicationAdministration();
        medicationAdministration2.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        medicationAdministration2.setAdministeredDateTime(DateTimeUtil.convertLocalDateTimeDate(medicationAdministeredTime2));
        MedicationAdministration savedMedicationAdministration2= medicationAdministrationDao.createOrUpdate(medicationAdministration2);

        Slot slot1 = new Slot();
        slot1.setSchedule(savedSchedule);
        slot1.setServiceType(testConcept);
        slot1.setStartDateTime(slot1StartTime);
        slot1.setOrder(drugOrder);

        Slot slot2 = new Slot();
        slot2.setSchedule(savedSchedule);
        slot2.setServiceType(testConcept);
        slot2.setStartDateTime(slot2StartTime);
        slot2.setMedicationAdministration(savedMedicationAdministration);
        slot2.setOrder(drugOrder2);

        Slot slot3 = new Slot();
        slot3.setSchedule(savedSchedule);
        slot3.setServiceType(testConcept);
        slot3.setStartDateTime(slot2StartTime);
        slot3.setOrder(drugOrder);

        Slot slot4 = new Slot();
        slot4.setSchedule(savedSchedule);
        slot4.setServiceType(testConcept);
        slot4.setStartDateTime(slot1StartTime);
        slot4.setMedicationAdministration(savedMedicationAdministration2);
        slot4.setOrder(drugOrder);

        Slot savedSlot1 = slotDAO.saveSlot(slot1);
        Slot savedSlot2 = slotDAO.saveSlot(slot2);
        Slot savedSlot3 = slotDAO.saveSlot(slot3);
        Slot savedSlot4 = slotDAO.saveSlot(slot4);


        List<Slot> slotsBySubjectReferenceIdAndServiceType = slotDAO.getSlotsBySubjectIncludingAdministeredTimeFrame(patientReference,startTime,startTime.plusHours(6),visit);

        Assertions.assertEquals(2, slotsBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedMedicationAdministration);
        sessionFactory.getCurrentSession().delete(savedMedicationAdministration2);
        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSlot3);
        sessionFactory.getCurrentSession().delete(savedSlot4);
        sessionFactory.getCurrentSession().delete(savedSchedule);
        sessionFactory.getCurrentSession().delete(visit);

    }

    @Test
    public void shouldGetTheSavedSlotsForPatientBySubjectReferenceAndAGivenTimeFrame() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        DrugOrder drugOrder2 = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9143");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Visit visit = new Visit(1);
        visit.setPatient(new Patient(123));
        visit.setStartDatetime(new Date());
        visit.setVisitType(new VisitType(321));

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setVisit(visit);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime slot1StartTime = LocalDateTime.now().plusHours(1);
        LocalDateTime slot2StartTime = LocalDateTime.now().plusDays(-1);
        LocalDateTime medicationAdministeredTime= LocalDateTime.now().plusHours(3);
        LocalDateTime medicationAdministeredTime2= LocalDateTime.now().plusDays(3);


        MedicationAdministration medicationAdministration=new MedicationAdministration();
        medicationAdministration.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        medicationAdministration.setAdministeredDateTime(DateTimeUtil.convertLocalDateTimeDate(medicationAdministeredTime));
        MedicationAdministration savedMedicationAdministration= medicationAdministrationDao.createOrUpdate(medicationAdministration);

        MedicationAdministration medicationAdministration2=new MedicationAdministration();
        medicationAdministration2.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        medicationAdministration2.setAdministeredDateTime(DateTimeUtil.convertLocalDateTimeDate(medicationAdministeredTime2));
        MedicationAdministration savedMedicationAdministration2= medicationAdministrationDao.createOrUpdate(medicationAdministration2);

        Slot slot1 = new Slot();
        slot1.setSchedule(savedSchedule);
        slot1.setServiceType(testConcept);
        slot1.setStartDateTime(slot1StartTime);
        slot1.setOrder(drugOrder);

        Slot slot2 = new Slot();
        slot2.setSchedule(savedSchedule);
        slot2.setServiceType(testConcept);
        slot2.setStartDateTime(slot2StartTime);
        slot2.setMedicationAdministration(savedMedicationAdministration);
        slot2.setOrder(drugOrder2);

        Slot slot3 = new Slot();
        slot3.setSchedule(savedSchedule);
        slot3.setServiceType(testConcept);
        slot3.setStartDateTime(slot2StartTime);
        slot3.setOrder(drugOrder);

        Slot slot4 = new Slot();
        slot4.setSchedule(savedSchedule);
        slot4.setServiceType(testConcept);
        slot4.setStartDateTime(slot1StartTime);
        slot4.setOrder(drugOrder);

        Slot savedSlot1 = slotDAO.saveSlot(slot1);
        Slot savedSlot2 = slotDAO.saveSlot(slot2);
        Slot savedSlot3 = slotDAO.saveSlot(slot3);
        Slot savedSlot4 = slotDAO.saveSlot(slot4);


        List<Slot> slotsBySubjectReferenceIdAndServiceType = slotDAO.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(patientReference,startTime,startTime.plusHours(6),visit,testConcept);

        Assertions.assertEquals(4, slotsBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedMedicationAdministration);
        sessionFactory.getCurrentSession().delete(savedMedicationAdministration2);
        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSlot3);
        sessionFactory.getCurrentSession().delete(savedSlot4);
        sessionFactory.getCurrentSession().delete(savedSchedule);
        sessionFactory.getCurrentSession().delete(visit);

    }

    @Test
    public void shouldGetSlotsForPatientListByTime() {
        executeDataSet("scheduleMedicationsTestData.xml");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse("2024-01-28 11:00:00", formatter);
        LocalDateTime endTime = LocalDateTime.parse("2024-01-29 11:00:00", formatter);
        List<String> patientUuids = new ArrayList<>();
        patientUuids.add("75e04d42-3ca8-11e3-bf2b-0800271c1b75");

        List<Slot> slotsForPatientListByTime = slotDAO.getSlotsForPatientListByTime(patientUuids, startTime, endTime);

        Assertions.assertEquals(3, slotsForPatientListByTime.size());
    }

    @Test
    public void shouldGetImmediatePreviousSlotsForPatientListByTime() {
        executeDataSet("scheduleMedicationsTestData.xml");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse("2024-01-28 11:00:00", formatter);
        List<String> patientUuids = new ArrayList<>();
        patientUuids.add("75e04d42-3ca8-11e3-bf2b-0800271c1b75");

        List<Slot> previousSlots = slotDAO.getImmediatePreviousSlotsForPatientListByTime(patientUuids, startTime);

        Assertions.assertEquals(1, previousSlots.size());
    }

    @Test
    public void shouldGetSlotDurationForPatientsByOrder() {
        executeDataSet("scheduleMedicationsTestData.xml");

        List<Order> orders = new ArrayList<>();
        Order order = Context.getOrderService().getOrderByUuid("6d0ae386-707a-4629-9850-f15206e63ab0");
        orders.add(order);
        List<Concept> serviceTypes = new ArrayList<>();
        serviceTypes.add(Context.getConceptService().getConceptByName("MedicationRequest"));

        List<Object[]> slotsDuration = slotDAO.getSlotDurationForPatientsByOrder(orders, serviceTypes);

        Assertions.assertEquals(1, slotsDuration.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse("2024-01-27 20:00:00", formatter);
        LocalDateTime endTime = LocalDateTime.parse("2024-01-29 08:00:00", formatter);

        Assertions.assertEquals(startTime, slotsDuration.get(0)[1]);
        Assertions.assertEquals(endTime, slotsDuration.get(0)[2]);
    }
}
