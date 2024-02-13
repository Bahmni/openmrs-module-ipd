package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

public interface IPDWardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    IPDWardPatientDetails getIPDPatientByWard(String wardUuid,Integer offset,Integer limit);


}
