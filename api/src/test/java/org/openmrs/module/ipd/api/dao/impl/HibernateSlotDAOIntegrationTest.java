package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HibernateSlotDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScheduleDAO scheduleDAO;

    @Autowired
    private SlotDAO slotDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void shouldSaveTheSlotForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setOrder(drugOrder);
        schedule.setForReference(patientReference);
        schedule.setByReference(providerReference);
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
        schedule.setOrder(drugOrder);
        schedule.setForReference(patientReference);
        schedule.setByReference(providerReference);
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
        schedule.setOrder(drugOrder);
        schedule.setForReference(patientReference);
        schedule.setByReference(providerReference);
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

        List<Slot> slotsAgainstSchedule = slotDAO.getSlotsByForReferenceIdAndForDateAndServiceType(patientReference, slotStartTime.toLocalDate(), testConcept);

        Assertions.assertEquals(1, slotsAgainstSchedule.size());

        sessionFactory.getCurrentSession().delete(savedSlot1);
        sessionFactory.getCurrentSession().delete(savedSlot2);
        sessionFactory.getCurrentSession().delete(savedSchedule);
    }
}
