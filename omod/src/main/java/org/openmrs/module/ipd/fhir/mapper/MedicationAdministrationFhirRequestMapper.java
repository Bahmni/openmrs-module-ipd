package org.openmrs.module.ipd.fhir.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationPerformerRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.springframework.stereotype.Component;

/**
 * Maps a FHIR R4 MedicationAdministration onto the request DTOs consumed by
 * {@code IPDMedicationAdministrationService}, so the FHIR provider reuses the existing business
 * logic (slot status, idempotency, adhoc schedule/slot creation, lock checks) instead of
 * duplicating it. Concepts (dose unit, route, site, performer function) are resolved by NAME by the
 * existing factory, hence they are read from CodeableConcept.text / Coding.display.
 */
@Component
public class MedicationAdministrationFhirRequestMapper {

    public MedicationAdministrationRequest toRequest(MedicationAdministration fhir) {
        MedicationAdministrationRequest.MedicationAdministrationRequestBuilder builder = MedicationAdministrationRequest.builder();
        if (fhir.hasIdElement() && fhir.getIdElement().hasIdPart()) {
            builder.uuid(fhir.getIdElement().getIdPart());
        }
        builder.patientUuid(idPart(fhir.getSubject()));
        builder.encounterUuid(idPart(fhir.getContext()));
        builder.orderUuid(idPart(fhir.getRequest()));
        if (fhir.hasStatus()) {
            builder.status(fhir.getStatus().toCode());
        }
        if (fhir.hasMedicationReference()) {
            builder.drugUuid(idPart(fhir.getMedicationReference()));
        }
        if (fhir.hasEffectiveDateTimeType() && fhir.getEffectiveDateTimeType().getValue() != null) {
            builder.administeredDateTime(toEpochSeconds(fhir.getEffectiveDateTimeType().getValue()));
        }
        Extension slotExtension = fhir.getExtensionByUrl(IPDFhirConstants.EXT_MEDICATION_ADMIN_SLOT);
        if (slotExtension != null && slotExtension.getValue() instanceof StringType) {
            builder.slotUuid(StringUtils.trimToNull(((StringType) slotExtension.getValue()).getValue()));
        }
        if (fhir.hasStatusReason()) {
            builder.statusReason(conceptName(fhir.getStatusReason().get(0)));
        }
        if (fhir.hasDosage()) {
            MedicationAdministration.MedicationAdministrationDosageComponent dosage = fhir.getDosage();
            builder.dosingInstructions(dosage.getText());
            if (dosage.hasDose()) {
                BigDecimal value = dosage.getDose().getValue();
                builder.dose(value != null ? value.doubleValue() : null);
                builder.doseUnits(dosage.getDose().getUnit());
            }
            builder.route(conceptName(dosage.getRoute()));
            builder.site(conceptName(dosage.getSite()));
        }

        List<MedicationAdministrationPerformerRequest> performers = new ArrayList<>();
        for (MedicationAdministration.MedicationAdministrationPerformerComponent performer : fhir.getPerformer()) {
            performers.add(MedicationAdministrationPerformerRequest.builder()
                    .providerUuid(idPart(performer.getActor()))
                    .function(conceptName(performer.getFunction()))
                    .build());
        }
        builder.providers(performers);

        List<MedicationAdministrationNoteRequest> notes = new ArrayList<>();
        for (Annotation annotation : fhir.getNote()) {
            notes.add(toNoteRequest(annotation));
        }
        builder.notes(notes);
        return builder.build();
    }

    /**
     * Annotation -> note request. The amendment-reason extension carries the concept uuid as the
     * coding code; the previous-note-id extension is carried through so the service can reject stale
     * amendments.
     */
    public MedicationAdministrationNoteRequest toNoteRequest(Annotation annotation) {
        MedicationAdministrationNoteRequest.MedicationAdministrationNoteRequestBuilder builder = MedicationAdministrationNoteRequest.builder()
                .text(annotation.getText());
        if (annotation.hasAuthorReference()) {
            builder.authorUuid(idPart(annotation.getAuthorReference()));
        }
        if (annotation.hasTime() && annotation.getTime() != null) {
            builder.recordedTime(toEpochSeconds(annotation.getTime()));
        }
        Extension reason = annotation.getExtensionByUrl(IPDFhirConstants.EXT_NOTE_AMENDMENT_REASON);
        if (reason != null) {
            if (!(reason.getValue() instanceof CodeableConcept) || !((CodeableConcept) reason.getValue()).hasCoding()
                    || StringUtils.isBlank(((CodeableConcept) reason.getValue()).getCodingFirstRep().getCode())) {
                throw new InvalidRequestException("Extension " + IPDFhirConstants.EXT_NOTE_AMENDMENT_REASON
                        + " must be a valueCodeableConcept whose coding code is the reason concept uuid");
            }
            builder.statusReasonUuid(((CodeableConcept) reason.getValue()).getCodingFirstRep().getCode());
        }
        Extension previous = annotation.getExtensionByUrl(IPDFhirConstants.EXT_NOTE_PREVIOUS_NOTE_ID);
        if (previous != null) {
            if (!(previous.getValue() instanceof StringType)) {
                throw new InvalidRequestException("Extension " + IPDFhirConstants.EXT_NOTE_PREVIOUS_NOTE_ID + " must be a valueString");
            }
            builder.previousNoteUuid(StringUtils.trimToNull(((StringType) previous.getValue()).getValue()));
        }
        return builder.build();
    }

    /** True when category carries the 'emergency' code; throws for any other category (no silent fall-through). */
    public boolean isEmergencyCategory(MedicationAdministration fhir) {
        if (!fhir.hasCategory()) {
            return false;
        }
        for (Coding coding : fhir.getCategory().getCoding()) {
            if (IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY.equals(coding.getCode())) {
                return true;
            }
        }
        throw new InvalidRequestException("Unsupported category; only '" + IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY
                + "' is supported (omit category for a scheduled administration)");
    }

    private String idPart(Reference reference) {
        if (reference == null || !reference.hasReference()) {
            return null;
        }
        return StringUtils.trimToNull(reference.getReferenceElement().getIdPart());
    }

    private String conceptName(CodeableConcept concept) {
        if (concept == null || concept.isEmpty()) {
            return null;
        }
        if (StringUtils.isNotBlank(concept.getText())) {
            return concept.getText();
        }
        for (Coding coding : concept.getCoding()) {
            if (StringUtils.isNotBlank(coding.getDisplay())) {
                return coding.getDisplay();
            }
        }
        return null;
    }

    private Long toEpochSeconds(Date date) {
        return TimeUnit.MILLISECONDS.toSeconds(date.getTime());
    }
}
