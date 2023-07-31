package org.openmrs.module.ipd.factory;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.model.Slot.SlotStatus.SCHEDULED;

@Component
public class SlotFactory {

    private final BedManagementService bedManagementService;
    private final ConceptService conceptService;
    private final PatientService patientService;

    private final String IPD_MEDICATION_SERVICE_TYPE  = "MedicationRequest";

    @Autowired
    public SlotFactory(BedManagementService bedManagementService, ConceptService conceptService, PatientService patientService) {
        this.bedManagementService = bedManagementService;
        this.conceptService = conceptService;
        this.patientService = patientService;
    }

    public List<Slot> createSlotsForMedicationFrom(Schedule savedSchedule, List<LocalDateTime> slotsStartTime) {

        return slotsStartTime.stream().map(slotStartTime -> {
            Slot slot = new Slot();

            String patientUuid = savedSchedule.getForReference().getTargetUuid();
            Patient patient = patientService.getPatientByUuid(patientUuid);
            Location patientLocation = bedManagementService.getBedAssignmentDetailsByPatient(patient).getPhysicalLocation();
            slot.setLocation(patientLocation);

            Concept medicationRequestServiceType = conceptService.getConceptByName(IPD_MEDICATION_SERVICE_TYPE);
            slot.setServiceType(medicationRequestServiceType);

            slot.setSchedule(savedSchedule);
            slot.setStartDateTime(slotStartTime);
            slot.setStatus(SCHEDULED);
            return slot;
        }).collect(Collectors.toList());
    }
}
