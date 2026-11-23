package org.openmrs.module.ipd.web.controller;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhirExtension.web.mapper.TaskMapper;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.web.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IPDMedicationAdministrationControllerTest {

    private static final String MEDICATION_ADMINISTRATION_UUID = "med-admin-uuid-123";

    @Mock private IPDMedicationAdministrationService ipdMedicationAdministrationService;
    @Mock private MedicationAdministrationFactory medicationAdministrationFactory;
    @Mock private TaskMapper taskMapper;
    @Mock private UserContext userContext;
    @Mock private AdministrationService administrationService;

    private IPDMedicationAdministrationController controller;
    private MockedStatic<Context> contextMock;

    @Before
    public void setUp() {
        contextMock = mockStatic(Context.class);
        contextMock.when(Context::getUserContext).thenReturn(userContext);
        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
        controller = new IPDMedicationAdministrationController(
                ipdMedicationAdministrationService, medicationAdministrationFactory, taskMapper);
    }

    @After
    public void tearDown() {
        contextMock.close();
    }

    @Test
    public void shouldReturnForbiddenForUpdateAdhocMedicationAdministrationWhenUserLacksPrivilege() {
        when(userContext.hasPrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS)).thenReturn(false);

        ResponseEntity<Object> response = controller.updateAdhocMedicationAdministration(
                MEDICATION_ADMINISTRATION_UUID, new MedicationAdministrationRequest());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(ipdMedicationAdministrationService);
        verifyNoInteractions(medicationAdministrationFactory);
    }

    @Test
    public void shouldReturnOkForUpdateAdhocMedicationAdministrationWhenUserHasPrivilege() {
        MedicationAdministrationRequest request = new MedicationAdministrationRequest();
        MedicationAdministration fhirMedicationAdministration = new MedicationAdministration();
        MedicationAdministrationResponse expectedResponse = new MedicationAdministrationResponse();

        when(userContext.hasPrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS)).thenReturn(true);
        when(ipdMedicationAdministrationService.updateAdhocMedicationAdministration(eq(MEDICATION_ADMINISTRATION_UUID), any()))
                .thenReturn(fhirMedicationAdministration);
        when(medicationAdministrationFactory.mapMedicationAdministrationToResponse(fhirMedicationAdministration))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = controller.updateAdhocMedicationAdministration(
                MEDICATION_ADMINISTRATION_UUID, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(ipdMedicationAdministrationService).updateAdhocMedicationAdministration(MEDICATION_ADMINISTRATION_UUID, request);
    }
}
