package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WardServiceImpl implements WardService {

    private final WardDAO wardDAO;

    @Autowired
    public WardServiceImpl(WardDAO wardDAO) {
        this.wardDAO = wardDAO;
    }


    @Override
    public WardPatientsSummary getIPDWardPatientSummary(String wardUuid) {
        Location location= Context.getService(LocationService.class).getLocationByUuid(wardUuid);
        return wardDAO.getWardPatientSummary(location);
    }

    @Override
    public List<AdmittedPatient> getWardPatientsByUuid(String wardUuid, String sortBy) {
        Location location= Context.getService(LocationService.class).getLocationByUuid(wardUuid);
        return wardDAO.getAdmittedPatientsByLocation(location, sortBy);
    }

    @Override
    public List<AdmittedPatient> searchWardPatients(String wardUuid, List<String> searchKeys, String searchValue, String sortBy) {
        Location location= Context.getService(LocationService.class).getLocationByUuid(wardUuid);
        return  wardDAO.searchAdmittedPatients(location,searchKeys,searchValue,sortBy);
    }

}
