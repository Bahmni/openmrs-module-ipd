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
import org.openmrs.module.ipd.api.dao.ScheduleDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class ScheduleServiceImplTest {

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private ScheduleDAO scheduleDAO;

    @Test
    public void shouldInvokeSaveScheduleWithGivenSchedule() {
        Schedule schedule = new Schedule();
        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setId(1);

        Mockito.when(scheduleDAO.saveSchedule(schedule)).thenReturn(expectedSchedule);

        scheduleService.saveSchedule(schedule);

        Mockito.verify(scheduleDAO, Mockito.times(1)).saveSchedule(schedule);
    }

    @Test
    public void shouldInvokeGetScheduleWithGivenScheduleId() {
        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setId(1);

        Mockito.when(scheduleDAO.getSchedule(1)).thenReturn(expectedSchedule);

        scheduleService.getSchedule(1);

        Mockito.verify(scheduleDAO, Mockito.times(1)).getSchedule(1);
    }

    @Test
    public void shouldInvokeGetSchedulesByForReferenceAndServiceTypeWithGivenScheduleId() {
        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setId(1);
        List<Schedule> schedules = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Concept medicationRequestConcept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("MedicationRequest");
        conceptName.setLocale(Locale.US);
        medicationRequestConcept.setFullySpecifiedName(conceptName);

        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");

        Mockito.when(scheduleDAO.getSchedulesBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept)).thenReturn(schedules);

        scheduleService.getSchedulesBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept);

        Mockito.verify(scheduleDAO, Mockito.times(1)).getSchedulesBySubjectReferenceIdAndServiceType(patientReference, medicationRequestConcept);
    }
}