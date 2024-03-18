package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;


public interface WardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    List<AdmittedPatient> getWardPatientsByUuid(String wardUuid, String sortBy);

    List<AdmittedPatient>  searchWardPatients(String wardUuid, List<String> searchKeys, String searchValue, String sortBy);

}
