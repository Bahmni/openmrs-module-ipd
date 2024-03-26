package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.Date;
import java.util.List;

public interface WardDAO {

    List<AdmittedPatient> searchAdmittedPatients(Location location, List<String> searchKeys, String searchValue, String sortBy);

    List<AdmittedPatient> getAdmittedPatients(Location location, Provider provider, Date currentDateTime, String sortBy);

    WardPatientsSummary getWardPatientSummary(Location location, Provider provider, Date currentDateTime);

}
