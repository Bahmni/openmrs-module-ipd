package org.openmrs.module.ipd.contract;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.ipd.api.model.PatientStats;

@Getter
@Setter
@Builder
public class PatientStatsResponse {

    private Integer totalPatients;


    public static PatientStatsResponse createFrom(PatientStats patientStats){
        return PatientStatsResponse.builder().
                totalPatients(patientStats.getTotalPatients()).
                build();
    }
}
