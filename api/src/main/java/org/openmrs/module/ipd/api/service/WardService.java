package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;


public interface WardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    IPDWardPatientDetails getWardPatientsByUuid(String wardUuid, Integer offset, Integer limit);
}
