package org.openmrs.module.ipd.fhir.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Test;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;

public class MedicationAdministrationFhirRequestMapperTest {

    private final MedicationAdministrationFhirRequestMapper mapper = new MedicationAdministrationFhirRequestMapper();

    @Test
    public void toRequest_emptyResource_yieldsNullsWithoutFailing() {
        MedicationAdministrationRequest request = mapper.toRequest(new MedicationAdministration());

        assertThat(request.getUuid(), nullValue());
        assertThat(request.getSlotUuid(), nullValue());
        assertThat(request.getAdministeredDateTime(), nullValue());
        assertThat(request.getProviders().isEmpty(), is(true));
    }

    @Test
    public void toRequest_usesResourceIdAsUuidForIdempotency() {
        MedicationAdministration ma = new MedicationAdministration();
        ma.setId("client-uuid");
        ma.setContext(new Reference("Encounter/enc-1"));

        MedicationAdministrationRequest request = mapper.toRequest(ma);

        assertThat(request.getUuid(), equalTo("client-uuid"));
        assertThat(request.getEncounterUuid(), equalTo("enc-1"));
    }

    @Test
    public void toNoteRequest_blankReasonCoding_isRejected() {
        Annotation annotation = new Annotation().setText("t");
        annotation.addExtension(IPDFhirConstants.EXT_NOTE_AMENDMENT_REASON, new CodeableConcept());

        try {
            mapper.toNoteRequest(annotation);
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            // 400
        }
    }

    @Test
    public void toNoteRequest_readsExtensions() {
        Annotation annotation = new Annotation().setText("t");
        annotation.addExtension(IPDFhirConstants.EXT_NOTE_PREVIOUS_NOTE_ID, new StringType("p1"));
        annotation.addExtension(IPDFhirConstants.EXT_NOTE_AMENDMENT_REASON,
                new CodeableConcept().addCoding(new Coding().setCode("concept-uuid")));

        MedicationAdministrationNoteRequest note = mapper.toNoteRequest(annotation);

        assertThat(note.getPreviousNoteUuid(), equalTo("p1"));
        assertThat(note.getStatusReasonUuid(), equalTo("concept-uuid"));
    }

    @Test
    public void isEmergencyCategory_absentIsScheduled_otherIsRejected() {
        MedicationAdministration ma = new MedicationAdministration();
        assertThat(mapper.isEmergencyCategory(ma), is(false));

        ma.setCategory(new CodeableConcept().addCoding(new Coding().setCode("emergency")));
        assertThat(mapper.isEmergencyCategory(ma), is(true));

        ma.setCategory(new CodeableConcept().addCoding(new Coding().setCode("adhoc")));
        try {
            mapper.isEmergencyCategory(ma);
            throw new AssertionError("expected InvalidRequestException");
        }
        catch (InvalidRequestException expected) {
            // 400
        }
    }
}
