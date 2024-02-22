package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;

import java.util.List;

public interface WardDAO {

    List<AdmittedPatient> searchAdmittedPatients(Location location,List<String> searchKeys,String searchValue,Integer offset,Integer limit);

    List<AdmittedPatient> getAdmittedPatientsByLocation(Location location,Integer offset,Integer limit);

    WardPatientsSummary getWardPatientSummary(Location location);

}
