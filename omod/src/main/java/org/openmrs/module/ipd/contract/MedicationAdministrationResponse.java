package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;


import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationResponse {

    private String uuid;
    private String notes;
    private Date administeredDateTime;
    private String status;
    private String orderUuid;
    private String patientUuid;
    private Object provider;

    public static MedicationAdministrationResponse createFrom(org.openmrs.module.fhir2.model.MedicationAdministration medicationAdministration) {
        if (medicationAdministration == null) {
            return null;
        }
        String patientUuid = null;
        if (medicationAdministration.getPatient() != null) {
            patientUuid = medicationAdministration.getPatient().getUuid();
        }
        return MedicationAdministrationResponse.builder()
                .uuid(medicationAdministration.getUuid())
                .notes(medicationAdministration.getNotes())
                .administeredDateTime(medicationAdministration.getAdministeredDateTime())
                .status(medicationAdministration.getStatus().getShortNameInLocale(Context.getLocale()).getName())
                // .orderUuid(medicationAdministration.getRequest().getReference())
                .patientUuid(patientUuid)
                .provider(ConversionUtil.convertToRepresentation(medicationAdministration.getAdminister(), Representation.REF))
                .build();
    }
}

