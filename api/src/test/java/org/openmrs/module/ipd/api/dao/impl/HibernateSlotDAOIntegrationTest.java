package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
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
import java.util.ArrayList;
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

    //Need to fic this test case
    @Test
    public void shouldGetTheSavedSlotsForPatientByAdministeredTime() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        DrugOrder drugOrder2 = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9143");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Patient patient = new Patient(123);
        patient.setUuid("d869ad24-d2a0-4747-a888-fe55048bb7cg");

//        Visit visit = new Visit();
//        visit.setPatient(patient);
//        visit.setStartDatetime(new Date());
//        visit.setVisitType(Context.getVisitService().getVisitType(1));
//        Visit visit1 = Context.getVisitService().saveVisit(visit);

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);
        schedule.setVisit(null);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime slot1StartTime = LocalDateTime.now().plusHours(1);
        LocalDateTime slot2StartTime = LocalDateTime.now().plusDays(-1);
        LocalDateTime medicationAdministeredTime= LocalDateTime.now().plusHours(3);
        LocalDateTime medicationAdministeredTime2= LocalDateTime.now().plusDays(3);


        MedicationAdministration medicationAdministration=new MedicationAdministration();
        medicationAdministration.setStatus(testConcept);
        medicationAdministration.setAdministeredDateTime(DateTimeUtil.convertLocalDateTimeDate(medicationAdministeredTime));
        MedicationAdministration savedMedicationAdministration= medicationAdministrationDao.createOrUpdate(medicationAdministration);

        MedicationAdministration medicationAdministration2=new MedicationAdministration();
        medicationAdministration2.setStatus(testConcept);
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


        List<Slot> slotsBySubjectReferenceIdAndServiceType = slotDAO.getSlotsBySubjectIncludingAdministeredTimeFrame(patientReference,startTime,startTime.plusHours(6),null);

        Assertions.assertEquals(0, slotsBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedMedicationAdministration);
        sessionFactory.getCurrentSession().delete(savedMedicationAdministration2);
        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSlot3);
        sessionFactory.getCurrentSession().delete(savedSlot4);
        sessionFactory.getCurrentSession().delete(savedSchedule);

    }
}
