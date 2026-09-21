package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class WardServiceImpl implements WardService {

    private WardDAO wardDAO;
    private LocationService locationService;
    private ProviderService providerService;

    @Autowired
    public WardServiceImpl(WardDAO wardDAO, LocationService locationService, ProviderService providerService) {
        this.wardDAO = wardDAO;
        this.locationService = locationService;
        this.providerService = providerService;
    }

    public void setWardDAO(WardDAO wardDAO) {
        this.wardDAO = wardDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Override
    public WardPatientsSummary getIPDWardPatientSummary(String wardUuid, String providerUuid) {
        Location location = locationService.getLocationByUuid(wardUuid);
        Provider provider = providerService.getProviderByUuid(providerUuid);
        Date currentDateTime = new Date();
        return wardDAO.getWardPatientSummary(location, provider, currentDateTime);
    }

    @Override
    public List<AdmittedPatient> getWardPatientsByUuid(String wardUuid, String sortBy) {
        Location location = locationService.getLocationByUuid(wardUuid);
        return wardDAO.getAdmittedPatients(location, null, null, sortBy);
    }

    @Override
    public List<AdmittedPatient> getPatientsByWardAndProvider(String wardUuid, String providerUuid, String sortBy) {
        Location location = locationService.getLocationByUuid(wardUuid);
        Provider provider = providerService.getProviderByUuid(providerUuid);
        Date currentDateTime = new Date();
        return wardDAO.getAdmittedPatients(location, provider, currentDateTime, sortBy);
    }

    @Override
    public List<AdmittedPatient> searchWardPatients(String wardUuid, List<String> searchKeys, String searchValue, String sortBy) {
        Location location = locationService.getLocationByUuid(wardUuid);
        return wardDAO.searchAdmittedPatients(location, searchKeys, searchValue, sortBy);
    }

    @Override
    public List<AdmittedPatient> getAdmittedPatients() {
        return wardDAO.getAdmittedPatients(null, null, null, null);
    }
}
