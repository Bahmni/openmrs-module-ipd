package org.openmrs.module.ipd.web.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.web.service.IPDScheduleService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class IPDVisitServiceImplTest {

    private static final long DAY = 24L * 60 * 60 * 1000;
    private static final String IPD_VISIT_UUID = "ipd-visit";
    private static final String PATIENT_UUID = "patient-uuid";

    @Mock
    private BahmniDrugOrderService drugOrderService;
    @Mock
    private IPDScheduleService ipdScheduleService;
    @Mock
    private SlotTimeCreationService slotTimeCreationService;
    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private ReferenceService referenceService;
    @Mock
    private VisitService visitService;
    @Mock
    private SlotService slotService;
    @Mock
    private BahmniDrugOrderMapper bahmniDrugOrderMapper;
    @Captor
    private ArgumentCaptor<List<String>> visitUuidsCaptor;
    @Captor
    private ArgumentCaptor<List<DrugOrder>> drugOrdersCaptor;

    private IPDVisitServiceImpl ipdVisitService;
    private Patient patient;
    private Date now;
    private Visit ipdVisit;

    @Before
    public void setUp() throws Exception {
        ipdVisitService = new IPDVisitServiceImpl(drugOrderService, ipdScheduleService, slotTimeCreationService,
                bahmniObsService, conceptService, referenceService, visitService, slotService);
        patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        now = new Date();

        ipdVisit = visit(IPD_VISIT_UUID, "IPD", daysFromNow(-2));
        Mockito.when(visitService.getVisitByUuid(IPD_VISIT_UUID)).thenReturn(ipdVisit);
        useDrugOrderMapper(bahmniDrugOrderMapper);
        Mockito.when(bahmniDrugOrderMapper.mapToResponse(Mockito.anyList(), Mockito.any(), Mockito.anyMap(), Mockito.any()))
                .thenAnswer(invocation -> toBahmniDrugOrders(invocation.getArgument(0)));
        Mockito.when(ipdScheduleService.getMedicationSlots(Mockito.anyString(), Mockito.any(), Mockito.anyList()))
                .thenAnswer(invocation -> new ArrayList<>());
    }

    @Test
    public void shouldReturnOrdersPlacedInTheIPDVisit() {
        givenVisitOrders(drugOrder("ipd-order", null));

        assertEquals(Collections.singletonList("ipd-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldAskForOrdersOfTheIPDVisitAndTheImmediatelyPrecedingOPDVisit() {
        givenPrecedingVisits(visit("opd-visit", "OPD", daysFromNow(-3)), visit("older-opd-visit", "OPD", daysFromNow(-10)));

        prescribedOrderUuids();

        assertEquals(Arrays.asList(IPD_VISIT_UUID, "opd-visit"), requestedVisitUuids());
    }

    @Test
    public void shouldNotAskForOrdersOfAPrecedingVisitThatIsNotOPD() {
        givenPrecedingVisits(visit("emergency-visit", "EMERGENCY", daysFromNow(-3)));

        prescribedOrderUuids();

        assertEquals(Collections.singletonList(IPD_VISIT_UUID), requestedVisitUuids());
    }

    @Test
    public void shouldAddActiveOrderFromAVisitOlderThanThePrecedingOne() {
        givenVisitOrders(drugOrder("ipd-order", null));
        givenActiveOrders(drugOrder("older-visit-order", null));

        assertEquals(Arrays.asList("ipd-order", "older-visit-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldAddActiveOrderFromAVisitThatIsNotOPD() {
        givenPrecedingVisits(visit("emergency-visit", "EMERGENCY", daysFromNow(-3)));
        givenActiveOrders(drugOrder("emergency-order", null));

        assertEquals(Collections.singletonList("emergency-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldAddActiveOrderScheduledToStartInTheFuture() {
        DrugOrder scheduledOrder = drugOrder("scheduled-order", daysFromNow(10));
        scheduledOrder.setScheduledDate(daysFromNow(5));
        givenActiveOrders(scheduledOrder);

        assertEquals(Collections.singletonList("scheduled-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldListAnOrderOnlyOnceWhenFoundByBothQueries() {
        givenVisitOrders(drugOrder("shared-order", null));
        givenActiveOrders(drugOrder("shared-order", null));

        assertEquals(Collections.singletonList("shared-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldListOrdersFromBothQueriesNewestFirst() {
        givenVisitOrders(activatedDrugOrder("visit-order", daysFromNow(-3)));
        givenActiveOrders(activatedDrugOrder("older-visit-order", daysFromNow(-20)), activatedDrugOrder("newer-active-order", daysFromNow(-1)));

        assertEquals(Arrays.asList("newer-active-order", "visit-order", "older-visit-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldKeepOrdersFromTheVisitQueryThatAreNoLongerActive() {
        DrugOrder stoppedDuringVisit = drugOrder("stopped-during-visit", null);
        stoppedDuringVisit.setDateStopped(daysFromNow(-1));
        givenVisitOrders(stoppedDuringVisit);

        assertEquals(Collections.singletonList("stopped-during-visit"), prescribedOrderUuids());
    }

    @Test
    public void shouldAddOrderFromAnOlderVisitThatExpiredDuringTheStay() {
        givenVisitOrders(drugOrder("ipd-order", null));
        givenInactiveOrders(drugOrder("expired-during-stay", daysFromNow(-1)));

        assertEquals(Arrays.asList("ipd-order", "expired-during-stay"), prescribedOrderUuids());
    }

    @Test
    public void shouldAddOrderFromAnOlderVisitThatWasStoppedDuringTheStay() {
        DrugOrder stoppedDuringStay = drugOrder("stopped-during-stay", null);
        stoppedDuringStay.setDateStopped(daysFromNow(-1));
        givenInactiveOrders(stoppedDuringStay);

        assertEquals(Collections.singletonList("stopped-during-stay"), prescribedOrderUuids());
    }

    @Test
    public void shouldListAnOrderOnlyOnceWhenFoundByTheVisitAndInactiveQueries() {
        givenVisitOrders(drugOrder("shared-order", daysFromNow(-1)));
        givenInactiveOrders(drugOrder("shared-order", daysFromNow(-1)));

        assertEquals(Collections.singletonList("shared-order"), prescribedOrderUuids());
    }

    @Test
    public void shouldReturnNothingWhenThePatientHasNoOrders() {
        assertTrue(prescribedOrderUuids().isEmpty());
    }

    @Test
    public void shouldForwardTheCallersParametersToTheVisitQueryAndLookUpActiveAndInactiveOrdersByPatient() {
        Date startDate = daysFromNow(-30);
        Date endDate = daysFromNow(30);

        ipdVisitService.getPrescribedOrders(IPD_VISIT_UUID, false, 3, startDate, endDate, true);

        Mockito.verify(drugOrderService).getPrescribedDrugOrders(Collections.singletonList(IPD_VISIT_UUID),
                PATIENT_UUID, false, 3, startDate, endDate, true);
        Mockito.verify(drugOrderService).getActiveDrugOrders(PATIENT_UUID);
        // null, not empty sets: an empty concept set would match no orders at all
        Mockito.verify(drugOrderService).getInactiveDrugOrders(PATIENT_UUID, null, null, null);
    }

    @Test
    public void shouldExcludeOrdersThatEndedBeforeTheVisitStarted() {
        DrugOrder expired = drugOrder("expired", daysFromNow(-5));
        DrugOrder stopped = drugOrder("stopped", null);
        stopped.setDateStopped(daysFromNow(-3));
        DrugOrder endsAfterAdmission = drugOrder("ends-after-admission", daysFromNow(1));
        DrugOrder noEnd = drugOrder("no-end", null);
        givenVisitOrders(expired, stopped, endsAfterAdmission, noEnd);

        assertEquals(Arrays.asList("ends-after-admission", "no-end"), prescribedOrderUuids());
    }

    @Test
    public void shouldMapTheMergedOrdersExceptThoseThatEndedBeforeAdmission() throws Exception {
        DrugOrder ipdOrder = activatedDrugOrder("ipd-order", daysFromNow(-1));
        DrugOrder endedBeforeAdmission = drugOrder("ended-before-admission", daysFromNow(-5));
        endedBeforeAdmission.setDateActivated(daysFromNow(-8));
        DrugOrder olderVisitOrder = activatedDrugOrder("older-visit-order", daysFromNow(-20));
        DrugOrder olderEndedBeforeAdmission = drugOrder("older-ended-before-admission", daysFromNow(-15));
        olderEndedBeforeAdmission.setDateActivated(daysFromNow(-30));
        DrugOrder expiredDuringStay = drugOrder("expired-during-stay", daysFromNow(-1));
        expiredDuringStay.setDateActivated(daysFromNow(-25));
        givenVisitOrders(ipdOrder, endedBeforeAdmission);
        givenActiveOrders(olderVisitOrder);
        givenInactiveOrders(olderEndedBeforeAdmission, expiredDuringStay);

        List<IPDDrugOrder> prescribedOrders = ipdVisitService.getPrescribedOrders(IPD_VISIT_UUID, true, null, null, null, false);

        Mockito.verify(drugOrderService).getDiscontinuedDrugOrders(drugOrdersCaptor.capture());
        List<String> expectedUuids = Arrays.asList("ipd-order", "older-visit-order", "expired-during-stay");
        assertEquals(expectedUuids, uuidsOf(drugOrdersCaptor.getValue()));
        assertEquals(expectedUuids, prescribedOrders.stream()
                .map(prescribedOrder -> prescribedOrder.getBahmniDrugOrder().getUuid()).collect(Collectors.toList()));
    }

    private List<String> prescribedOrderUuids() {
        return ipdVisitService.getPrescribedOrders(IPD_VISIT_UUID, true, null, null, null, false).stream()
                .map(prescribedOrder -> prescribedOrder.getBahmniDrugOrder().getUuid()).collect(Collectors.toList());
    }

    private List<String> uuidsOf(List<DrugOrder> drugOrders) {
        return drugOrders.stream().map(DrugOrder::getUuid).collect(Collectors.toList());
    }

    private List<String> requestedVisitUuids() {
        Mockito.verify(drugOrderService).getPrescribedDrugOrders(visitUuidsCaptor.capture(), Mockito.eq(PATIENT_UUID),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        return visitUuidsCaptor.getValue();
    }

    private void givenPrecedingVisits(Visit... precedingVisits) {
        List<Visit> patientVisits = new ArrayList<>(Collections.singletonList(ipdVisit));
        patientVisits.addAll(Arrays.asList(precedingVisits));
        Mockito.when(visitService.getVisitsByPatient(patient)).thenReturn(patientVisits);
    }

    private void givenVisitOrders(DrugOrder... drugOrders) {
        Mockito.when(drugOrderService.getPrescribedDrugOrders(Mockito.anyList(), Mockito.eq(PATIENT_UUID), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>(Arrays.asList(drugOrders)));
    }

    private void givenActiveOrders(DrugOrder... drugOrders) {
        Mockito.when(drugOrderService.getActiveDrugOrders(PATIENT_UUID)).thenReturn(new ArrayList<>(Arrays.asList(drugOrders)));
    }

    private void givenInactiveOrders(DrugOrder... drugOrders) {
        Mockito.when(drugOrderService.getInactiveDrugOrders(PATIENT_UUID, null, null, null)).thenReturn(new ArrayList<>(Arrays.asList(drugOrders)));
    }

    // The service creates its mapper itself, and the real one needs fully populated orders.
    private void useDrugOrderMapper(BahmniDrugOrderMapper mapper) throws ReflectiveOperationException {
        Field mapperField = IPDVisitServiceImpl.class.getDeclaredField("bahmniDrugOrderMapper");
        mapperField.setAccessible(true);
        mapperField.set(ipdVisitService, mapper);
    }

    private List<BahmniDrugOrder> toBahmniDrugOrders(List<DrugOrder> drugOrders) {
        return drugOrders.stream().map(drugOrder -> {
            EncounterTransaction.DrugOrder encounterDrugOrder = new EncounterTransaction.DrugOrder();
            encounterDrugOrder.setUuid(drugOrder.getUuid());
            BahmniDrugOrder bahmniDrugOrder = new BahmniDrugOrder();
            bahmniDrugOrder.setDrugOrder(encounterDrugOrder);
            return bahmniDrugOrder;
        }).collect(Collectors.toList());
    }

    private Date daysFromNow(int days) {
        return new Date(now.getTime() + days * DAY);
    }

    private DrugOrder activatedDrugOrder(String uuid, Date dateActivated) {
        DrugOrder drugOrder = drugOrder(uuid, null);
        drugOrder.setDateActivated(dateActivated);
        return drugOrder;
    }

    private DrugOrder drugOrder(String uuid, Date autoExpireDate) {
        DrugOrder drugOrder = new DrugOrder();
        drugOrder.setUuid(uuid);
        drugOrder.setAutoExpireDate(autoExpireDate);
        return drugOrder;
    }

    private Visit visit(String uuid, String visitTypeName, Date startDatetime) {
        Visit visit = new Visit();
        visit.setUuid(uuid);
        visit.setPatient(patient);
        visit.setVisitType(new VisitType(visitTypeName, visitTypeName));
        visit.setStartDatetime(startDatetime);
        return visit;
    }
}
