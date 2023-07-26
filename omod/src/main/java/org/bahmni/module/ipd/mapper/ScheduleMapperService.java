package org.bahmni.module.ipd.mapper;

import org.bahmni.module.ipd.contract.ScheduleMedicationRequest;
import org.bahmni.module.ipd.model.Reference;
import org.bahmni.module.ipd.model.Schedule;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapperService {

    private final OrderService orderService;

    @Autowired
    public ScheduleMapperService(OrderService orderService) {
        this.orderService = orderService;
    }

    public Schedule mapScheduleMedicationRequestToSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {
        Schedule schedule = new Schedule();

        DrugOrder drugOrder = (DrugOrder) orderService.getOrderByUuid(scheduleMedicationRequest.getOrderUuid());

        Reference openmrsForReference = new Reference();
        openmrsForReference.setType(Patient.class.getTypeName());
        openmrsForReference.setTargetUuid(scheduleMedicationRequest.getPatientUuid());
        openmrsForReference.setName(openmrsForReference.getType() + "/" + openmrsForReference.getReference());

        Reference openmrsByReference = new Reference();
        openmrsByReference.setType(Patient.class.getTypeName());
        openmrsByReference.setTargetUuid(scheduleMedicationRequest.getProviderUuid());
        openmrsByReference.setName(openmrsByReference.getType() + "/" + openmrsByReference.getReference());

        schedule.setForReference(openmrsForReference);
        schedule.setByReference(openmrsByReference);
        schedule.setStartDate(drugOrder.getEffectiveStartDate());
        schedule.setEndDate(drugOrder.getEffectiveStopDate());
        schedule.setActive(true);

        return schedule;
    }
}
