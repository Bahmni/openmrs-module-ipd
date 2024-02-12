package org.openmrs.module.ipd.service;

import org.openmrs.module.bedmanagement.AdmissionLocation;
import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;

import java.util.List;

public interface IPDWardService {

    List<AdmissionLocation> getIPDWards();

    IPDWardPatientDetails getIPDPatientByWard(String wardUuid,Integer offset,Integer limit);


}
