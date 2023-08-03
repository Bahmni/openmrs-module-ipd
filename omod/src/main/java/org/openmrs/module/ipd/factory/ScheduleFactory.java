package org.openmrs.module.ipd.factory;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Optional;

@Component
public class ScheduleFactory {

    private final OrderService orderService;
    private final ConceptService conceptService;

    private final ReferenceService referenceService;

    private final String IPD_MEDICATION_SERVICE_TYPE  = "MedicationRequest";

    @Autowired
    public ScheduleFactory(OrderService orderService, ConceptService conceptService, ReferenceService referenceService) {
        this.orderService = orderService;
        this.conceptService = conceptService;
        this.referenceService = referenceService;
    }

    public Schedule createScheduleForMedicationFrom(ScheduleMedicationRequest scheduleMedicationRequest) {
        Schedule schedule = new Schedule();

        DrugOrder drugOrder = (DrugOrder) orderService.getOrderByUuid(scheduleMedicationRequest.getOrderUuid());
        Concept medicationRequestServiceType = conceptService.getConceptByName(IPD_MEDICATION_SERVICE_TYPE);

        Reference openmrsForReference = getReference(Patient.class.getTypeName(), scheduleMedicationRequest.getPatientUuid());
        Reference openmrsByReference = getReference(Provider.class.getTypeName(), scheduleMedicationRequest.getProviderUuid());

        schedule.setForReference(openmrsForReference);
        schedule.setByReference(openmrsByReference);
        schedule.setStartDate(drugOrder.getEffectiveStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        schedule.setEndDate(drugOrder.getEffectiveStopDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        schedule.setServiceType(medicationRequestServiceType);
        schedule.setOrder(drugOrder);
        schedule.setActive(true);

        return schedule;
    }

    private Reference getReference(String type, String targetUuid) {
        Optional<Reference> reference = referenceService.getReferenceByTypeAndTargetUUID(type, targetUuid);
        if(reference.isPresent())
            return reference.get();

        Reference openmrsReference = new Reference();
        openmrsReference.setType(type);
        openmrsReference.setTargetUuid(targetUuid);
        openmrsReference.setName(type + "/" + targetUuid);

        return openmrsReference;
    }
}
