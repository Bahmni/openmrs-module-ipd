package org.openmrs.module.ipd.service.impl;


import org.openmrs.module.ipd.api.model.IPDWardPatientDetails;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.openmrs.module.ipd.api.service.WardService;
import org.openmrs.module.ipd.service.IPDWardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public IPDWardPatientDetails getIPDPatientByWard(String wardUuid,Integer offset,Integer limit) {
        return wardService.getWardPatientsByUuid(wardUuid,offset,limit);
    }
}
