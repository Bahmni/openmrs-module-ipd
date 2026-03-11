package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Builder
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class MedicationSlotResponse {
    private Integer id;
    private String uuid;
    private String serviceType;
    private String status;
    private long startTime;
    private Object order;
    private Object medicationAdministration;
    private String notes;

    private static MedicationAdministration fetchMedicationAdministration(Integer id) {
        if (id == null) return null;
        return Context.getService(IPDMedicationAdministrationService.class).getMedicationAdministrationById(id);
    }

    public static MedicationSlotResponse createFrom(Slot slot) {
        return MedicationSlotResponse.builder()
                .id(slot.getId())
                .uuid(slot.getUuid())
                .serviceType(slot.getServiceType().getName().getName())
                .status(slot.getStatus().name())
                .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                .order(ConversionUtil.convertToRepresentation(slot.getOrder(), Representation.FULL))
                .medicationAdministration(MedicationAdministrationResponse.createFrom(fetchMedicationAdministration(slot.getMedicationAdministrationId())))
                .notes(slot.getNotes())
                .build();
    }

    public static MedicationSlotResponse createFrom(Slot slot, Representation rep) {
        if (rep.equals(Representation.REF))
        {
            return MedicationSlotResponse.builder()
                    .id(slot.getId())
                    .uuid(slot.getUuid())
                    .serviceType(slot.getServiceType().getName().getName())
                    .status(slot.getStatus().name())
                    .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                    .medicationAdministration(MedicationAdministrationResponse.createFrom(fetchMedicationAdministration(slot.getMedicationAdministrationId())))
                    .notes(slot.getNotes())
                    .build();
        }
        return MedicationSlotResponse.createFrom(slot);

    }
}
