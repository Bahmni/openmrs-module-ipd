package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public IPDWardPatientDetails getWardPatientsByUuid(String wardUuid, Integer offset, Integer limit) {
        Location location= Context.getService(LocationService.class).getLocationByUuid(wardUuid);
        List<AdmittedPatient> activePatients= wardDAO.getAdmittedPatients(location);
        if (activePatients==null){
            return new IPDWardPatientDetails(new ArrayList<>(),new WardPatientsSummary());
        }
        offset = Math.min(offset, activePatients.size());
        limit = Math.min(limit, activePatients.size() - offset);

        WardPatientsSummary wardPatientsSummary = computePatientStats(activePatients);
        return new IPDWardPatientDetails(activePatients.subList(offset, offset + limit), wardPatientsSummary);
    }

    private WardPatientsSummary computePatientStats(List<AdmittedPatient> activePatients){
        WardPatientsSummary wardPatientsSummary =new WardPatientsSummary();
        wardPatientsSummary.setTotalPatients(activePatients !=null ? activePatients.size() : 0L);
        return wardPatientsSummary;
    }
}
