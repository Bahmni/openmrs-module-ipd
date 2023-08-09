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
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HibernateScheduleDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScheduleDAO scheduleDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void shouldSaveTheScheduledCreatedForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        Assertions.assertEquals(drugOrder, schedule.getOrder());
        Assertions.assertEquals(patientReference, schedule.getSubject());
        Assertions.assertEquals(providerReference, schedule.getActor());
        Assertions.assertEquals(startDate, schedule.getStartDate());
        Assertions.assertEquals(endDate, schedule.getEndDate());
        Assertions.assertEquals(testConcept, schedule.getServiceType());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedScheduledCreatedForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        Schedule scheduleById = scheduleDAO.getSchedule(savedSchedule.getId());

        Assertions.assertEquals(drugOrder, scheduleById.getOrder());
        Assertions.assertEquals(patientReference, scheduleById.getSubject());
        Assertions.assertEquals(providerReference, scheduleById.getActor());
        Assertions.assertEquals(startDate, scheduleById.getStartDate());
        Assertions.assertEquals(endDate, scheduleById.getEndDate());
        Assertions.assertEquals(testConcept, scheduleById.getServiceType());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSchedulesForPatientByForReferenceIdAndServiceTypeAndOrderUuid() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);
        List<String> orderUuidList = new ArrayList<>();
        orderUuidList.add(orderUuid);

        List<Schedule> schedulesBySubjectReferenceIdAndServiceType = scheduleDAO.getSchedulesBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, testConcept, orderUuidList);

        Assertions.assertEquals(1, schedulesBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedSchedulesForPatientByForReferenceIdAndServiceType() {

        String orderUuid = "921de0a3-05c4-444a-be03-e01b4c4b9142";
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid(orderUuid);
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());
        LocalDateTime endDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStopDate());

        Schedule schedule = new Schedule();
        schedule.setOrder(drugOrder);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setServiceType(testConcept);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);
        List<String> orderUuidList = new ArrayList<>();
        orderUuidList.add(orderUuid);

        List<Schedule> schedulesBySubjectReferenceIdAndServiceType = scheduleDAO.getSchedulesBySubjectReferenceIdAndServiceTypeAndOrderUuids(patientReference, testConcept);

        Assertions.assertEquals(1, schedulesBySubjectReferenceIdAndServiceType.size());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }
}