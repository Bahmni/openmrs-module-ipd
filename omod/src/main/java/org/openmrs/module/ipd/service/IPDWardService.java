package org.openmrs.module.ipd.service;

import org.openmrs.module.bedmanagement.AdmissionLocation;
import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;
import org.openmrs.module.ipd.api.model.PatientStats;

import java.util.List;

public interface IPDWardService {

    PatientStats getIPDWardsStats();

    IPDWardPatientDetails getIPDPatientByWard(String wardUuid,Integer offset,Integer limit);


}
