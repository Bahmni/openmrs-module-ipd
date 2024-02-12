package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;


public interface WardService {
    
    IPDWardPatientDetails getWardPatientsByUuid(String wardUuid, Integer offset, Integer limit);
}
