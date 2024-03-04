package org.openmrs.module.ipd.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Slot;
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
    private long endTime;
    private Object order;
    private Object medicationAdministration;
    private String notes;

    public static MedicationSlotResponse createFrom(Slot slot) {
        MedicationSlotResponseBuilder builder =  MedicationSlotResponse.builder()
                .id(slot.getId())
                .uuid(slot.getUuid())
                .serviceType(slot.getServiceType().getName().getName())
                .status(slot.getStatus().name())
                .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                .order(ConversionUtil.convertToRepresentation(slot.getOrder(), Representation.FULL))
                .medicationAdministration(MedicationAdministrationResponse.createFrom((slot.getMedicationAdministration())))
                .notes(slot.getNotes());
        if(slot.getEndDateTime() != null) {
            builder.endTime(convertLocalDateTimeToUTCEpoc(slot.getEndDateTime()));
        }
        return builder.build();
    }

    public static MedicationSlotResponse createFrom(Slot slot, Representation rep) {
        if (rep.equals(Representation.REF))
        {
            MedicationSlotResponseBuilder builder = MedicationSlotResponse.builder()
                    .id(slot.getId())
                    .uuid(slot.getUuid())
                    .serviceType(slot.getServiceType().getName().getName())
                    .status(slot.getStatus().name())
                    .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                    .medicationAdministration(MedicationAdministrationResponse.createFrom((slot.getMedicationAdministration())))
                    .notes(slot.getNotes());
            if(slot.getEndDateTime() != null) {
                builder.endTime(convertLocalDateTimeToUTCEpoc(slot.getEndDateTime()));
            }
            return builder.build();
        }
        return MedicationSlotResponse.createFrom(slot);

    }
}
