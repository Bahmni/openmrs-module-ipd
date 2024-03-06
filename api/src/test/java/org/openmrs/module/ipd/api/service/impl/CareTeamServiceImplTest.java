package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.dao.CareTeamDAO;
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class CareTeamServiceImplTest {

    @InjectMocks
    private CareTeamServiceImpl careTeamService;

    @Mock
    private CareTeamDAO careTeamDAO;

    @Test
    public void shouldInvokeSaveCareTeamWithGivenCareTeam() {
        CareTeam careTeam = new CareTeam();
        CareTeam expectedCareTeam = new CareTeam();
        expectedCareTeam.setId(1);

        Mockito.when(careTeamDAO.saveCareTeam(careTeam)).thenReturn(expectedCareTeam);

        careTeamService.saveCareTeam(careTeam);

        Mockito.verify(careTeamDAO, Mockito.times(1)).saveCareTeam(careTeam);
    }

    @Test
    public void shouldInvokeGetCareTeamWithGivenVisit() {
        CareTeam expectedCareTeam = new CareTeam();
        Visit visit = new Visit();
        expectedCareTeam.setId(1);

        Mockito.when(careTeamDAO.getCareTeamByVisit(visit)).thenReturn(expectedCareTeam);

        careTeamService.getCareTeamByVisit(visit);

        Mockito.verify(careTeamDAO, Mockito.times(1)).getCareTeamByVisit(visit);
    }

}
