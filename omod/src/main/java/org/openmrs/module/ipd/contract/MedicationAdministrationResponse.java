package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.factory.SlotFactory;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationResponse {

    private String uuid;
    private String notes;
    private Date effectiveDateTime;
    private String status;
   // private String slotUuid;
    private String orderUuid;
    private String patientUuid;
    private String providerUuid;

    public static MedicationAdministrationResponse createFrom(org.openmrs.module.fhir2.model.MedicationAdministration medicationAdministration) {
        if (medicationAdministration == null) {
            return null;
        }
        String patientUuid = null;
        String providerUuid = null;
//        String slotUuid = medicationAdministration.getSupportingInformation().get(0).getReference().split("/")[1];
//        Slot slot = slotService.getSlotByUUID(slotUuid);
        if (medicationAdministration.getPatient() != null) {
            patientUuid = medicationAdministration.getPatient().getUuid();
        }
        if (medicationAdministration.getAdminister() != null) {
            providerUuid = medicationAdministration.getAdminister().getUuid();
        }
        return MedicationAdministrationResponse.builder()
                .uuid(medicationAdministration.getUuid())
                .notes(medicationAdministration.getNotes())
                .effectiveDateTime(medicationAdministration.getAdministeredDateTime())
                .status(medicationAdministration.getStatus().getDisplayString())
                // .orderUuid(medicationAdministration.getRequest().getReference())
                // .slot(MedicationSlotResponse.createFrom(slot))
                .patientUuid(patientUuid)
                .providerUuid(providerUuid)
                .build();
    }
}

