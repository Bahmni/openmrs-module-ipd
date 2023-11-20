package org.openmrs.module.ipd.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationScheduleResponse {

    private Integer id;
    private String uuid;
    private String serviceType;
    private String comments;
    private long startDate;
    private long endDate;
    private List<MedicationSlot> slots;
    public static MedicationScheduleResponse createFrom(Schedule schedule, List<Slot> slots) {

        return MedicationScheduleResponse.builder()
                .id(schedule.getId())
                .uuid(schedule.getUuid())
                .serviceType(schedule.getServiceType().getName().getName())
                .comments(schedule.getComments())
                .startDate(convertLocalDateTimeToUTCEpoc(schedule.getStartDate()))
                .endDate(convertLocalDateTimeToUTCEpoc(schedule.getEndDate()))
                .slots(slots.stream().map(MedicationSlot::createFrom).collect(Collectors.toList()))
                .build();

    }

    @Builder
    @Getter
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @AllArgsConstructor
    @NoArgsConstructor
    static class MedicationSlot {
        private Integer id;
        private String uuid;
        private String serviceType;
        private String status;
        private long startTime;
        private Object order;
        public static MedicationSlot createFrom(Slot slot) {
            return MedicationSlot.builder()
                    .id(slot.getId())
                    .uuid(slot.getUuid())
                    .serviceType(slot.getServiceType().getName().getName())
                    .status(slot.getStatus().name())
                    .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                    .order(ConversionUtil.convertToRepresentation(slot.getOrder(), Representation.FULL))
                    .build();
        }
    }
}