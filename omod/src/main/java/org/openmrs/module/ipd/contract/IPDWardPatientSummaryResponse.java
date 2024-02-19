package org.openmrs.module.ipd.contract;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

@Getter
@Setter
@Builder
public class IPDWardPatientSummaryResponse {

    private Long totalPatients;

    public static IPDWardPatientSummaryResponse createFrom(WardPatientsSummary wardPatientsSummary){
        return IPDWardPatientSummaryResponse.builder().
                totalPatients(wardPatientsSummary.getTotalPatients()).
                build();
    }
}
