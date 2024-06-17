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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class IPDWardServiceImpl implements IPDWardService {

    private final WardService wardService;

    @Autowired
    public IPDWardServiceImpl(WardService wardService) {
        this.wardService = wardService;
    }

    private static final Pattern numericPattern = Pattern.compile("^\\d+$");

    @Override
    public WardPatientsSummary getIPDWardPatientSummary(String wardUuid, String providerUuid) {
        return wardService.getIPDWardPatientSummary(wardUuid, providerUuid);
    }

    @Override
    public IPDPatientDetails getIPDPatientByWard(String wardUuid, Integer offset, Integer limit, String sortBy) {

        List<AdmittedPatient> admittedPatients = wardService.getWardPatientsByUuid(wardUuid,sortBy);

        if (admittedPatients ==null ){
            return new IPDPatientDetails(new ArrayList<>(),0);
        }

        offset = Math.min(offset, admittedPatients.size());
        limit = Math.min(limit, admittedPatients.size() - offset);

        return new IPDPatientDetails(admittedPatients.subList(offset, offset + limit), admittedPatients.size());
    }

    @Override
    public IPDPatientDetails getIPDPatientsByWardAndProvider(String wardUuid, String providerUuid, Integer offset, Integer limit, String sortBy) {

        List<AdmittedPatient> admittedPatients = wardService.getPatientsByWardAndProvider(wardUuid, providerUuid, sortBy);

        if (admittedPatients ==null ){
            return new IPDPatientDetails(new ArrayList<>(),0);
        }
        List<AdmittedPatient> admittedPatientsSortedList = sortBedNumbers(admittedPatients);

        offset = Math.min(offset, admittedPatientsSortedList.size());
        limit = Math.min(limit, admittedPatientsSortedList.size() - offset);

        return new IPDPatientDetails(admittedPatientsSortedList.subList(offset, offset + limit), admittedPatients.size());
    }

    @Override
    public IPDPatientDetails searchIPDPatientsInWard(String wardUuid, List<String> searchKeys, String searchValue,
                                                     Integer offset, Integer limit, String sortBy) {

        List<AdmittedPatient> admittedPatients = wardService.searchWardPatients(wardUuid,searchKeys,searchValue,sortBy);

        if (admittedPatients ==null ){
            return new IPDPatientDetails(new ArrayList<>(),0);
        }

        List<AdmittedPatient> admittedPatientsSortedList = sortBedNumbers(admittedPatients);

        offset = Math.min(offset, admittedPatientsSortedList.size());
        limit = Math.min(limit, admittedPatientsSortedList.size() - offset);

        return new IPDPatientDetails(admittedPatientsSortedList.subList(offset, offset + limit), admittedPatients.size());
    }

    private List<AdmittedPatient> sortBedNumbers(List<AdmittedPatient> admittedPatients) {
        Collections.sort(admittedPatients, (patientA, patientB) -> {
            String bedNumberA = patientA.getBedPatientAssignment().getBed().getBedNumber();
            String bedNumberB = patientB.getBedPatientAssignment().getBed().getBedNumber();

            boolean isNumericA = numericPattern.matcher(bedNumberA).matches();
            boolean isNumericB = numericPattern.matcher(bedNumberB).matches();

            if (isNumericA && isNumericB) {
                return Integer.compare(Integer.parseInt(bedNumberA), Integer.parseInt(bedNumberB));
            } else if (!isNumericA && !isNumericB) {
                return bedNumberA.compareTo(bedNumberB);
            } else {
                return isNumericA ? -1 : 1;
            }
        });

        return admittedPatients;

    }
}
