package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.contract.MedicationSlot;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.contract.ScheduleMedicationResponse;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/schedule")
@Slf4j
public class IPDScheduleController extends BaseRestController {

    private final IPDScheduleService ipdScheduleService;

    @Autowired
    public IPDScheduleController(IPDScheduleService ipdScheduleService) {
        this.ipdScheduleService = ipdScheduleService;
    }

    @RequestMapping(value = "type/medication", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createMedicationSchedule(@Valid @RequestBody ScheduleMedicationRequest scheduleMedicationRequest) {
        try {
            Schedule schedule = ipdScheduleService.saveMedicationSchedule(scheduleMedicationRequest);
            return new ResponseEntity<>(ScheduleMedicationResponse.constructFrom(schedule), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "type/medication", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getMedicationSchedule(@Valid @RequestParam(value = "patientUuid") String patientUuid,
                                                        @RequestParam(value = "serviceType") String serviceType,
                                                        @RequestParam(value = "forDate") long forDate) {
        try {
            LocalDate localDate = Instant.ofEpochSecond(forDate).atZone(ZoneOffset.systemDefault()).toLocalDate();
            List<Slot> medicationSlots = ipdScheduleService.getMedicationSlots(patientUuid, serviceType, localDate);
            List<MedicationSlot> slots = medicationSlots.stream().map(MedicationSlot::createFrom).collect(Collectors.toList());
            return new ResponseEntity<>(slots, OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }
}
