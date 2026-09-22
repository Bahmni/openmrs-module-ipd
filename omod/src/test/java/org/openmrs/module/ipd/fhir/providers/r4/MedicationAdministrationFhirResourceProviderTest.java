package org.openmrs.module.ipd.fhir.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.search.param.MedicationAdministrationSearchParams;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.ipd.web.service.IPDVisitService;

@RunWith(MockitoJUnitRunner.class)
public class MedicationAdministrationFhirResourceProviderTest {

    private static final String ADMINISTRATION_UUID = "test-ma-uuid-1234-5678";
    private static final String WRONG_UUID = "wrong-uuid-0000-0000";
    private static final String VISIT_UUID = "visit-uuid-abcd-efgh";

    private static final int START_INDEX = 0;
    private static final int END_INDEX = 10;

    @Mock
    private FhirMedicationAdministrationService service;

    @Mock
    private IPDVisitService ipdVisitService;

    private MedicationAdministrationFhirResourceProvider provider;

    @Before
    public void setup() {
        provider = new MedicationAdministrationFhirResourceProvider();
        provider.setService(service);
        provider.setIpdVisitService(ipdVisitService);
    }

    private List<IBaseResource> getResources(IBundleProvider results) {
        return results.getResources(START_INDEX, END_INDEX);
    }

    @Test
    public void getResourceType_returnsMedicationAdministrationClass() {
        //when
        Class<? extends IBaseResource> resourceType = provider.getResourceType();

        //then
        assertThat(resourceType, equalTo(MedicationAdministration.class));
    }

    @Test
    public void read_returnsMatchingAdministration_whenFound() {
        //given
        MedicationAdministration expected = new MedicationAdministration();
        expected.setId(ADMINISTRATION_UUID);
        when(service.get(ADMINISTRATION_UUID)).thenReturn(expected);

        //when
        MedicationAdministration result = provider.getMedicationAdministrationByUuid(new IdType().setValue(ADMINISTRATION_UUID));

        //then
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(ADMINISTRATION_UUID));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void read_throwsResourceNotFoundException_whenServiceReturnsNull() {
        //given
        when(service.get(WRONG_UUID)).thenReturn(null);

        //when
        provider.getMedicationAdministrationByUuid(new IdType().setValue(WRONG_UUID));
    }

