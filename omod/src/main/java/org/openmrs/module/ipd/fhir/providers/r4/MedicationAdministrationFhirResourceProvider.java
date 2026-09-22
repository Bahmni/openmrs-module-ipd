package org.openmrs.module.ipd.fhir.providers.r4;

import static lombok.AccessLevel.PACKAGE;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.ReferenceOrListParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.search.param.MedicationAdministrationSearchParams;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.fhir.IPDFhirConstants;
import org.openmrs.module.ipd.web.service.IPDVisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("medicationAdministrationFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class MedicationAdministrationFhirResourceProvider implements IResourceProvider {

    private static final Logger log = LoggerFactory.getLogger(MedicationAdministrationFhirResourceProvider.class);

    @Autowired
    private FhirMedicationAdministrationService service;

    @Autowired
    private IPDVisitService ipdVisitService;

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
        return medicationAdministration;
    }

    @Search
    public IBundleProvider searchMedicationAdministrations(
            @OptionalParam(name = MedicationAdministration.SP_PATIENT, chainWhitelist = { "" }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
            @OptionalParam(name = MedicationAdministration.SP_CONTEXT, chainWhitelist = { "" }, targetTypes = Encounter.class) ReferenceAndListParam contextReference,
            @OptionalParam(name = MedicationAdministration.SP_STATUS) TokenAndListParam status,
            @OptionalParam(name = IPDFhirConstants.SP_MEDICATION_ADMIN_CATEGORY) TokenAndListParam category) {

        boolean emergencyOnly = false;
        if (category != null) {
            outer:
            for (TokenOrListParam orList : category.getValuesAsQueryTokens()) {
                for (TokenParam token : orList.getValuesAsQueryTokens()) {
                    if (IPDFhirConstants.MEDICATION_ADMIN_CATEGORY_EMERGENCY.equals(token.getValue())) {
                        emergencyOnly = true;
                        break outer;
                    }
                }
            }
        }

        if (emergencyOnly && contextReference != null) {
            // visitUuid is passed via the 'context' search parameter — Bahmni convention where visit maps to Encounter identifier
            String visitUuid = null;
            List<ReferenceOrListParam> contextOrLists = contextReference.getValuesAsQueryTokens();
            if (contextOrLists != null && !contextOrLists.isEmpty()) {
                List<ReferenceParam> contextParams = contextOrLists.get(0).getValuesAsQueryTokens();
                if (contextParams != null && !contextParams.isEmpty()) {
                    visitUuid = contextParams.get(0).getIdPart();
                }
            }

            if (visitUuid == null || visitUuid.isEmpty()) {
                log.warn("Emergency category search requested but no valid visitUuid found in context reference; falling through to unfiltered search");
            } else {
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
                        .collect(Collectors.toList());

                return new SimpleBundleProvider((List<IBaseResource>) (List<?>) filtered);
            }
        }

        MedicationAdministrationSearchParams params = new MedicationAdministrationSearchParams();
        params.setPatientReference(patientReference);
        params.setEncounterReference(contextReference);
        params.setStatus(status);
        return service.searchForMedicationAdministration(params);
    }

    private boolean matchesStatusFilter(String code, TokenAndListParam filter) {
        if (filter == null) {
            return true;
        }
        for (TokenOrListParam orList : filter.getValuesAsQueryTokens()) {
            for (TokenParam token : orList.getValuesAsQueryTokens()) {
                if (code.equals(token.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}
