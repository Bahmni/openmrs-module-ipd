package org.openmrs.module.ipd.service.impl;


import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.openmrs.module.ipd.service.IPDWardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<AdmittedPatient> getIPDPatientByWard(String wardUuid, Integer offset, Integer limit) {
        return wardService.getWardPatientsByUuid(wardUuid,offset,limit);
    }

    @Override
    public List<AdmittedPatient> searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue, Integer offset, Integer limit) {
        return wardService.searchWardPatients(wardUuid,searchKeys,searchValue,offset,limit);
    }
}
