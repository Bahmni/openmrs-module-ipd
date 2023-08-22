package org.openmrs.module.ipd.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

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
    private Object order;

    public static ScheduleMedicationResponse constructFrom(Schedule schedule) {
        return ScheduleMedicationResponse.builder()
            .id(schedule.getId())
            .patientUuid(schedule.getSubject().getUuid())
            .comments(schedule.getComments())
            .order(ConversionUtil.convertToRepresentation(schedule.getOrder(), Representation.REF)) // TODO. Clarify why we need to use REF here with product team
            .startDate(convertLocalDateTimeToUTCEpoc(schedule.getStartDate()))
            .endDate(convertLocalDateTimeToUTCEpoc(schedule.getEndDate()))
            .build();
    }
}
