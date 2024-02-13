package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Location;
import org.openmrs.module.bedmanagement.AdmissionLocation;
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
import java.util.*;
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
    public ResponseEntity<Object> getIPDWardStats () throws ParseException {
        PatientStats patientStats= ipdWardService.getIPDWardsStats();
        return new ResponseEntity<>(PatientStatsResponse.createFrom(patientStats), OK);
    }

    @RequestMapping(value = "{wardUuid}/patients", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> createMedicationSchedule(@PathVariable("wardUuid") String wardUuid,
                                                           @RequestParam(value = "offset") Integer offset,
                                                           @RequestParam (value = "limit") Integer limit) throws ParseException {
        try {
            IPDWardPatientDetails wardPatientDetails = ipdWardService.getIPDPatientByWard(wardUuid,offset,limit);
            return new ResponseEntity<>(IPDWardPatientDetailsResponse.createFrom(wardPatientDetails), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

}
