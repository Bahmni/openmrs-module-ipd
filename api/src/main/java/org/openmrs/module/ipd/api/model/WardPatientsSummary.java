package org.openmrs.module.ipd.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class WardPatientsSummary {
    private Long totalPatients = 0L;
    private Long totalProviderPatients = 0L;

    // to be added in future
//    private Integer toBeDischargedPatients;
}
