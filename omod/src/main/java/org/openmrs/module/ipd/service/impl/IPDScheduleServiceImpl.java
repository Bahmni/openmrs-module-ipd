package org.openmrs.module.ipd.service.impl;

import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.factory.ScheduleFactory;
import org.openmrs.module.ipd.factory.SlotFactory;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.model.Slot.SlotStatus.SCHEDULED;

@Service
@Transactional
public class IPDScheduleServiceImpl implements IPDScheduleService {

    private final ScheduleService scheduleService;
    private final ScheduleFactory scheduleFactory;
    private final SlotFactory slotFactory;
    private final SlotService slotService;
    private final SlotTimeCreationService slotTimeCreationService;
    private final ConceptService conceptService;
    private final ReferenceService referenceService;
    private final VisitService visitService;
    private final PatientService patientService;
    private final OrderService orderService;

    @Autowired
    public IPDScheduleServiceImpl(ScheduleService scheduleService, ScheduleFactory scheduleFactory, SlotFactory slotFactory, SlotService slotService, SlotTimeCreationService slotTimeCreationService, ConceptService conceptService, ReferenceService referenceService, VisitService visitService, PatientService patientService, OrderService orderService) {
        this.scheduleService = scheduleService;
        this.scheduleFactory = scheduleFactory;
        this.slotFactory = slotFactory;
        this.slotService = slotService;
        this.slotTimeCreationService = slotTimeCreationService;
        this.conceptService = conceptService;
        this.referenceService = referenceService;
        this.visitService = visitService;
        this.patientService = patientService;
        this.orderService = orderService;
    }

    @Override
    public Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {
        Patient patient = patientService.getPatientByUuid(scheduleMedicationRequest.getPatientUuid());
        Visit visit = visitService.getActiveVisitsByPatient(patient).get(0);
        Schedule savedSchedule = scheduleService.getScheduleByVisit(visit);
        if(savedSchedule == null || savedSchedule.getId() == null) {
            Schedule schedule = scheduleFactory.createScheduleForMedicationFrom(scheduleMedicationRequest, visit);
            savedSchedule = scheduleService.saveSchedule(schedule);
        }
        DrugOrder order = (DrugOrder) orderService.getOrderByUuid(scheduleMedicationRequest.getOrderUuid());
        List<Slot> existingSlots = getMedicationSlots(patient.getUuid(),ServiceType.MEDICATION_REQUEST,new ArrayList<>(Arrays.asList(new String[]{order.getUuid()})));
        if (existingSlots !=null && !existingSlots.isEmpty()) {
            throw new RuntimeException("Slots already created for this drug order");
        }
        List<LocalDateTime> slotsStartTime = slotTimeCreationService.createSlotsStartTimeFrom(scheduleMedicationRequest, order);
        slotFactory.createSlotsForMedicationFrom(savedSchedule, slotsStartTime, order, null, SCHEDULED, ServiceType.MEDICATION_REQUEST, scheduleMedicationRequest.getComments())
                .forEach(slotService::saveSlot);

        return savedSchedule;
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, LocalDate forDate) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid);
        if(!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsBySubjectReferenceIdAndForDateAndServiceType(subjectReference.get(), forDate, concept);
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid);
        if(!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsBySubjectReferenceIdAndServiceType(subjectReference.get(), concept);
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, List<String> orderUuids) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid);
        if(!subjectReference.isPresent())
            return Collections.emptyList();
         return slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(subjectReference.get(), concept, orderUuids);
    }

    @Override
    public Schedule updateMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {
        voidExistingMedicationSlotsForOrder(scheduleMedicationRequest.getPatientUuid(),scheduleMedicationRequest.getOrderUuid(),"");
        return saveMedicationSchedule(scheduleMedicationRequest);
    }

    private void voidExistingMedicationSlotsForOrder(String patientUuid,String orderUuid,String voidReason){
        List<Slot> existingSlots = getMedicationSlots(patientUuid,ServiceType.MEDICATION_REQUEST,new ArrayList<>(Arrays.asList(new String[]{orderUuid})));
        existingSlots.stream().forEach(slot -> slotService.voidSlot(slot,voidReason));
    }


    @Override
    public List<Slot> getMedicationSlotsForTheGivenTimeFrame(String patientUuid, LocalDateTime localStartDate, LocalDateTime localEndDate, Boolean considerAdministeredTime, Visit visit) {
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), patientUuid);
        if(!subjectReference.isPresent())
            return Collections.emptyList();
        if (considerAdministeredTime) {
            return slotService.getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(subjectReference.get(), localStartDate, localEndDate, visit);
        }
        return slotService.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(subjectReference.get(), localStartDate,localEndDate, visit);
    }

    @Override
    public void handlePostProcessEncounterTransaction(Encounter encounter, EncounterTransaction encounterTransaction) {
        if (Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("bahmni-ipd.allowSlotStopOnDrugOrderStop","false"))) {
            handleDrugOrderStop(encounterTransaction);
        }
    }

    private void handleDrugOrderStop(EncounterTransaction encounterTransaction){
        List<EncounterTransaction.DrugOrder> stoppedDrugOrders = encounterTransaction.getDrugOrders().stream().filter(drugOrder -> drugOrder.getDateStopped() !=null).collect(Collectors.toList());
        String patientUuid = encounterTransaction.getPatientUuid();
        for (EncounterTransaction.DrugOrder drugOrder : stoppedDrugOrders) {
            List<Slot> existingSlots = getMedicationSlots(patientUuid,ServiceType.MEDICATION_REQUEST,new ArrayList<>(Arrays.asList(new String[]{drugOrder.getPreviousOrderUuid()})));
            if (existingSlots == null || existingSlots.isEmpty()) {
                continue;
            }
            boolean atleastOneMedicationAdministered = existingSlots.stream().anyMatch(slot -> slot.getMedicationAdministration() != null);
            if (atleastOneMedicationAdministered){ // Mark status of non administered slots to stopped
                existingSlots.stream().forEach(slot -> {
                    if ((slot.getMedicationAdministration() == null) && !slot.isStopped() && (DateTimeUtil.convertDateToLocalDateTime(drugOrder.getDateStopped())
                            .compareTo(slot.getStartDateTime())) < 0)  {slot.setStatus(Slot.SlotStatus.STOPPED); slotService.saveSlot(slot);}});
            } else { // Void all slots
                existingSlots.stream().forEach(slot -> slotService.voidSlot(slot, ""));
            }
        }

    }
}