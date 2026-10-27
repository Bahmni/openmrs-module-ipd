package org.openmrs.module.ipd.fhir.providers.r4;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.APIException;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.ipd.fhir.mapper.MedicationAdministrationFhirRequestMapper;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.service.AmendmentConflictException;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MedicationAdministrationFhirResourceProviderWriteTest {

    private static final String MA_UUID = "ma-uuid-1";
    private static final String SLOT_UUID = "slot-uuid-1";
    private static final String PATIENT_UUID = "patient-uuid-1";
    private static final String PRACTITIONER_UUID = "practitioner-uuid-1";
    private static final String ORDER_UUID = "order-uuid-1";
    private static final String DRUG_UUID = "drug-uuid-1";

    @Mock
    private FhirMedicationAdministrationService service;

    @Mock
    private IPDMedicationAdministrationService ipdService;

    private MedicationAdministrationFhirResourceProvider provider;

    @Before
    public void setUp() {
        provider = spy(new MedicationAdministrationFhirResourceProvider());
        provider.setService(service);
        provider.setIpdMedicationAdministrationService(ipdService);
        provider.setRequestMapper(new MedicationAdministrationFhirRequestMapper());
        doReturn(true).when(provider).hasPrivilege(anyString());
    }

    private MedicationAdministration scheduled() {
        MedicationAdministration ma = new MedicationAdministration();
        ma.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        ma.setSubject(new Reference("Patient/" + PATIENT_UUID));
        ma.setRequest(new Reference("MedicationRequest/" + ORDER_UUID));
        ma.setEffective(new DateTimeType(new Date(1700000000000L)));
        ma.addPerformer().setActor(new Reference("Practitioner/" + PRACTITIONER_UUID))
                .setFunction(new CodeableConcept().setText("Performer"));
        ma.addNote().setText("given").setAuthor(new Reference("Practitioner/" + PRACTITIONER_UUID));
        ma.addExtension(IPDFhirConstants.EXT_MEDICATION_ADMIN_SLOT, new StringType(SLOT_UUID));
        return ma;
    }

    private MedicationAdministration emergency() {
        MedicationAdministration ma = new MedicationAdministration();
        ma.setStatus(MedicationAdministration.MedicationAdministrationStatus.COMPLETED);
        ma.setSubject(new Reference("Patient/" + PATIENT_UUID));
        ma.setCategory(new CodeableConcept().addCoding(new org.hl7.fhir.r4.model.Coding().setCode("emergency")));
        ma.setMedication(new Reference("Medication/" + DRUG_UUID));
        ma.addPerformer().setActor(new Reference("Practitioner/" + PRACTITIONER_UUID));
        SimpleQuantity dose = new SimpleQuantity();
        dose.setValue(2.5);
        dose.setUnit("Tablet");
        MedicationAdministration.MedicationAdministrationDosageComponent dosage = ma.getDosage();
        dosage.setDose(dose);
        dosage.setRoute(new CodeableConcept().setText("Oral"));
        dosage.setSite(new CodeableConcept().addCoding(new org.hl7.fhir.r4.model.Coding().setDisplay("Mouth")));
        return ma;
    }

    private MedicationAdministration stored(boolean withRequest) {
        MedicationAdministration ma = new MedicationAdministration();
        ma.setId(MA_UUID);
        if (withRequest) {
            ma.setRequest(new Reference("MedicationRequest/" + ORDER_UUID));
        }
        return ma;
    }

    // ---- create ----

    @Test
    public void create_scheduled_delegatesToServiceWithMappedRequest() {
        when(ipdService.saveScheduledMedicationAdministration(any())).thenReturn(stored(true));

        MethodOutcome outcome = provider.createMedicationAdministration(scheduled());

        ArgumentCaptor<MedicationAdministrationRequest> captor = ArgumentCaptor.forClass(MedicationAdministrationRequest.class);
        verify(ipdService).saveScheduledMedicationAdministration(captor.capture());
        MedicationAdministrationRequest request = captor.getValue();
        assertThat(request.getSlotUuid(), equalTo(SLOT_UUID));
        assertThat(request.getPatientUuid(), equalTo(PATIENT_UUID));
        assertThat(request.getOrderUuid(), equalTo(ORDER_UUID));
        assertThat(request.getStatus(), equalTo("completed"));
        assertThat(request.getAdministeredDateTime(), equalTo(1700000000L));
        assertThat(request.getProviders().get(0).getProviderUuid(), equalTo(PRACTITIONER_UUID));
        assertThat(request.getProviders().get(0).getFunction(), equalTo("Performer"));
        assertThat(request.getNotes().get(0).getAuthorUuid(), equalTo(PRACTITIONER_UUID));
        assertThat(request.getNotes().get(0).getText(), equalTo("given"));
        verify(ipdService, never()).saveAdhocMedicationAdministration(any());
        verify(provider).hasPrivilege(PrivilegeConstants.EDIT_MEDICATION_ADMINISTRATION);
        assertThat(outcome.getResource(), notNullValue());
        assertThat(((MedicationAdministration) outcome.getResource()).hasCategory(), is(false));
    }

    @Test(expected = InvalidRequestException.class)
    public void create_scheduled_withoutSlotExtension_isRejected() {
        MedicationAdministration ma = scheduled();
        ma.setExtension(Collections.emptyList());

        provider.createMedicationAdministration(ma);
    }

    @Test
    public void create_emergency_usesAdhocPathAndReturnsCategory() {
        when(ipdService.saveAdhocMedicationAdministration(any())).thenReturn(stored(false));

        MethodOutcome outcome = provider.createMedicationAdministration(emergency());

        ArgumentCaptor<MedicationAdministrationRequest> captor = ArgumentCaptor.forClass(MedicationAdministrationRequest.class);
        verify(ipdService).saveAdhocMedicationAdministration(captor.capture());
        MedicationAdministrationRequest request = captor.getValue();
        assertThat(request.getDrugUuid(), equalTo(DRUG_UUID));
        assertThat(request.getDose(), equalTo(2.5));
        assertThat(request.getDoseUnits(), equalTo("Tablet"));
        assertThat(request.getRoute(), equalTo("Oral"));
        assertThat(request.getSite(), equalTo("Mouth"));
        assertThat(request.getPatientUuid(), equalTo(PATIENT_UUID));
        verify(ipdService, never()).saveScheduledMedicationAdministration(any());
        verify(provider).hasPrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS);
        MedicationAdministration created = (MedicationAdministration) outcome.getResource();
        assertThat(created.getCategory().getCodingFirstRep().getCode(), equalTo("emergency"));
    }

    @Test
    public void create_unknownCategory_isRejectedWithoutCallingService() {
        MedicationAdministration ma = scheduled();
        ma.setCategory(new CodeableConcept().addCoding(new org.hl7.fhir.r4.model.Coding().setCode("routine")));

        try {
            provider.createMedicationAdministration(ma);
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            verify(ipdService, never()).saveScheduledMedicationAdministration(any());
            verify(ipdService, never()).saveAdhocMedicationAdministration(any());
        }
    }

    @Test(expected = InvalidRequestException.class)
    public void create_emergency_withoutPerformer_isRejected() {
        MedicationAdministration ma = emergency();
        ma.setPerformer(Collections.emptyList());

        provider.createMedicationAdministration(ma);
    }

    @Test(expected = InvalidRequestException.class)
    public void create_emergency_withoutMedication_isRejected() {
        MedicationAdministration ma = emergency();
        ma.setMedication(null);

        provider.createMedicationAdministration(ma);
    }

    @Test(expected = InvalidRequestException.class)
    public void create_withoutSubject_isRejected() {
        MedicationAdministration ma = scheduled();
        ma.setSubject(null);

        provider.createMedicationAdministration(ma);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void create_withoutPrivilege_isForbidden() {
        doReturn(false).when(provider).hasPrivilege(anyString());

        provider.createMedicationAdministration(scheduled());
    }

    @Test(expected = InvalidRequestException.class)
    public void create_serviceApiException_isMappedToBadRequest() {
        when(ipdService.saveScheduledMedicationAdministration(any())).thenThrow(new APIException("Slot not found"));

        provider.createMedicationAdministration(scheduled());
    }

    // ---- update ----

    @Test
    public void update_adhoc_delegatesToService() {
        when(service.get(MA_UUID)).thenReturn(stored(false));
        when(ipdService.updateAdhocMedicationAdministration(eq(MA_UUID), any())).thenReturn(stored(false));

        MethodOutcome outcome = provider.updateMedicationAdministration(new IdType(MA_UUID), emergency());

        verify(ipdService).updateAdhocMedicationAdministration(eq(MA_UUID), any(MedicationAdministrationRequest.class));
        verify(provider).hasPrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS);
        assertThat(((MedicationAdministration) outcome.getResource()).getCategory().getCodingFirstRep().getCode(),
                equalTo("emergency"));
    }

    @Test
    public void update_scheduledAdministration_isRejected() {
        when(service.get(MA_UUID)).thenReturn(stored(true));

        try {
            provider.updateMedicationAdministration(new IdType(MA_UUID), emergency());
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            verify(ipdService, never()).updateAdhocMedicationAdministration(anyString(), any());
        }
    }

    @Test(expected = ResourceNotFoundException.class)
    public void update_unknownId_isNotFound() {
        when(service.get(MA_UUID)).thenReturn(null);

        provider.updateMedicationAdministration(new IdType(MA_UUID), emergency());
    }

    @Test(expected = InvalidRequestException.class)
    public void update_bodyIdMismatch_isRejected() {
        MedicationAdministration body = emergency();
        body.setId("another-id");

        provider.updateMedicationAdministration(new IdType(MA_UUID), body);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void update_withoutPrivilege_isForbidden() {
        doReturn(false).when(provider).hasPrivilege(anyString());

        provider.updateMedicationAdministration(new IdType(MA_UUID), emergency());
    }

    // ---- patch ----

    private static final String NOTE_PATCH = "[{\"op\":\"add\",\"path\":\"/note/-\",\"value\":{"
            + "\"text\":\"Dose corrected\","
            + "\"authorReference\":{\"reference\":\"Practitioner/" + PRACTITIONER_UUID + "\"},"
            + "\"time\":\"2023-11-14T22:13:20Z\","
            + "\"extension\":["
            + "{\"url\":\"" + IPDFhirConstants.EXT_NOTE_PREVIOUS_NOTE_ID + "\",\"valueString\":\"prev-note-uuid\"},"
            + "{\"url\":\"" + IPDFhirConstants.EXT_NOTE_AMENDMENT_REASON + "\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"reason-concept-uuid\"}]}}"
            + "]}}]";

    @Test
    public void patch_appendNote_delegatesToAmendNoteAndReturnsUpdatedResource() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        when(ipdService.amendNote(eq(MA_UUID), any())).thenReturn(new MedicationAdministrationNote());

        MethodOutcome outcome = provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH,
                NOTE_PATCH, null);

        ArgumentCaptor<MedicationAdministrationNoteRequest> captor = ArgumentCaptor.forClass(MedicationAdministrationNoteRequest.class);
        verify(ipdService).amendNote(eq(MA_UUID), captor.capture());
        MedicationAdministrationNoteRequest note = captor.getValue();
        assertThat(note.getText(), equalTo("Dose corrected"));
        assertThat(note.getAuthorUuid(), equalTo(PRACTITIONER_UUID));
        assertThat(note.getStatusReasonUuid(), equalTo("reason-concept-uuid"));
        assertThat(note.getPreviousNoteUuid(), equalTo("prev-note-uuid"));
        assertThat(note.getRecordedTime(), equalTo(1700000000L));
        verify(provider).hasPrivilege(PrivilegeConstants.EDIT_MEDICATION_ADMINISTRATION);
        assertThat(outcome.getResource(), notNullValue());
    }

    @Test
    public void patch_staleOrLockedAmendment_isConflict() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        when(ipdService.amendNote(eq(MA_UUID), any())).thenThrow(new AmendmentConflictException("Stale amendment"));

        try {
            provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, NOTE_PATCH, null);
            throw new AssertionError("expected ResourceVersionConflictException");
        }
        catch (ResourceVersionConflictException expected) {
            // 409
        }
    }

    @Test(expected = InvalidRequestException.class)
    public void patch_otherApiException_isBadRequest() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        when(ipdService.amendNote(eq(MA_UUID), any())).thenThrow(new APIException("Provider not found"));

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, NOTE_PATCH, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void patch_nonJsonPatchType_isRejected() {
        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.FHIR_PATCH_JSON, NOTE_PATCH, null);
    }

    @Test
    public void patch_unsupportedOperation_isRejectedBeforeAnyNoteIsAppended() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        String body = "[{\"op\":\"add\",\"path\":\"/note/-\",\"value\":{\"text\":\"ok\",\"authorReference\":{\"reference\":\"Practitioner/p\"}}},"
                + "{\"op\":\"replace\",\"path\":\"/status\",\"value\":\"stopped\"}]";

        try {
            provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, body, null);
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            verify(ipdService, never()).amendNote(anyString(), any());
        }
    }

    @Test(expected = InvalidRequestException.class)
    public void patch_noteWithoutAuthor_isRejected() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        String body = "[{\"op\":\"add\",\"path\":\"/note/-\",\"value\":{\"text\":\"no author\"}}]";

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, body, null);
    }

    @Test(expected = InvalidRequestException.class)
    public void patch_invalidJson_isRejected() {
        when(service.get(MA_UUID)).thenReturn(stored(true));

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, "not json", null);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void patch_unknownMedicationAdministration_isNotFound() {
        when(service.get(MA_UUID)).thenReturn(null);

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, NOTE_PATCH, null);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void patch_withoutPrivilege_isForbidden() {
        doReturn(false).when(provider).hasPrivilege(anyString());

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, NOTE_PATCH, null);
    }

    @Test
    public void patch_multipleNotes_onlyFirstCarriesClientPreviousNoteId() {
        when(service.get(MA_UUID)).thenReturn(stored(true));
        String note = "{\"text\":\"n\",\"authorReference\":{\"reference\":\"Practitioner/p\"},\"extension\":[{\"url\":\""
                + IPDFhirConstants.EXT_NOTE_PREVIOUS_NOTE_ID + "\",\"valueString\":\"prev\"}]}";
        String body = "[{\"op\":\"add\",\"path\":\"/note/-\",\"value\":" + note + "},{\"op\":\"add\",\"path\":\"/note/-\",\"value\":" + note + "}]";

        provider.patchMedicationAdministration(new IdType(MA_UUID), PatchTypeEnum.JSON_PATCH, body, null);

        ArgumentCaptor<MedicationAdministrationNoteRequest> captor = ArgumentCaptor.forClass(MedicationAdministrationNoteRequest.class);
        verify(ipdService, org.mockito.Mockito.times(2)).amendNote(eq(MA_UUID), captor.capture());
        assertThat(captor.getAllValues().get(0).getPreviousNoteUuid(), equalTo("prev"));
        assertThat(captor.getAllValues().get(1).getPreviousNoteUuid(), nullValue());
    }

    // ---- category on read / search ----

    @Test
    public void read_withoutMedicationRequest_getsEmergencyCategory() {
        when(service.get(MA_UUID)).thenReturn(stored(false));

        MedicationAdministration result = provider.getMedicationAdministrationByUuid(new IdType(MA_UUID));

        assertThat(result.getCategory().getCodingFirstRep().getCode(), equalTo("emergency"));
    }

    @Test
    public void read_withMedicationRequest_hasNoCategory() {
        when(service.get(MA_UUID)).thenReturn(stored(true));

        MedicationAdministration result = provider.getMedicationAdministrationByUuid(new IdType(MA_UUID));

        assertThat(result.hasCategory(), is(false));
    }

    @Test
    public void genericSearch_setsCategoryOnReturnedResources() {
        IBundleProvider upstream = new SimpleBundleProvider(java.util.Arrays.<org.hl7.fhir.instance.model.api.IBaseResource>asList(stored(false), stored(true)));
        when(service.searchForMedicationAdministration(any())).thenReturn(upstream);

        IBundleProvider results = provider.searchMedicationAdministrations(null, null, null, null);

        java.util.List<org.hl7.fhir.instance.model.api.IBaseResource> resources = results.getResources(0, 10);
        assertThat(((MedicationAdministration) resources.get(0)).hasCategory(), is(true));
        assertThat(((MedicationAdministration) resources.get(1)).hasCategory(), is(false));
        assertThat(results.size(), equalTo(2));
    }
}
