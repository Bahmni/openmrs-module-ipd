package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.VisitDAO;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;

public class HibernateScheduleDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScheduleDAO scheduleDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private VisitDAO visitDAO;

    @Test
    public void shouldSaveTheScheduledCreatedForPatientGivenPatientSchedule() {
        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());

        Patient patient = Context.getPatientService().getPatientByUuid("2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setStartDatetime(new Date());
        visit.setVisitType(Context.getVisitService().getVisitType(1));

        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setServiceType(testConcept);
        schedule.setVisit(visit);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        Assertions.assertEquals(patientReference, schedule.getSubject());
        Assertions.assertEquals(providerReference, schedule.getActor());
        Assertions.assertEquals(startDate, schedule.getStartDate());
        Assertions.assertEquals(testConcept, schedule.getServiceType());
        Assertions.assertEquals(visit, schedule.getVisit());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedScheduledCreatedForPatientGivenPatientSchedule() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());

        Patient patient = Context.getPatientService().getPatientByUuid("2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setStartDatetime(new Date());
        visit.setVisitType(Context.getVisitService().getVisitType(1));


        Schedule schedule = new Schedule();
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setServiceType(testConcept);
        schedule.setVisit(visit);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        Schedule scheduleById = scheduleDAO.getSchedule(savedSchedule.getId());

        Assertions.assertEquals(patientReference, scheduleById.getSubject());
        Assertions.assertEquals(providerReference, scheduleById.getActor());
        Assertions.assertEquals(startDate, scheduleById.getStartDate());
        Assertions.assertEquals(testConcept, scheduleById.getServiceType());
        Assertions.assertEquals(visit, scheduleById.getVisit());

        sessionFactory.getCurrentSession().delete(savedSchedule);
    }

    @Test
    public void shouldGetTheSavedScheduleCreatedForPatientGivenVisit() {

        DrugOrder drugOrder = (DrugOrder) Context.getOrderService().getOrderByUuid("921de0a3-05c4-444a-be03-e01b4c4b9142");
        Reference patientReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference providerReference = new Reference(Patient.class.getTypeName(), "d869ad24-d2a0-4747-a888-fe55048bb7ce");
        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        LocalDateTime startDate = DateTimeUtil.convertDateToLocalDateTime(drugOrder.getEffectiveStartDate());

        Visit visit = new Visit();
        visit.setPatient(new Patient(2));
        visit.setStartDatetime(new Date());
        visit.setVisitType(Context.getVisitService().getVisitType(1));
        Visit savedVisit = visitDAO.saveVisit(visit);

        Schedule schedule = new Schedule();
        schedule.setDateCreated(new Date());
        schedule.setActive(true);
        schedule.setSubject(patientReference);
        schedule.setActor(providerReference);
        schedule.setStartDate(startDate);
        schedule.setServiceType(testConcept);
        schedule.setVisit(savedVisit);

        Schedule savedSchedule = scheduleDAO.saveSchedule(schedule);

        Schedule scheduleByVisit = scheduleDAO.getScheduleByVisit(visit);

        Assertions.assertEquals(patientReference, scheduleByVisit.getSubject());
        Assertions.assertEquals(providerReference, scheduleByVisit.getActor());
        Assertions.assertEquals(startDate, scheduleByVisit.getStartDate());
        Assertions.assertEquals(testConcept, scheduleByVisit.getServiceType());
        Assertions.assertEquals(visit, scheduleByVisit.getVisit());

        sessionFactory.getCurrentSession().delete(savedSchedule);
        sessionFactory.getCurrentSession().delete(savedVisit);
    }
}