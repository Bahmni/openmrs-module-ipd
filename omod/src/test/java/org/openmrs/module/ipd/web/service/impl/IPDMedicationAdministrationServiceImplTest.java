package org.openmrs.module.ipd.web.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.FhirMedicationAdministrationService;
import org.openmrs.module.fhir2.apiext.dao.FhirMedicationAdministrationDao;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.translators.MedicationAdministrationToSlotStatusTranslator;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.web.factory.ScheduleFactory;
import org.openmrs.module.ipd.web.factory.SlotFactory;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class IPDMedicationAdministrationServiceImplTest {

    @Mock
    private FhirMedicationAdministrationService fhirMedicationAdministrationService;
    @Mock
    private MedicationAdministrationTranslator medicationAdministrationTranslator;
    @Mock
    private MedicationAdministrationFactory medicationAdministrationFactory;
    @Mock
    private SlotFactory slotFactory;
    @Mock
    private SlotService slotService;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private FhirMedicationAdministrationDao fhirMedicationAdministrationDao;
    @Mock
    private MedicationAdministrationToSlotStatusTranslator medicationAdministrationToSlotStatusTranslator;
    @Mock
    private ScheduleFactory scheduleFactory;

    @InjectMocks
    private IPDMedicationAdministrationServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldConvertPrnPlaceholderToAsNeededMedicationRequest_WhenSavingScheduledAdministration() {
        String slotUuid = "prn-slot-uuid";
        long administeredDateTime = System.currentTimeMillis() / 1000;

        Concept prnConcept = mock(Concept.class);
        ConceptName prnConceptName = mock(ConceptName.class);
        when(prnConcept.getName()).thenReturn(prnConceptName);
        when(prnConceptName.getName()).thenReturn(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName());

        Slot slot = new Slot();
        slot.setServiceType(prnConcept);
        slot.setStatus(Slot.SlotStatus.SCHEDULED);
        slot.setStartDateTime(LocalDateTime.now().minusHours(2));

        Concept adminConcept = new Concept();

        org.hl7.fhir.r4.model.MedicationAdministration fhirAdmin =
                mock(org.hl7.fhir.r4.model.MedicationAdministration.class);
        when(fhirAdmin.getId()).thenReturn("admin-id");

        MedicationAdministration openmrsAdmin = new MedicationAdministration();

        MedicationAdministrationRequest request = MedicationAdministrationRequest.builder()
                .slotUuid(slotUuid)
                .administeredDateTime(administeredDateTime)
                .build();

        when(slotService.getSlotByUUID(slotUuid)).thenReturn(slot);
        when(medicationAdministrationFactory.mapRequestToMedicationAdministration(any(), any()))
                .thenReturn(new MedicationAdministration());
        when(medicationAdministrationTranslator.toFhirResource(any())).thenReturn(fhirAdmin);
        when(fhirMedicationAdministrationService.create(any())).thenReturn(fhirAdmin);
        when(fhirMedicationAdministrationDao.get("admin-id")).thenReturn(openmrsAdmin);
        when(medicationAdministrationToSlotStatusTranslator.toSlotStatus(any()))
                .thenReturn(Slot.SlotStatus.COMPLETED);

        PowerMockito.mockStatic(Context.class);
        ConceptService conceptService = mock(ConceptService.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(conceptService.getConceptByName(ServiceType.AS_NEEDED_MEDICATION_REQUEST.conceptName()))
                .thenReturn(adminConcept);

        service.saveScheduledMedicationAdministration(request);

        assertEquals("Service type should be converted to AsNeededMedicationRequest", adminConcept, slot.getServiceType());
        verify(slotService).saveSlot(slot);
    }

    @Test
    public void shouldNotConvertServiceType_WhenSlotIsNotPrnPlaceholder() {
        String slotUuid = "regular-slot-uuid";

        Concept regularConcept = mock(Concept.class);
        ConceptName regularConceptName = mock(ConceptName.class);
        when(regularConcept.getName()).thenReturn(regularConceptName);
        when(regularConceptName.getName()).thenReturn(ServiceType.MEDICATION_REQUEST.conceptName());

        Slot slot = new Slot();
        slot.setServiceType(regularConcept);
        slot.setStatus(Slot.SlotStatus.SCHEDULED);
        slot.setStartDateTime(LocalDateTime.now().minusHours(1));

        org.hl7.fhir.r4.model.MedicationAdministration fhirAdmin =
                mock(org.hl7.fhir.r4.model.MedicationAdministration.class);
        when(fhirAdmin.getId()).thenReturn("admin-id");

        MedicationAdministration openmrsAdmin = new MedicationAdministration();

        MedicationAdministrationRequest request = MedicationAdministrationRequest.builder()
                .slotUuid(slotUuid)
                .administeredDateTime(System.currentTimeMillis() / 1000)
                .build();

        when(slotService.getSlotByUUID(slotUuid)).thenReturn(slot);
        when(medicationAdministrationFactory.mapRequestToMedicationAdministration(any(), any()))
                .thenReturn(new MedicationAdministration());
        when(medicationAdministrationTranslator.toFhirResource(any())).thenReturn(fhirAdmin);
        when(fhirMedicationAdministrationService.create(any())).thenReturn(fhirAdmin);
        when(fhirMedicationAdministrationDao.get("admin-id")).thenReturn(openmrsAdmin);
        when(medicationAdministrationToSlotStatusTranslator.toSlotStatus(any()))
                .thenReturn(Slot.SlotStatus.COMPLETED);

        service.saveScheduledMedicationAdministration(request);

        assertEquals("Service type should not be changed for a non-PRN slot", regularConcept, slot.getServiceType());
        verify(slotService).saveSlot(slot);
    }
}
