package org.openmrs.module.ipd.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.ipd.model.DrugOrderSchedule;
import org.openmrs.module.ipd.model.IPDDrugOrder;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.service.IPDDrugOrderService;
import org.openmrs.module.ipd.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IPDDrugOrderServiceImpl implements IPDDrugOrderService {

    private BahmniDrugOrderService drugOrderService;
    private IPDScheduleService ipdScheduleService;
    private SlotTimeCreationService slotTimeCreationService;
    private BahmniObsService bahmniObsService;
    private ConceptService conceptService;
    private BahmniDrugOrderMapper bahmniDrugOrderMapper;

    @Autowired
    public IPDDrugOrderServiceImpl(BahmniDrugOrderService drugOrderService,
                                   IPDScheduleService ipdScheduleService,
                                   SlotTimeCreationService slotTimeCreationService,
                                   BahmniObsService bahmniObsService,
                                   ConceptService conceptService) {
        this.drugOrderService = drugOrderService;
        this.ipdScheduleService = ipdScheduleService;
        this.slotTimeCreationService = slotTimeCreationService;
        this.bahmniObsService = bahmniObsService;
        this.conceptService = conceptService;
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
    }



    @Override
    public List<IPDDrugOrder> getPrescribedOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly) {
        List<DrugOrder> prescribedDrugOrders = drugOrderService.getPrescribedDrugOrders(visitUuids, patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate, getEffectiveOrdersOnly);
        return getIPDDrugOrders(patientUuid, prescribedDrugOrders);
    }

    private List<IPDDrugOrder> getIPDDrugOrders(String patientUuid, List<DrugOrder> drugOrders) {
        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null, null, null);
            List<BahmniDrugOrder> bahmniDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrders, orderAttributeObs, drugOrderMap , null);
            bahmniDrugOrders=sortDrugOrdersAccordingToTheirSortWeight(bahmniDrugOrders);
            Map<String, DrugOrderSchedule> drugOrderScheduleByOrders = getDrugOrderScheduleForOrders(patientUuid, bahmniDrugOrders);

            return bahmniDrugOrders.stream().map(bahmniDrugOrder -> IPDDrugOrder.createFrom(bahmniDrugOrder.getDrugOrder(),drugOrderScheduleByOrders.get(bahmniDrugOrder.getUuid()))).collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

    private Map<String, DrugOrderSchedule> getDrugOrderScheduleForOrders(String patientUuid, List<BahmniDrugOrder> bahmniDrugOrders) {
        List<String> orderUuids = bahmniDrugOrders.stream()
                .map(BahmniDrugOrder::getUuid)
                .collect(Collectors.toList());
        List<Slot> slots = ipdScheduleService.getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST,orderUuids);
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
}
