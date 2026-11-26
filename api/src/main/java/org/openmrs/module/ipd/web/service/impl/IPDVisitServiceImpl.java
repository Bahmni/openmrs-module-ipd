package org.openmrs.module.ipd.web.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.ipd.web.model.DrugOrderSchedule;
import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.model.*;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.web.service.IPDVisitService;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class IPDVisitServiceImpl implements IPDVisitService {

    private BahmniDrugOrderService drugOrderService;
    private IPDScheduleService ipdScheduleService;
    private SlotTimeCreationService slotTimeCreationService;
    private BahmniObsService bahmniObsService;
    private ConceptService conceptService;
    private BahmniDrugOrderMapper bahmniDrugOrderMapper;
    private ReferenceService referenceService;
    private VisitService visitService;
    private SlotService slotService;

    @Autowired
    public IPDVisitServiceImpl(BahmniDrugOrderService drugOrderService,
                               IPDScheduleService ipdScheduleService,
                               SlotTimeCreationService slotTimeCreationService,
                               BahmniObsService bahmniObsService,
                               ConceptService conceptService,
                               ReferenceService referenceService,
                               VisitService visitService,
                               SlotService slotService) {
        this.drugOrderService = drugOrderService;
        this.ipdScheduleService = ipdScheduleService;
        this.slotTimeCreationService = slotTimeCreationService;
        this.bahmniObsService = bahmniObsService;
        this.conceptService = conceptService;
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
        this.referenceService = referenceService;
        this.visitService = visitService;
        this.slotService = slotService;
    }



    @Override
    public List<IPDDrugOrder> getPrescribedOrders(String visitUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        List<DrugOrder> drugOrders = getVisitAndActiveDrugOrders(visit, includeActiveVisit, numberOfVisits, startDate, endDate, getEffectiveOrdersOnly);
        return getIPDDrugOrders(visit.getPatient().getUuid(), drugOrders, visit);
    }

    private List<DrugOrder> getVisitAndActiveDrugOrders(Visit visit, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly) {
        String patientUuid = visit.getPatient().getUuid();
        List<DrugOrder> prescribedDrugOrders = drugOrderService.getPrescribedDrugOrders(getVisitUuidsToFetchOrdersFrom(visit), patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate, getEffectiveOrdersOnly);
        // Orders placed in any earlier visit must show up as well, otherwise active ones can never be added to the drug chart
        // and ones that stopped during the stay would disappear from it. Active and inactive orders are looked up by patient and,
        // like the visit query, ignore the caller's date and visit-count filters. Null filters: an empty concept set would match nothing.
        List<DrugOrder> patientDrugOrders = new ArrayList<>(drugOrderService.getActiveDrugOrders(patientUuid));
        patientDrugOrders.addAll(drugOrderService.getInactiveDrugOrders(patientUuid, null, null, null));
        List<DrugOrder> mergedDrugOrders = mergeDistinctByUuid(prescribedDrugOrders, excludeOrdersActivatedAfterVisit(patientDrugOrders, visit));
        mergedDrugOrders.sort(Comparator.comparing(DrugOrder::getDateActivated, Comparator.nullsLast(Comparator.reverseOrder())));
        return mergedDrugOrders;
    }

    private List<String> getVisitUuidsToFetchOrdersFrom(Visit visit) {
        List<String> visitUuids = new ArrayList<>();
        visitUuids.add(visit.getUuid());
        // Logic to fetch immediate preceded OPD Visit's drug orders as well as doctors tend to convert OPD to IPD immediately on emergency situations.
        String precededVisitUuid = getImmediatePrecededOPDVisit(visit.getPatient(), visit.getUuid());
        if (precededVisitUuid != null) {
            visitUuids.add(precededVisitUuid);
        }
        return visitUuids;
    }

    private List<DrugOrder> mergeDistinctByUuid(List<DrugOrder> drugOrders, List<DrugOrder> otherDrugOrders) {
        // LinkedHashMap + putIfAbsent: for an order found by both queries the first list wins, deterministically. The caller re-sorts the result.
        Map<String, DrugOrder> drugOrdersByUuid = new LinkedHashMap<>();
        drugOrders.forEach(drugOrder -> drugOrdersByUuid.putIfAbsent(drugOrder.getUuid(), drugOrder));
        otherDrugOrders.forEach(drugOrder -> drugOrdersByUuid.putIfAbsent(drugOrder.getUuid(), drugOrder));
        return new ArrayList<>(drugOrdersByUuid.values());
    }

    // Orders placed after a closed visit ended belong to a later visit. An open visit has no end, so nothing is excluded.
    private List<DrugOrder> excludeOrdersActivatedAfterVisit(List<DrugOrder> drugOrders, Visit visit) {
        Date visitEnd = visit.getStopDatetime();
        return drugOrders.stream()
                .filter(drugOrder -> visitEnd == null || drugOrder.getDateActivated() == null || !drugOrder.getDateActivated().after(visitEnd))
                .collect(Collectors.toList());
    }

    private List<DrugOrder> excludeOrdersEndedBeforeVisit(List<DrugOrder> drugOrders, Visit currentVisit) {
        return drugOrders.stream()
                .filter(drugOrder -> drugOrder.getEffectiveStopDate() == null || drugOrder.getEffectiveStopDate().after(currentVisit.getStartDatetime()))
                .collect(Collectors.toList());
    }

    private List<IPDDrugOrder> getIPDDrugOrders(String patientUuid, List<DrugOrder> drugOrders,Visit currentVisit) {
        // exclude drug orders that ended before the current visit started
        List<DrugOrder> drugOrdersFiltered = excludeOrdersEndedBeforeVisit(drugOrders, currentVisit);
        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrdersFiltered);

        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null, null, null);
            List<BahmniDrugOrder> bahmniDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrdersFiltered, orderAttributeObs, drugOrderMap , null);
            bahmniDrugOrders=sortDrugOrdersAccordingToTheirSortWeight(bahmniDrugOrders);
            Map<String, DrugOrderSchedule> drugOrderScheduleByOrders = getDrugOrderScheduleForOrders(patientUuid, bahmniDrugOrders);

            return bahmniDrugOrders.stream().map(bahmniDrugOrder -> IPDDrugOrder.createFrom(bahmniDrugOrder,drugOrderScheduleByOrders.get(bahmniDrugOrder.getUuid()))).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

    private Map<String, DrugOrderSchedule> getDrugOrderScheduleForOrders(String patientUuid, List<BahmniDrugOrder> bahmniDrugOrders) {
        List<String> orderUuids = bahmniDrugOrders.stream()
                .map(BahmniDrugOrder::getUuid)
                .collect(Collectors.toList());
        List<Slot> slots = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST,orderUuids);
        List<Slot> prnSlots = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.AS_NEEDED_MEDICATION_REQUEST,orderUuids);
        slots.addAll(prnSlots);
        Map<DrugOrder, List<Slot>> groupedByOrders = slots.stream()
                .collect(Collectors.groupingBy(slot -> (DrugOrder) slot.getOrder()));

        Map<String, DrugOrderSchedule> drugOrderScheduleByOrders = slotTimeCreationService.getDrugOrderScheduledTime(groupedByOrders);


        return drugOrderScheduleByOrders;
    }

    private List<BahmniDrugOrder> sortDrugOrdersAccordingToTheirSortWeight(List<BahmniDrugOrder> bahmniDrugOrders) {
        Map<String, ArrayList<BahmniDrugOrder>> bahmniDrugOrderMap = groupDrugOrdersAccordingToOrderSet(bahmniDrugOrders);
        List<BahmniDrugOrder> sortDrugOrders = new ArrayList<>();
        for (String key : bahmniDrugOrderMap.keySet()) {
            if(key == null) {
                continue;
            }
            List<BahmniDrugOrder> bahmniDrugOrder = bahmniDrugOrderMap.get(key);
            Collections.sort(bahmniDrugOrder, new Comparator<BahmniDrugOrder>() {
                @Override
                public int compare(BahmniDrugOrder o1, BahmniDrugOrder o2) {
                    return o1.getSortWeight().compareTo(o2.getSortWeight());
                }
            });
        }

        for (String s : bahmniDrugOrderMap.keySet()) {
            sortDrugOrders.addAll(bahmniDrugOrderMap.get(s));
        }
        return sortDrugOrders;
    }

    private Map<String, ArrayList<BahmniDrugOrder>> groupDrugOrdersAccordingToOrderSet(List<BahmniDrugOrder> bahmniDrugOrders) {
        Map<String, ArrayList<BahmniDrugOrder>> groupedDrugOrders = new LinkedHashMap<>();

        for (BahmniDrugOrder bahmniDrugOrder: bahmniDrugOrders) {
            String orderSetUuid = null == bahmniDrugOrder.getOrderGroup() ? null : bahmniDrugOrder.getOrderGroup().getOrderSet().getUuid();

            if(!groupedDrugOrders.containsKey(orderSetUuid)){
                groupedDrugOrders.put(orderSetUuid, new ArrayList<BahmniDrugOrder>());
            }

            groupedDrugOrders.get(orderSetUuid).add(bahmniDrugOrder);
        }

        return groupedDrugOrders;
    }

    private Collection<Concept> getOrdAttributeConcepts() {
        Concept orderAttribute = conceptService.getConceptByName(BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME);
        return orderAttribute == null ? Collections.EMPTY_LIST : orderAttribute.getSetMembers();
    }

    @Override
    public List<Slot> getMedicationSlots(String visitUuid, ServiceType serviceType) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        if (visit == null)
            return Collections.emptyList();
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), visit.getPatient().getUuid());

        if(!subjectReference.isPresent())
            return Collections.emptyList();

        return slotService.getSlotsByPatientAndVisitAndServiceType(subjectReference.get(), visit, concept);
    }

    private String getImmediatePrecededOPDVisit(Patient patient,String currentVisitUuid){
        String previousOPDVisitUuid=null;
        List<Visit> visits= visitService.getVisitsByPatient(patient);
        List<Visit> sortedVisits = visits.stream()
                .sorted(Comparator.comparing(Visit::getStartDatetime).reversed())
                .collect(Collectors.toList());

        int currentVisitIndex = IntStream.range(0, sortedVisits.size())
                .filter(i -> sortedVisits.get(i).getUuid().equals(currentVisitUuid))
                .findFirst()
                .orElse(-1);

        if (currentVisitIndex != -1 && currentVisitIndex + 1 < sortedVisits.size()) {
            Visit previousVisit = sortedVisits.get(currentVisitIndex + 1);
            if ("OPD".equals(previousVisit.getVisitType().getName())){
                previousOPDVisitUuid=previousVisit.getUuid();
            }
        }
        return previousOPDVisitUuid;
    }
}
