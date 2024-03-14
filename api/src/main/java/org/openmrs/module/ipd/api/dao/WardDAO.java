package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.time.LocalDateTime;
import java.util.List;

public interface WardDAO {

    List<AdmittedPatient> searchAdmittedPatients(Location location,List<String> searchKeys,String searchValue);

    List<AdmittedPatient> getAdmittedPatients(Location location, Provider provider, LocalDateTime currentDateTime);

    WardPatientsSummary getWardPatientSummary(Location location);

}
