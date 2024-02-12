package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.module.ipd.api.model.AdmittedPatient;

import java.util.List;

public interface WardDAO {

    List<AdmittedPatient> getAdmittedPatients(Location location);

}
