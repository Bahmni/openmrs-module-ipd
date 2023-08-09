package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.contract.MedicationScheduleResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.model.ServiceType.MEDICATION_REQUEST;
import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertEpocUTCToLocalTimeZone;
import static org.openmrs.module.ipd.contract.MedicationScheduleResponse.createFrom;
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
    public ResponseEntity<Object> createMedicationSchedule(@RequestBody ScheduleMedicationRequest scheduleMedicationRequest) {
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
    public ResponseEntity<Object> getMedicationScheduleByDate(@RequestParam(value = "patientUuid") String patientUuid,
                                                              @RequestParam(value = "forDate") long forDate) {
        try {
            LocalDate localDate = convertEpocUTCToLocalTimeZone(forDate).toLocalDate();
            List<Slot> slots = ipdScheduleService.getMedicationSlots(patientUuid, MEDICATION_REQUEST, localDate);
            return new ResponseEntity<>(constructResponse(slots), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "type/medication", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getMedicationScheduleByOrderUuids(@RequestParam(value = "patientUuid") String patientUuid,
                                                                    @RequestParam(value = "orderUuids", required = false) List<String> orderUuids) {
        try {
            List<Schedule> schedules;
            if (orderUuids == null || orderUuids.isEmpty()) {
                schedules = ipdScheduleService.getMedicationSchedules(patientUuid, MEDICATION_REQUEST);
            } else {
                schedules = ipdScheduleService.getMedicationSchedules(patientUuid, MEDICATION_REQUEST, orderUuids);
            }
            List<ScheduleMedicationResponse> medicationResponses = schedules.stream()
                    .map(ScheduleMedicationResponse::constructFrom)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(medicationResponses, OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to retrieve schedules created by patient", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }


    private List<MedicationScheduleResponse> constructResponse(List<Slot> slots) {
        Map<Schedule, List<Slot>> slotsBySchedule = slots.stream().collect(Collectors.groupingBy(Slot::getSchedule));
        return slotsBySchedule.entrySet().stream().map(entry -> createFrom(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
}