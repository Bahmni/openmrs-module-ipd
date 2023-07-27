package org.openmrs.module.ipd.controller;

import org.junit.jupiter.api.BeforeEach;
import org.openmrs.module.ipd.BaseIntegrationTest;
import org.openmrs.module.ipd.contract.ScheduleMedicationResponse;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

public class ScheduleMedicationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SessionFactory sessionFactory;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void shouldSaveMedicationSchedule() throws Exception {

        List<DrugOrder> allDrugOrders = getAllDrudOrders();

        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"orderUuid\": \""+allDrugOrders.get(0).getUuid()+"\", " +
                "\"slotStartTime\": \"2107-07-15T17:30:00.0\"," +
                "\"firstDaySlotsStartTime\": [\"2107-07-15T17:30:00.0\"]," +
                "\"dayWiseSlotsStartTime\": [\"2107-07-15T17:30:00.0\"]," +
                "\"comments\":\"changes the schedule\"," +
                "\"scheduleStrategy\":\"SCHEDULE_BY_HOUR\"" +
                "}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/" + RestConstants.VERSION_1 + "/ipd/schedule/medication", content));
        ScheduleMedicationResponse scheduleMedicationResponse = deserialize(response, new TypeReference<ScheduleMedicationResponse>() {});
        Schedule savedSchedule = scheduleService.getSchedule(scheduleMedicationResponse.getId());
        scheduleService.purgeSchedule(savedSchedule);
    }

    private List<DrugOrder> getAllDrudOrders() {
        Query query = sessionFactory.getCurrentSession().createQuery("FROM org.openmrs.DrugOrder");
        return (List<DrugOrder>) query.getResultList();
    }
}