package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;


public interface WardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    List<AdmittedPatient> getWardPatients(String wardUuid, String providerUUID);

    List<AdmittedPatient>  searchWardPatients(String wardUuid, List<String> searchKeys, String searchValue);

}
