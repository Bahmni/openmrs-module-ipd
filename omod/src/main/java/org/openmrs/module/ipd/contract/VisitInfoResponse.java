package org.openmrs.module.ipd.contract;

import lombok.*;
import org.openmrs.module.bahmniemrapi.visit.contract.VisitData;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitInfoResponse {
    private String uuid;
    private Date startDateTime;

    public static VisitInfoResponse createFrom(VisitData visitData){
        return VisitInfoResponse.builder().
                uuid(visitData.getUuid()).
                startDateTime(visitData.getStartDateTime()).
                build();
    }
}
