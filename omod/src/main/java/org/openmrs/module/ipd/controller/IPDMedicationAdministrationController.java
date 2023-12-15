package org.openmrs.module.ipd.controller;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.contract.ScheduleMedicationResponse;
import org.openmrs.module.ipd.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.service.IPDMedicationAdministrationService;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.openmrs.module.ipd.api.model.ServiceType.MEDICATION_REQUEST;
import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertEpocUTCToLocalTimeZone;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd")
@Slf4j
public class IPDMedicationAdministrationController extends BaseRestController {

    private final IPDMedicationAdministrationService ipdMedicationAdministrationService;
    private final SlotService slotService;
    private final MedicationAdministrationTranslator medicationAdministrationTranslator;
    private final MedicationAdministrationFactory medicationAdministrationFactory;

    @Autowired
    public IPDMedicationAdministrationController(IPDMedicationAdministrationService ipdMedicationAdministrationService,
                                                 SlotService slotService,
                                                 MedicationAdministrationTranslator medicationAdministrationTranslator,
                                                 MedicationAdministrationFactory medicationAdministrationFactory) {
        this.ipdMedicationAdministrationService = ipdMedicationAdministrationService;
        this.slotService = slotService;
        this.medicationAdministrationTranslator = medicationAdministrationTranslator;
        this.medicationAdministrationFactory = medicationAdministrationFactory;
    }

    @RequestMapping(value = "/medicationAdministrations", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createMedicationAdministration(@RequestBody List<MedicationAdministrationRequest> medicationAdministrationRequestList) {
        try {
            List<MedicationAdministrationResponse> medicationAdministrationResponseList = new ArrayList<>();
            for (MedicationAdministrationRequest medicationAdministrationRequest : medicationAdministrationRequestList) {
                MedicationAdministration medicationAdministration = ipdMedicationAdministrationService.saveMedicationAdministration(medicationAdministrationRequest);
                System.out.println("medicationAdministration uuid is ********* "+medicationAdministration.getId());
                Slot slot = slotService.getSlotByUUID(medicationAdministrationRequest.getSlotUuid());
                if(medicationAdministration.getStatus().equals(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.COMPLETED)){
                    slot.setStatus(Slot.SlotStatus.COMPLETED);
                }
                slot.setMedicationAdministration((org.openmrs.module.fhir2.model.MedicationAdministration) medicationAdministrationTranslator.toOpenmrsType(medicationAdministration));
                System.out.println("before saving slot ********* "+slot.getMedicationAdministration().getUuid());
                slotService.saveSlot(slot);
                System.out.println("after saving slot ********* "+slot.getMedicationAdministration().getUuid());
                medicationAdministrationResponseList.add(medicationAdministrationFactory.createFrom(medicationAdministration));
            }
            return new ResponseEntity<>(medicationAdministrationResponseList, OK);
        } catch (Exception e) {
            log.error("Runtime error while trying to create new medicationAdministration", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/medicationAdministrations", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getMedicationSlotsByDate(@RequestParam(value = "patientUuid", required = false) String patientUuid,
                                                           @RequestParam(value = "forDate", required = false) Long forDate,
                                                            @RequestParam(value = "providerUuid", required = false) String providerUuid,
                                                            @RequestParam (value = "slotUuid" , required = false)String slotUuid) {
        try {
            LocalDate localDate = forDate !=null ? convertEpocUTCToLocalTimeZone(forDate).toLocalDate() : null;
            List<MedicationAdministrationResponse> medicationAdministrationResponseList = ipdMedicationAdministrationService.getMedicationAdministrationList(patientUuid,localDate,providerUuid,slotUuid);
            return new ResponseEntity<>(medicationAdministrationResponseList, OK);
        } catch (Exception e) {
            log.error("Runtime error while fetching medicationAdministrations ", e);
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), BAD_REQUEST);
        }
    }
}
