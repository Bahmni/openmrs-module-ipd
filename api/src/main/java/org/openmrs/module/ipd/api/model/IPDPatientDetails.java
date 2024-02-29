package org.openmrs.module.ipd.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class IPDPatientDetails  {

    private List<AdmittedPatient> admittedPatients;
    private Integer patientCount;

}