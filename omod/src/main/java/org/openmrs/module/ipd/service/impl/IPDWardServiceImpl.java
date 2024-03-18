package org.openmrs.module.ipd.service.impl;


import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.IPDPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.openmrs.module.ipd.service.IPDWardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IPDWardServiceImpl implements IPDWardService {

    private final WardService wardService;

    @Autowired
    public IPDWardServiceImpl(WardService wardService) {
        this.wardService = wardService;
    }


    @Override
    public WardPatientsSummary getIPDWardPatientSummary(String wardUuid) {
        return wardService.getIPDWardPatientSummary(wardUuid);
    }

    @Override
    public IPDPatientDetails getIPDPatientByWard(String wardUuid, Integer offset, Integer limit, String sortBy) {

        List<AdmittedPatient> admittedPatients = wardService.getWardPatientsByUuid(wardUuid, sortBy);

        if (admittedPatients ==null ){
            return new IPDPatientDetails(new ArrayList<>(),0);
        }

        offset = Math.min(offset, admittedPatients.size());
        limit = Math.min(limit, admittedPatients.size() - offset);

        return new IPDPatientDetails(admittedPatients.subList(offset, offset + limit), admittedPatients.size());
    }

    @Override
    public IPDPatientDetails searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue,
                                                     Integer offset, Integer limit, String sortBy) {

        List<AdmittedPatient> admittedPatients = wardService.searchWardPatients(wardUuid,searchKeys,searchValue,sortBy);
        if (admittedPatients ==null ){
            return new IPDPatientDetails(new ArrayList<>(),0);
        }

        offset = Math.min(offset, admittedPatients.size());
        limit = Math.min(limit, admittedPatients.size() - offset);

        return new IPDPatientDetails(admittedPatients.subList(offset, offset + limit), admittedPatients.size());
    }
}
