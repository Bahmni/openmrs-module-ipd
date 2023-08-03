package org.openmrs.module.ipd.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Slot;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class MedicationSlot {
    private String uuid;
    private String serviceType;
    private String status;
    private LocalDateTime startTime;

    public static MedicationSlot createFrom(Slot slot) {
        return MedicationSlot.builder()
                .uuid(slot.getUuid())
                .serviceType(slot.getServiceType().getName().getName())
                .status(slot.getStatus().name())
                .startTime(slot.getStartDateTime())
                .build();
    }
}
