package org.openmrs.module.ipd.factory;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.model.ServiceType.MEDICATION_REQUEST;
import static org.openmrs.module.ipd.api.model.Slot.SlotStatus.SCHEDULED;

@Component
public class SlotFactory {

    private final BedManagementService bedManagementService;
    private final ConceptService conceptService;
    private final PatientService patientService;
    private final SlotService slotService;

    @Autowired
    public SlotFactory(BedManagementService bedManagementService, ConceptService conceptService, PatientService patientService,SlotService slotService) {
        this.bedManagementService = bedManagementService;
        this.conceptService = conceptService;
        this.patientService = patientService;
        this.slotService = slotService;
    }

    public List<Slot> createSlotsForMedicationFrom(Schedule savedSchedule, List<LocalDateTime> slotsStartTime, Order drugOrder) {

        return slotsStartTime.stream().map(slotStartTime -> {
            Slot slot = new Slot();

            String patientUuid = savedSchedule.getSubject().getTargetUuid();
            Patient patient = patientService.getPatientByUuid(patientUuid);
            BedDetails bedAssignmentDetailsByPatient = bedManagementService.getBedAssignmentDetailsByPatient(patient);
            if(bedAssignmentDetailsByPatient != null){
                slot.setLocation(bedAssignmentDetailsByPatient.getPhysicalLocation());
            }

            Concept medicationRequestServiceType = conceptService.getConceptByName(MEDICATION_REQUEST.conceptName());
            slot.setServiceType(medicationRequestServiceType);

            slot.setOrder(drugOrder);
            slot.setSchedule(savedSchedule);
            slot.setStartDateTime(slotStartTime);
            slot.setStatus(SCHEDULED);
            return slot;
        }).collect(Collectors.toList());
    }

    public Slot getSlotFromUUID(String uuid){
       return slotService.getSlotByUUID(uuid);
    }
}
