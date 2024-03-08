package org.openmrs.module.ipd.service;


import org.openmrs.module.ipd.api.model.IPDPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;

public interface IPDWardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    IPDPatientDetails getIPDPatientByWard(String wardUuid, Integer offset, Integer limit, String providerUUID);

    IPDPatientDetails searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue, Integer offset, Integer limit);



}
