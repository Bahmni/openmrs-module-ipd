package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;

public interface IPDWardService {

    WardPatientsSummary getIPDWardPatientSummary(String wardUuid);

    List<AdmittedPatient> getIPDPatientByWard(String wardUuid, Integer offset, Integer limit);

    List<AdmittedPatient> searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue,Integer offset, Integer limit);



}
