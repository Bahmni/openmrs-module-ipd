package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.api.util.IPDConstants;
import org.openmrs.module.ipd.contract.MedicationScheduleResponse;
import org.openmrs.module.ipd.contract.MedicationSlotResponse;
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

import java.time.LocalDateTime;
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

    @RequestMapping(value = "type/medication/edit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> updateMedicationSchedule(@RequestBody ScheduleMedicationRequest scheduleMedicationRequest) {
        try {
            Schedule schedule = ipdScheduleService.updateMedicationSchedule(scheduleMedicationRequest);
            return new ResponseEntity<>(ScheduleMedicationResponse.constructFrom(schedule), OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "type/medication", method = RequestMethod.GET, params = {"patientUuid", "startTime", "endTime"})
    @ResponseBody
    public ResponseEntity<Object> getMedicationSlotsByDate(@RequestParam(value = "patientUuid") String patientUuid,
                                                           @RequestParam(value = "startTime") Long startTime, @RequestParam(value = "endTime") Long endTime,
                                                           @RequestParam(value = "view", required = false) String view) {
        try {
            if (startTime != null && endTime != null) {
                LocalDateTime localStartDate = convertEpocUTCToLocalTimeZone(startTime);
                LocalDateTime localEndDate = convertEpocUTCToLocalTimeZone(endTime);
                List<Slot> slots = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(patientUuid, localStartDate, localEndDate,false);

                if (view!=null & IPDConstants.IPD_VIEW_DRUG_CHART.equals(view)){
                    List<Slot> slotsAdministered = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(patientUuid, localStartDate, localEndDate,true);
                    return new ResponseEntity<>(constructResponseDrugView(slots,slotsAdministered,startTime,endTime), OK);
                }
                return new ResponseEntity<>(constructResponse(slots), OK);
            }
            throw new Exception();
        } catch (Exception e) {
            log.error("Runtime error while trying to create new schedule", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "type/medication", method = RequestMethod.GET, params = {"patientUuid"})
    @ResponseBody
    public ResponseEntity<Object> getMedicationSlotsByOrderUuids(@RequestParam(value = "patientUuid") String patientUuid,
                                                                 @RequestParam(value = "orderUuids", required = false) List<String> orderUuids) {
        try {
            List<Slot> slots;
            if (orderUuids == null || orderUuids.isEmpty()) {
                slots = ipdScheduleService.getMedicationSlots(patientUuid, MEDICATION_REQUEST);
            } else {
                slots = ipdScheduleService.getMedicationSlots(patientUuid, MEDICATION_REQUEST, orderUuids);
            }
            List<MedicationSlotResponse> medicationResponses = slots.stream()
                    .map(MedicationSlotResponse::createFrom)
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

    private List<MedicationScheduleResponse> constructResponseDrugView(List<Slot> slots,List<Slot> slotsAdministered,Long startTime,Long endTime) {
        slotsAdministered.removeAll(slots);
        slots.addAll(slotsAdministered);
        Map<Schedule, List<Slot>> slotsBySchedule = slots.stream().collect(Collectors.groupingBy(Slot::getSchedule));
        return slotsBySchedule.entrySet().stream()
                .map(entry -> {
                    List<Slot> filteredList = entry.getValue().stream()
                            .filter(slot ->
                                    slot == null ||
                                            slot.getMedicationAdministration() == null ||
                                            DateTimeUtil.isGivenTimeFallInRange(slot.getMedicationAdministration().getAdministeredDateTime().getTime()/1000,startTime,endTime)
                            )
                            .collect(Collectors.toList());

                    return createFrom(entry.getKey(), filteredList);
                })
                .collect(Collectors.toList());

    }

}