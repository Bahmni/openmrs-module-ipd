package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.contract.ScheduleMedicationResponse;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd")
@Slf4j
public class IPDMedicationAdministrationController extends BaseRestController {

    private final IPDMedicationAdministrationService ipdMedicationAdministrationService;

    @Autowired
    public IPDMedicationAdministrationController(IPDMedicationAdministrationService ipdMedicationAdministrationService) {
        this.ipdMedicationAdministrationService = ipdMedicationAdministrationService;
    }


    @RequestMapping(value = "/medicationAdministration", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createMedicationAdministration(@RequestBody MedicationAdministrationRequest medicationAdministrationRequest) {
        try {
            MedicationAdministrationResponse medicationAdministrationResponse = ipdMedicationAdministrationService.saveMedicationAdministration(medicationAdministrationRequest);
            return new ResponseEntity<>(medicationAdministrationResponse, OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }
}
