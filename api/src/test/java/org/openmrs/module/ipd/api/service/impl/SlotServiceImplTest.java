package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class SlotServiceImplTest {

    @InjectMocks
    private SlotServiceImpl slotService;

    @Mock
    private SlotDAO slotDAO;

    @Test
    public void shouldInvokeSaveSlotWithGivenSlot() {
        Slot slot = new Slot();
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);

        Mockito.when(slotDAO.saveSlot(slot)).thenReturn(expectedSlot);

        slotService.saveSlot(slot);

        Mockito.verify(slotDAO, Mockito.times(1)).saveSlot(slot);
    }

    @Test
    public void shouldInvokeGetSlotWithGivenSlotId() {
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);

        Mockito.when(slotDAO.getSlot(1)).thenReturn(expectedSlot);

        slotService.getSlot(1);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlot(1);
    }

    @Test
    public void shouldInvokeGetSlotsByForReferenceAndForDateAndServiceTypeWithGivenScheduleId() {
        Slot expectedSlot = new Slot();
        expectedSlot.setId(1);
        List<Slot> slots = new ArrayList<>();

        LocalDate today = LocalDate.now();
        Concept medicationRequestConcept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName("MedicationRequest");
        conceptName.setLocale(Locale.US);
        medicationRequestConcept.setFullySpecifiedName(conceptName);

        Reference patientReference = new Reference(Patient.class.getTypeName(), "patientUuid");

        Mockito.when(slotDAO.getSlotsByForReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept)).thenReturn(slots);

        slotService.getSlotByForReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept);

        Mockito.verify(slotDAO, Mockito.times(1)).getSlotsByForReferenceIdAndForDateAndServiceType(patientReference, today, medicationRequestConcept);
    }
}