    @Test
    public void search_withoutCategory_delegatesToService() {
        //given
        ReferenceAndListParam patientReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam("patient-uuid")));
        ReferenceAndListParam contextReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam(VISIT_UUID)));

        MedicationAdministration ma = new MedicationAdministration();
        ma.setId(ADMINISTRATION_UUID);
        when(service.searchForMedicationAdministration(any())).thenReturn(
                new ca.uhn.fhir.rest.server.SimpleBundleProvider(Collections.singletonList(ma)));

        //when
        IBundleProvider results = provider.searchMedicationAdministrations(patientReference, contextReference, null, null);

        //then
        ArgumentCaptor<MedicationAdministrationSearchParams> captor = ArgumentCaptor.forClass(MedicationAdministrationSearchParams.class);
        verify(service).searchForMedicationAdministration(captor.capture());

        MedicationAdministrationSearchParams captured = captor.getValue();
        assertThat(captured.getPatientReference(), equalTo(patientReference));
        assertThat(captured.getEncounterReference(), equalTo(contextReference));

        List<IBaseResource> resources = getResources(results);
        assertThat(resources, hasSize(1));
    }

    @Test
    public void search_withEmergencyCategory_filtersViaSlotService() {
        //given
        ReferenceAndListParam contextReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam(VISIT_UUID)));
        TokenAndListParam category = new TokenAndListParam()
                .addAnd(new TokenOrListParam().add(new TokenParam(IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY)));

        // Build two slots with linked domain MedicationAdministration
        org.openmrs.module.ipd.api.model.MedicationAdministration domainMa1 =
                mock(org.openmrs.module.ipd.api.model.MedicationAdministration.class);
        when(domainMa1.getUuid()).thenReturn("uuid-1");

        org.openmrs.module.ipd.api.model.MedicationAdministration domainMa2 =
                mock(org.openmrs.module.ipd.api.model.MedicationAdministration.class);
        when(domainMa2.getUuid()).thenReturn("uuid-2");

        Slot slot1 = mock(Slot.class);
        when(slot1.getMedicationAdministration()).thenReturn(domainMa1);

        Slot slot2 = mock(Slot.class);
        when(slot2.getMedicationAdministration()).thenReturn(domainMa2);

        when(ipdVisitService.getMedicationSlots(VISIT_UUID, ServiceType.EMERGENCY_MEDICATION_REQUEST))
                .thenReturn(Arrays.asList(slot1, slot2));

        MedicationAdministration fhirMa1 = new MedicationAdministration();
        fhirMa1.setId("uuid-1");
        fhirMa1.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);

        MedicationAdministration fhirMa2 = new MedicationAdministration();
        fhirMa2.setId("uuid-2");
        fhirMa2.setStatus(MedicationAdministration.MedicationAdministrationStatus.INPROGRESS);

        when(service.get(Arrays.asList("uuid-1", "uuid-2"))).thenReturn(Arrays.asList(fhirMa1, fhirMa2));

        //when
        IBundleProvider results = provider.searchMedicationAdministrations(null, contextReference, null, category);

        //then
        List<IBaseResource> resources = getResources(results);
        assertThat(resources, notNullValue());
        assertThat(resources, hasSize(2));
    }

    @Test
    public void search_withEmergencyCategory_noMatchingSlots_returnsEmptyBundle() {
        //given
        ReferenceAndListParam contextReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam(VISIT_UUID)));
        TokenAndListParam category = new TokenAndListParam()
                .addAnd(new TokenOrListParam().add(new TokenParam(IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY)));

        when(ipdVisitService.getMedicationSlots(VISIT_UUID, ServiceType.EMERGENCY_MEDICATION_REQUEST))
                .thenReturn(Collections.emptyList());

        //when
        IBundleProvider results = provider.searchMedicationAdministrations(null, contextReference, null, category);

        //then
        List<IBaseResource> resources = getResources(results);
        assertThat(resources, notNullValue());
        assertThat(resources, hasSize(0));
    }

    @Test
    public void search_withEmergencyCategoryAndStatusFilter_appliesStatusFilterClientSide() {
        //given
        ReferenceAndListParam contextReference = new ReferenceAndListParam()
                .addAnd(new ReferenceOrListParam().add(new ReferenceParam(VISIT_UUID)));
        TokenAndListParam category = new TokenAndListParam()
                .addAnd(new TokenOrListParam().add(new TokenParam(IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY)));
        TokenAndListParam status = new TokenAndListParam()
                .addAnd(new TokenOrListParam().add(new TokenParam("in-progress")));

        org.openmrs.module.ipd.api.model.MedicationAdministration domainMa1 =
                mock(org.openmrs.module.ipd.api.model.MedicationAdministration.class);
        when(domainMa1.getUuid()).thenReturn("uuid-1");

        org.openmrs.module.ipd.api.model.MedicationAdministration domainMa2 =
                mock(org.openmrs.module.ipd.api.model.MedicationAdministration.class);
        when(domainMa2.getUuid()).thenReturn("uuid-2");

        org.openmrs.module.ipd.api.model.MedicationAdministration domainMa3 =
                mock(org.openmrs.module.ipd.api.model.MedicationAdministration.class);
        when(domainMa3.getUuid()).thenReturn("uuid-3");

        Slot slot1 = mock(Slot.class);
        when(slot1.getMedicationAdministration()).thenReturn(domainMa1);

        Slot slot2 = mock(Slot.class);
        when(slot2.getMedicationAdministration()).thenReturn(domainMa2);

        Slot slot3 = mock(Slot.class);
        when(slot3.getMedicationAdministration()).thenReturn(domainMa3);

        when(ipdVisitService.getMedicationSlots(VISIT_UUID, ServiceType.EMERGENCY_MEDICATION_REQUEST))
                .thenReturn(Arrays.asList(slot1, slot2, slot3));

        MedicationAdministration fhirMa1 = new MedicationAdministration();
        fhirMa1.setId("uuid-1");
        fhirMa1.setStatus(MedicationAdministration.MedicationAdministrationStatus.INPROGRESS);

        MedicationAdministration fhirMa2 = new MedicationAdministration();
        fhirMa2.setId("uuid-2");
        fhirMa2.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);

        MedicationAdministration fhirMa3 = new MedicationAdministration();
        fhirMa3.setId("uuid-3");
        fhirMa3.setStatus(MedicationAdministration.MedicationAdministrationStatus.INPROGRESS);

        when(service.get(Arrays.asList("uuid-1", "uuid-2", "uuid-3")))
                .thenReturn(Arrays.asList(fhirMa1, fhirMa2, fhirMa3));

        //when
        IBundleProvider results = provider.searchMedicationAdministrations(null, contextReference, status, category);

        //then
        List<IBaseResource> resources = getResources(results);
        assertThat(resources, notNullValue());
        // only in-progress resources should match; "in-progress" is the toCode() value for INPROGRESS
        assertThat(resources, hasSize(2));
    }
}
