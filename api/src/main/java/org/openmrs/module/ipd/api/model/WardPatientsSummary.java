package org.openmrs.module.ipd.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PatientStats {
    private Integer totalPatients = 0;

    // to be added in future
//    private Integer myPatients;
//    private Integer toBeDischargedPatients;
}
