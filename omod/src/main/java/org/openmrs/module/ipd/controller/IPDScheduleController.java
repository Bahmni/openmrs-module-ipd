package org.openmrs.module.ipd.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.util.IPDConstants;
import org.openmrs.module.ipd.api.service.ScheduleService;
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
    private  final VisitService visitService;
    private final PatientService patientService;
    private final ScheduleService scheduleService;

    @Autowired
    public IPDScheduleController(IPDScheduleService ipdScheduleService, VisitService visitService, PatientService patientService, ScheduleService scheduleService) {
        this.ipdScheduleService = ipdScheduleService;
        this.visitService = visitService;
        this.patientService = patientService;
        this.scheduleService = scheduleService;
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
;            if (startTime != null && endTime != null) {
                LocalDateTime localStartDate = convertEpocUTCToLocalTimeZone(startTime);
                LocalDateTime localEndDate = convertEpocUTCToLocalTimeZone(endTime);
                Boolean considerAdministeredTime = view!=null & IPDConstants.IPD_VIEW_DRUG_CHART.equals(view);
                Patient patient = patientService.getPatientByUuid(patientUuid);
                Visit visit = visitService.getActiveVisitsByPatient(patient).get(0);
                List<Slot> slots = ipdScheduleService.getMedicationSlotsForTheGivenTimeFrame(patientUuid, localStartDate, localEndDate,considerAdministeredTime, visit);
                return new ResponseEntity<>(constructResponse(slots, visit), OK);
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
                                                                 @RequestParam(value = "serviceType", required = false) ServiceType serviceType,
                                                                 @RequestParam(value = "orderUuids", required = false) List<String> orderUuids) {
        try {
            List<Slot> slots;
            if (orderUuids == null || orderUuids.isEmpty()) {
                slots =
                        serviceType == null ? ipdScheduleService.getMedicationSlots(patientUuid, MEDICATION_REQUEST) :
                                ipdScheduleService.getMedicationSlots(patientUuid, serviceType);
            } else {
                slots =
                        serviceType == null ? ipdScheduleService.getMedicationSlots(patientUuid, MEDICATION_REQUEST, orderUuids) :
                                ipdScheduleService.getMedicationSlots(patientUuid, serviceType, orderUuids);
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


    private List<MedicationScheduleResponse> constructResponse(List<Slot> slots, Visit visit) {
        Schedule schedule = scheduleService.getScheduleByVisit(visit);
        if(slots.isEmpty() && schedule != null){
            return Lists.newArrayList(createFrom(schedule, slots));
        }
        Map<Schedule, List<Slot>> slotsBySchedule = slots.stream().collect(Collectors.groupingBy(Slot::getSchedule));
        return slotsBySchedule.entrySet().stream().map(entry -> createFrom(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

}