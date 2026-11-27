package org.openmrs.module.ipd.web.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;

public class MedicationAdministrationFactoryTest {

    private final MedicationAdministrationFactory factory = new MedicationAdministrationFactory(null);

    @Test
    public void mapRequestToMedicationAdministrationForReplace_shouldNotAppendNotes() {
        MedicationAdministration existing = new MedicationAdministration();
        existing.setNotes(new HashSet<>());
        MedicationAdministrationNote existingNote = new MedicationAdministrationNote();
        existingNote.setUuid("existing-note-uuid");
        existingNote.setText("original note");
        existing.getNotes().add(existingNote);

        MedicationAdministrationNoteRequest noteFromPut = MedicationAdministrationNoteRequest.builder()
                .text("note sent in PUT body")
                .authorUuid("provider-uuid")
                .build();
        MedicationAdministrationRequest request = MedicationAdministrationRequest.builder()
                .status("completed")
                .notes(Collections.singletonList(noteFromPut))
                .build();

        MedicationAdministration result = factory.mapRequestToMedicationAdministrationForReplace(request, existing);

        assertSame(existing, result);
        assertEquals(1, result.getNotes().size());
        assertEquals("existing-note-uuid", result.getNotes().iterator().next().getUuid());
    }
}
