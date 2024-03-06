package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.CareTeamDAO;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.CareTeamParticipant;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class HibernateCareTeamDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CareTeamDAO careTeamDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void shouldSaveTheCareTeamCreatedForPatientGivenPatientVisit() {

        executeDataSet("CareTeamDAOTestData.xml");

        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        Visit visit=Context.getVisitService().getVisitByUuid("84d8b838-1111-11e3-b47b-c6959a448789");
        Provider provider=Context.getProviderService().getProviderByUuid("2bdc3f7d-d911-401a-84e9-5494dda83e8e");


        CareTeamParticipant participant = new CareTeamParticipant();
        participant.setStartTime(visit.getStartDatetime());
        participant.setProvider(provider);
        Set<CareTeamParticipant> participantSet=new HashSet<>();
        participantSet.add(participant);

        CareTeam careTeam = new CareTeam();
        careTeam.setPatient(visit.getPatient());
        careTeam.setStartTime(visit.getStartDatetime());
        careTeam.setVisit(visit);
        careTeam.setParticipants(participantSet);

        CareTeam savedCareTeam = careTeamDAO.saveCareTeam(careTeam);

        List<CareTeamParticipant> participantsList = new ArrayList<>(savedCareTeam.getParticipants());

        Assertions.assertEquals(visit.getPatient().getPatientId(), savedCareTeam.getPatient().getPatientId());
        Assertions.assertEquals(provider, participantsList.get(0).getProvider());
        Assertions.assertEquals(visit, savedCareTeam.getVisit());

        sessionFactory.getCurrentSession().delete(savedCareTeam);
    }


    @Test
    public void shouldGetCareTeamGivenPatientVisit() {

        executeDataSet("CareTeamDAOTestData.xml");

        Concept testConcept = Context.getConceptService().getConceptByName("UNKNOWN");
        Visit visit=Context.getVisitService().getVisitByUuid("84d8b838-1111-11e3-b47b-c6959a448789");
        Provider provider=Context.getProviderService().getProviderByUuid("2bdc3f7d-d911-401a-84e9-5494dda83e8e");


        CareTeamParticipant participant = new CareTeamParticipant();
        participant.setStartTime(visit.getStartDatetime());
        participant.setProvider(provider);
        Set<CareTeamParticipant> participantSet=new HashSet<>();
        participantSet.add(participant);

        CareTeam careTeam = new CareTeam();
        careTeam.setPatient(visit.getPatient());
        careTeam.setStartTime(visit.getStartDatetime());
        careTeam.setVisit(visit);
        careTeam.setParticipants(participantSet);

        CareTeam savedCareTeam=careTeamDAO.saveCareTeam(careTeam);

        CareTeam careTeamByVisit= careTeamDAO.getCareTeamByVisit(visit);

        Assertions.assertEquals(savedCareTeam.getCareTeamId(),careTeamByVisit.getCareTeamId());
        Assertions.assertEquals(visit,careTeamByVisit.getVisit());
        Assertions.assertEquals(1,careTeam.getParticipants().size());

        sessionFactory.getCurrentSession().delete(savedCareTeam);
    }


}