package org.openmrs.module.ipd.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PatientStats {
    private Integer totalPatients = 0;

    // to be added in future
//    private Integer myPatients;
//    private Integer toBeDischargedPatients;
}
