package org.openmrs.module.ipd.web.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IPDScheduleControllerTest {

    @Mock private IPDScheduleService ipdScheduleService;
    @Mock private VisitService visitService;
    @Mock private PatientService patientService;
    @Mock private ScheduleService scheduleService;
    @Mock private UserContext userContext;
    @Mock private AdministrationService administrationService;

    private IPDScheduleController controller;
    private MockedStatic<Context> contextMock;

    private final List<String> patientUuids = Collections.singletonList("patient-uuid-1");
    private final Long startTime = 1700000000L;
    private final Long endTime = 1700086400L;

    @Before
    public void setUp() {
        contextMock = mockStatic(Context.class);
        contextMock.when(Context::getUserContext).thenReturn(userContext);
        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
        controller = new IPDScheduleController(ipdScheduleService, visitService, patientService, scheduleService);
    }

    @After
    public void tearDown() {
        contextMock.close();
    }

    @Test
    public void shouldReturnForbiddenForPatientsMedicationSummaryWhenUserHasNoPrivileges() {
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_ADMINISTRATIONS)).thenReturn(false);

        ResponseEntity<Object> response = controller.getSlotsForPatientsAndTime(patientUuids, startTime, endTime, null, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(ipdScheduleService);
    }

    @Test
    public void shouldReturnForbiddenForPatientsMedicationSummaryWhenUserLacksGetMedicationTasks() {
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_ADMINISTRATIONS)).thenReturn(true);
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_TASKS)).thenReturn(false);

        ResponseEntity<Object> response = controller.getSlotsForPatientsAndTime(patientUuids, startTime, endTime, null, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(ipdScheduleService);
    }

    @Test
    public void shouldReturnForbiddenForPatientsMedicationSummaryWhenUserLacksGetMedicationAdministrations() {
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_ADMINISTRATIONS)).thenReturn(false);

        ResponseEntity<Object> response = controller.getSlotsForPatientsAndTime(patientUuids, startTime, endTime, null, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(ipdScheduleService);
    }

    @Test
    public void shouldReturnOkForPatientsMedicationSummaryWhenUserHasBothPrivileges() {
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_ADMINISTRATIONS)).thenReturn(true);
        when(userContext.hasPrivilege(PrivilegeConstants.GET_MEDICATION_TASKS)).thenReturn(true);
        when(ipdScheduleService.getSlotsForPatientListByTime(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = controller.getSlotsForPatientsAndTime(patientUuids, startTime, endTime, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(ipdScheduleService).getSlotsForPatientListByTime(any(), any(), any(), any(), any());
    }
}
