package org.openmrs.module.ipd.factory;

import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationStatusTranslator;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.ipd.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationPerformerRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Component
public class MedicationAdministrationFactory {

    private MedicationAdministrationTranslator medicationAdministrationTranslator;
    private MedicationAdministrationStatusTranslator medicationAdministrationStatusTranslator;

    @Autowired
    public MedicationAdministrationFactory(MedicationAdministrationTranslator medicationAdministrationTranslator,
                                           MedicationAdministrationStatusTranslator medicationAdministrationStatusTranslator) {
        this.medicationAdministrationTranslator = medicationAdministrationTranslator;
        this.medicationAdministrationStatusTranslator = medicationAdministrationStatusTranslator;
    }

    public MedicationAdministration mapRequestToMedicationAdministration(MedicationAdministrationRequest request, MedicationAdministration medicationAdministration) {

        medicationAdministration.setAdministeredDateTime(request.getAdministeredDateTimeAsLocaltime());
        medicationAdministration.setStatus(medicationAdministrationStatusTranslator.toOpenmrsType(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.fromCode(request.getStatus())));
        medicationAdministration.setPatient(Context.getPatientService().getPatientByUuid(request.getPatientUuid()));
        medicationAdministration.setEncounter(Context.getEncounterService().getEncounterByUuid(request.getEncounterUuid()));
        medicationAdministration.setDrugOrder((DrugOrder) Context.getOrderService().getOrderByUuid(request.getOrderUuid()));

        List<MedicationAdministrationPerformer> providers = new ArrayList<>();
        if (request.getProviders() != null) {
            for (MedicationAdministrationPerformerRequest performer : request.getProviders()) {
                MedicationAdministrationPerformer newProvider = new MedicationAdministrationPerformer();
                newProvider.setActor(Context.getProviderService().getProviderByUuid(performer.getProviderUuid()));
                newProvider.setFunction(Context.getConceptService().getConceptByName(performer.getFunction()));
                providers.add(newProvider);
            }
        }
        medicationAdministration.setPerformers(new HashSet<>(providers));
        List<MedicationAdministrationNote> notes = new ArrayList<>();
        if (request.getNotes() != null) {
            for (MedicationAdministrationNoteRequest note : request.getNotes()) {
                MedicationAdministrationNote newNote = new MedicationAdministrationNote();
                newNote.setAuthor(Context.getProviderService().getProviderByUuid(note.getAuthorUuid()));
                newNote.setText(note.getText());
                newNote.setRecordedTime(note.getRecordedTimeAsLocaltime());
                notes.add(newNote);
            }
        }
        medicationAdministration.setNotes(new HashSet<>(notes));
        medicationAdministration.setDrug(Context.getConceptService().getDrugByUuid(request.getDrugUuid()));
        medicationAdministration.setDosingInstructions(request.getDosingInstructions());
        medicationAdministration.setDose(request.getDose());
        medicationAdministration.setDoseUnits(Context.getConceptService().getConceptByUuid(request.getDoseUnitsUuid()));
        medicationAdministration.setRoute(Context.getConceptService().getConceptByUuid(request.getRouteUuid()));
        medicationAdministration.setSite(Context.getConceptService().getConceptByUuid(request.getSiteUuid()));

        return medicationAdministration;
    }

    public MedicationAdministrationResponse mapMedicationAdministrationToResponse(org.hl7.fhir.r4.model.MedicationAdministration fhirMedicationAdministration) {
        MedicationAdministration openmrsMedicationAdministration = (MedicationAdministration) medicationAdministrationTranslator.toOpenmrsType(fhirMedicationAdministration);
        MedicationAdministrationResponse response =  MedicationAdministrationResponse.createFrom(openmrsMedicationAdministration);
        return response;
    }


}
