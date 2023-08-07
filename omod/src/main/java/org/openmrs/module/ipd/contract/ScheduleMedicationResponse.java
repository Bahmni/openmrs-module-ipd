package org.openmrs.module.ipd.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Schedule;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Builder
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMedicationResponse {
    private Integer id;
    private String patientUuid;
    private String comments;
    private long startDate;
    private long endDate;

    public static ScheduleMedicationResponse constructFrom(Schedule schedule) {
        return ScheduleMedicationResponse.builder()
            .id(schedule.getId())
            .patientUuid(schedule.getSubject().getUuid())
            .comments(schedule.getComments())
            .startDate(convertLocalDateTimeToUTCEpoc(schedule.getStartDate()))
            .endDate(convertLocalDateTimeToUTCEpoc(schedule.getEndDate()))
            .build();
    }
}
