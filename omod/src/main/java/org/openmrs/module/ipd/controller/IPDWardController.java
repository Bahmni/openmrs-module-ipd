package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.model.*;
import org.openmrs.module.ipd.contract.*;
import org.openmrs.module.ipd.service.IPDWardService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/wards")
@Slf4j
public class IPDWardController extends BaseRestController {

    private IPDWardService ipdWardService;

    @Autowired
    public IPDWardController(IPDWardService ipdWardService) {
        this.ipdWardService = ipdWardService;
    }

    @RequestMapping(value = "{wardUuid}/summary",method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getIPDWardPatientStats (@PathVariable("wardUuid") String wardUuid) throws ParseException {
        WardPatientsSummary wardPatientsSummary = ipdWardService.getIPDWardPatientSummary(wardUuid);
        return new ResponseEntity<>(IPDWardPatientSummaryResponse.createFrom(wardPatientsSummary), OK);
    }

    @RequestMapping(value = "{wardUuid}/patients", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getIPDWardPatient(@PathVariable("wardUuid") String wardUuid,
                                                           @RequestParam(value = "offset") Integer offset,
                                                           @RequestParam (value = "limit") Integer limit) throws ParseException {
        try {
            List<AdmittedPatient> admittedPatients = ipdWardService.getIPDPatientByWard(wardUuid,offset,limit);
            return new ResponseEntity<>(admittedPatients.stream().map(AdmittedPatientResponse::createFrom).collect(Collectors.toList()), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "{wardUuid}/patients/search", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> searchIPDWardPatient(@PathVariable("wardUuid") String wardUuid,
                                                    @RequestParam(value = "offset") Integer offset,
                                                    @RequestParam (value = "limit") Integer limit,
                                                       @RequestParam(value = "searchKeys") List<String> searchKeys,
                                                       @RequestParam(value = "searchValue") String searchValue) throws ParseException {
        try {
            List<AdmittedPatient> admittedPatients = ipdWardService.searchIPDPatientsInWard(wardUuid,searchKeys,searchValue,offset,limit);
            return new ResponseEntity<>(admittedPatients.stream().map(AdmittedPatientResponse::createFrom).collect(Collectors.toList()), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

}
