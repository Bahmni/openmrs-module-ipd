package org.openmrs.module.ipd.fhir.providers.r4;

import static lombok.AccessLevel.PACKAGE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.search.param.MedicationAdministrationSearchParams;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.openmrs.module.ipd.fhir.mapper.MedicationAdministrationFhirRequestMapper;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.service.AmendmentConflictException;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.web.service.IPDVisitService;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("medicationAdministrationFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class MedicationAdministrationFhirResourceProvider implements IResourceProvider {

    @Autowired
    private FhirMedicationAdministrationService service;

    @Autowired
    private IPDVisitService ipdVisitService;

    @Autowired
    private IPDMedicationAdministrationService ipdMedicationAdministrationService;

    @Autowired
    private MedicationAdministrationFhirRequestMapper requestMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationAdministration.class;
    }

    @Read
    public MedicationAdministration getMedicationAdministrationByUuid(@IdParam IdType id) {
        MedicationAdministration medicationAdministration = service.get(id.getIdPart());
        if (medicationAdministration == null) {
            throw new ResourceNotFoundException("Could not find MedicationAdministration with Id " + id.getIdPart());
        }
        return applyCategory(medicationAdministration);
    }

    @Create
    public MethodOutcome createMedicationAdministration(@ResourceParam MedicationAdministration resource) {
        if (resource == null) {
            throw new InvalidRequestException("A MedicationAdministration resource is required");
        }
        if (!resource.hasStatus()) {
            throw new InvalidRequestException("MedicationAdministration.status is required");
        }
        if (!resource.hasSubject() || !resource.getSubject().hasReference()) {
            throw new InvalidRequestException("MedicationAdministration.subject (Patient/<uuid>) is required");
        }
        MedicationAdministrationRequest request = requestMapper.toRequest(resource);
        boolean emergency = requestMapper.isEmergencyCategory(resource);
        MedicationAdministration created;
        if (emergency) {
            requirePrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS);
            if (!resource.hasMedicationReference()) {
                throw new InvalidRequestException("MedicationAdministration.medicationReference (Medication/<drugUuid>) is required for category=emergency");
            }
            if (request.getProviders() == null || request.getProviders().isEmpty()
                    || request.getProviders().get(0).getProviderUuid() == null) {
                throw new InvalidRequestException("MedicationAdministration.performer.actor (Practitioner/<uuid>) is required for category=emergency");
            }
            created = callService(() -> ipdMedicationAdministrationService.saveAdhocMedicationAdministration(request));
        } else {
            requirePrivilege(PrivilegeConstants.EDIT_MEDICATION_ADMINISTRATION);
            if (request.getSlotUuid() == null) {
                throw new InvalidRequestException("Extension " + IPDFhirConstants.EXT_MEDICATION_ADMIN_SLOT
                        + " (valueString = slot uuid) is required for a scheduled administration");
            }
            created = callService(() -> ipdMedicationAdministrationService.saveScheduledMedicationAdministration(request));
        }
        return FhirProviderUtils.buildCreate(applyCategory(created));
    }

    /**
     * PUT is restricted to adhoc (category=emergency) administrations and keeps the legacy
     * semantics of {@code PUT /ipd/adhocMedicationAdministrations/{uuid}}: performers and notes in the
     * body are appended to the stored administration; other fields are not modified.
     * Deliberate difference from the REST endpoint: that one has no privilege check; this one requires
     * the same privilege as adhoc create (EDIT_ADHOC_MEDICATION_TASKS) so access is not widened.
     */
    @Update
    public MethodOutcome updateMedicationAdministration(@IdParam IdType id, @ResourceParam MedicationAdministration resource) {
        if (id == null || id.getIdPart() == null) {
            throw new InvalidRequestException("id must be specified to update a MedicationAdministration");
        }
        if (resource == null) {
            throw new InvalidRequestException("A MedicationAdministration resource is required");
        }
        if (resource.hasIdElement() && resource.getIdElement().hasIdPart()
                && !id.getIdPart().equals(resource.getIdElement().getIdPart())) {
            throw new InvalidRequestException("Resource id in the body does not match the id in the URL");
        }
        requirePrivilege(PrivilegeConstants.EDIT_ADHOC_MEDICATION_TASKS);
        MedicationAdministration existing = service.get(id.getIdPart());
        if (existing == null) {
            throw new ResourceNotFoundException("Could not find MedicationAdministration with Id " + id.getIdPart());
        }
        if (!isEmergency(existing)) {
            throw new InvalidRequestException("Only adhoc (category=emergency) medication administrations can be updated");
        }
        MedicationAdministrationRequest request = requestMapper.toRequest(resource);
        MedicationAdministration updated = callService(
                () -> ipdMedicationAdministrationService.updateAdhocMedicationAdministration(id.getIdPart(), request));
        return FhirProviderUtils.buildUpdate(applyCategory(updated));
    }

    /**
     * JSON Patch limited to appending amendment notes: {@code [{"op":"add","path":"/note/-","value":<Annotation>}]}.
     * Each note goes through the existing amendNote logic (lock check, server-computed previousNote,
     * statusReason from the amendment-reason extension, author from authorReference). A client-sent
     * previous-note-id is validated against the server's latest note (409 when stale), never trusted.
     */
    @Patch
    public MethodOutcome patchMedicationAdministration(@IdParam IdType id, PatchTypeEnum patchType,
            @ResourceParam String body, RequestDetails requestDetails) {
        if (id == null || id.getIdPart() == null) {
            throw new InvalidRequestException("id must be specified to patch a MedicationAdministration");
        }
        if (patchType != PatchTypeEnum.JSON_PATCH) {
            throw new InvalidRequestException("Only JSON Patch (application/json-patch+json) is supported");
        }
        requirePrivilege(PrivilegeConstants.EDIT_MEDICATION_ADMINISTRATION);
        if (service.get(id.getIdPart()) == null) {
            throw new ResourceNotFoundException("Could not find MedicationAdministration with Id " + id.getIdPart());
        }
        List<MedicationAdministrationNoteRequest> noteRequests = parseNoteAppendOperations(body);
        for (int i = 0; i < noteRequests.size(); i++) {
            MedicationAdministrationNoteRequest noteRequest = noteRequests.get(i);
            try {
                ipdMedicationAdministrationService.amendNote(id.getIdPart(), noteRequest);
            } catch (AmendmentConflictException e) {
                throw new ResourceVersionConflictException(e.getMessage());
            } catch (APIException e) {
                throw new InvalidRequestException(e.getMessage());
            }
        }
        MedicationAdministration updated = service.get(id.getIdPart());
        return FhirProviderUtils.buildPatch(applyCategory(updated));
    }

    private List<MedicationAdministrationNoteRequest> parseNoteAppendOperations(String body) {
        JsonNode operations;
        try {
            operations = OBJECT_MAPPER.readTree(body);
        }
        catch (IOException | RuntimeException e) {
            throw new InvalidRequestException("Patch body is not valid JSON: " + e.getMessage());
        }
        if (operations == null || !operations.isArray() || operations.size() == 0) {
            throw new InvalidRequestException("Patch body must be a non-empty JSON Patch array");
        }
        List<MedicationAdministrationNoteRequest> result = new ArrayList<>();
        for (JsonNode operation : operations) {
            String op = operation.path("op").asText();
            String path = operation.path("path").asText();
            if (!"add".equals(op) || !"/note/-".equals(path)) {
                throw new InvalidRequestException("Unsupported patch operation '" + op + " " + path
                        + "'; only 'add' on '/note/-' is supported");
            }
            JsonNode value = operation.get("value");
            if (value == null || !value.isObject()) {
                throw new InvalidRequestException("Patch operation 'add /note/-' requires an Annotation as value");
            }
            Annotation annotation = parseAnnotation(value);
            if (annotation.getText() == null || annotation.getText().trim().isEmpty()) {
                throw new InvalidRequestException("Note text is required");
            }
            if (!annotation.hasAuthorReference() || !annotation.getAuthorReference().hasReference()) {
                throw new InvalidRequestException("Note authorReference (Practitioner/<uuid>) is required");
            }
            MedicationAdministrationNoteRequest noteRequest = requestMapper.toNoteRequest(annotation);
            if (!result.isEmpty()) {
                // later notes of the same patch chain onto the note appended just before them
                noteRequest = withoutPreviousNote(noteRequest);
            }
            result.add(noteRequest);
        }
        return result;
    }

    private MedicationAdministrationNoteRequest withoutPreviousNote(MedicationAdministrationNoteRequest note) {
        return MedicationAdministrationNoteRequest.builder().uuid(note.getUuid()).authorUuid(note.getAuthorUuid())
                .recordedTime(note.getRecordedTime()).text(note.getText()).statusReasonUuid(note.getStatusReasonUuid())
                .build();
    }

    /** An Annotation is a datatype, which the HAPI parser can only read inside a resource, so wrap it. */
    private Annotation parseAnnotation(JsonNode annotationJson) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode wrapper = OBJECT_MAPPER.createObjectNode();
            wrapper.put("resourceType", "MedicationAdministration");
            wrapper.putArray("note").add(annotationJson);
            MedicationAdministration parsed = ca.uhn.fhir.context.FhirContext.forR4Cached().newJsonParser()
                    .parseResource(MedicationAdministration.class, wrapper.toString());
            return parsed.getNoteFirstRep();
        }
        catch (RuntimeException e) {
            throw new InvalidRequestException("Invalid Annotation in patch value: " + e.getMessage());
        }
    }

    private <T> T callService(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        }
        catch (AmendmentConflictException e) {
            throw new ResourceVersionConflictException(e.getMessage());
        }
        catch (APIException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    /** Package-private so tests can stub it without PowerMock. */
    boolean hasPrivilege(String privilege) {
        return Context.getUserContext().hasPrivilege(privilege);
    }

    private void requirePrivilege(String privilege) {
        if (!hasPrivilege(privilege)) {
            throw new ForbiddenOperationException("User doesn't have the following privilege: " + privilege);
        }
    }

    /**
     * An administration is adhoc/emergency when it has no MedicationRequest ({@code request}); that is
     * exactly the legacy rule behind Slot ServiceType EMERGENCY_MEDICATION_REQUEST (drugOrder == null),
     * so no Slot lookup is needed.
     */
    private boolean isEmergency(MedicationAdministration resource) {
        return !resource.hasRequest();
    }

    /** Sets the derived category (emergency) unless the resource already carries one. */
    MedicationAdministration applyCategory(MedicationAdministration resource) {
        if (resource != null && !resource.hasCategory() && isEmergency(resource)) {
            CodeableConcept category = new CodeableConcept();
            category.addCoding().setCode(IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY);
            resource.setCategory(category);
        }
        return resource;
    }

    @Search
    public IBundleProvider searchMedicationAdministrations(
            @OptionalParam(name = MedicationAdministration.SP_PATIENT, chainWhitelist = { "" }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
            @OptionalParam(name = MedicationAdministration.SP_CONTEXT, chainWhitelist = { "" }, targetTypes = Encounter.class) ReferenceAndListParam contextReference,
            @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenAndListParam status,
            @OptionalParam(name = IPDFhirConstants.SP_MEDICATION_ADMIN_CATEGORY) TokenAndListParam category) {

        if (category != null) {
            validateEmergencyCategory(category);

            // visitUuid is passed via the 'context' search parameter — Bahmni convention where visit maps to Encounter identifier
            String visitUuid = extractVisitUuid(contextReference);
            if (visitUuid == null || visitUuid.isEmpty()) {
                throw new InvalidRequestException("The 'context' parameter with a visit uuid is required when category=emergency");
            }

            List<Slot> slots = ipdVisitService.getMedicationSlots(visitUuid, ServiceType.EMERGENCY_MEDICATION_REQUEST);

            List<String> maUuids = slots.stream()
                    .map(Slot::getMedicationAdministration)
                    .filter(Objects::nonNull)
                    .map(org.openmrs.module.ipd.api.model.MedicationAdministration::getUuid)
                    .collect(Collectors.toList());

            if (maUuids.isEmpty()) {
                return new SimpleBundleProvider(Collections.emptyList());
            }

            List<MedicationAdministration> resources = service.get(maUuids);

            List<MedicationAdministration> filtered = resources.stream()
                    .filter(r -> r.getStatus() != null && matchesStatusFilter(r.getStatus().toCode(), status))
                    .filter(r -> matchesPatientFilter(r, patientReference))
                    .map(this::applyCategory)
                    .collect(Collectors.toList());

            return new SimpleBundleProvider((List<IBaseResource>) (List<?>) filtered);
        }

        MedicationAdministrationSearchParams params = new MedicationAdministrationSearchParams();
        params.setPatientReference(patientReference);
        params.setEncounterReference(contextReference);
        params.setStatus(status);
        return new CategoryDecoratingBundleProvider(service.searchForMedicationAdministration(params), this::applyCategory);
    }

    private void validateEmergencyCategory(TokenAndListParam category) {
        for (TokenOrListParam orList : category.getValuesAsQueryTokens()) {
            for (TokenParam token : orList.getValuesAsQueryTokens()) {
                if (!IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY.equals(token.getValue())) {
                    throw new InvalidRequestException("Unsupported category '" + token.getValue()
                            + "'; only '" + IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY + "' is supported");
                }
            }
        }
    }

    private String extractVisitUuid(ReferenceAndListParam contextReference) {
        if (contextReference == null) {
            return null;
        }
        List<ReferenceOrListParam> contextOrLists = contextReference.getValuesAsQueryTokens();
        if (contextOrLists != null && !contextOrLists.isEmpty()) {
            List<ReferenceParam> contextParams = contextOrLists.get(0).getValuesAsQueryTokens();
            if (contextParams != null && !contextParams.isEmpty()) {
                return contextParams.get(0).getIdPart();
            }
        }
        return null;
    }

    private boolean matchesStatusFilter(String code, TokenAndListParam filter) {
        if (filter == null) {
            return true;
        }
        for (TokenOrListParam orList : filter.getValuesAsQueryTokens()) {
            boolean anyMatch = false;
            for (TokenParam token : orList.getValuesAsQueryTokens()) {
                if (code.equals(token.getValue())) {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesPatientFilter(MedicationAdministration resource, ReferenceAndListParam filter) {
        if (filter == null) {
            return true;
        }
        String subjectId = resource.hasSubject() ? resource.getSubject().getReferenceElement().getIdPart() : null;
        for (ReferenceOrListParam orList : filter.getValuesAsQueryTokens()) {
            boolean anyMatch = false;
            for (ReferenceParam param : orList.getValuesAsQueryTokens()) {
                if (subjectId != null && subjectId.equals(param.getIdPart())) {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch) {
                return false;
            }
        }
        return true;
    }
}
