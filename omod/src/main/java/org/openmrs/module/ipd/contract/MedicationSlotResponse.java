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
    private Object order;

    public static MedicationSlotResponse createFrom(Slot slot) {
        return MedicationSlotResponse.builder()
                .id(slot.getId())
                .uuid(slot.getUuid())
                .serviceType(slot.getServiceType().getName().getName())
                .status(slot.getStatus().name())
                .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                .order(ConversionUtil.convertToRepresentation(slot.getOrder(), Representation.FULL))
                .build();
    }
}
