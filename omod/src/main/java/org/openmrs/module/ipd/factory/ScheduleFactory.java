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

import java.util.Optional;

import static org.openmrs.module.ipd.api.model.ServiceType.MEDICATION_REQUEST;
import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertDateToLocalDateTime;

@Component
public class ScheduleFactory {

    private final OrderService orderService;
    private final ConceptService conceptService;
    private final ReferenceService referenceService;

    @Autowired
    public ScheduleFactory(OrderService orderService, ConceptService conceptService, ReferenceService referenceService) {
        this.orderService = orderService;
        this.conceptService = conceptService;
        this.referenceService = referenceService;
    }

    public Schedule createScheduleForMedicationFrom(ScheduleMedicationRequest request) {
        Schedule schedule = new Schedule();

        DrugOrder drugOrder = (DrugOrder) orderService.getOrderByUuid(request.getOrderUuid());
        Concept medicationRequestServiceType = conceptService.getConceptByName(MEDICATION_REQUEST.conceptName());

        Reference subject = getReference(Patient.class.getTypeName(), request.getPatientUuid());
        Reference actor = getReference(Provider.class.getTypeName(), request.getProviderUuid());

        schedule.setSubject(subject);
        schedule.setActor(actor);
        schedule.setStartDate(convertDateToLocalDateTime(drugOrder.getEffectiveStartDate()));
        schedule.setEndDate(convertDateToLocalDateTime(drugOrder.getEffectiveStopDate()));
        schedule.setServiceType(medicationRequestServiceType);
        schedule.setOrder(drugOrder);
        schedule.setActive(true);

        return schedule;
    }

    private Reference getReference(String type, String targetUuid) {
        Optional<Reference> reference = referenceService.getReferenceByTypeAndTargetUUID(type, targetUuid);
        return reference.orElseGet(() -> new Reference(type, targetUuid));
    }
}
