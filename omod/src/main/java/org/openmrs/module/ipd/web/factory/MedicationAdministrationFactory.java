package org.openmrs.module.ipd.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationPerformerRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
public class MedicationAdministrationFactory {

    private MedicationAdministrationTranslator medicationAdministrationTranslator;

    @Autowired
    public MedicationAdministrationFactory(MedicationAdministrationTranslator medicationAdministrationTranslator) {
        this.medicationAdministrationTranslator = medicationAdministrationTranslator;
    }

    public MedicationAdministration mapRequestToMedicationAdministration(MedicationAdministrationRequest request, MedicationAdministration existingMedicationAdministration) {

        MedicationAdministration medicationAdministration = new MedicationAdministration();
        if (existingMedicationAdministration ==null ||  existingMedicationAdministration.getId() == null) {
            medicationAdministration.setAdministeredDateTime(request.getAdministeredDateTimeAsLocaltime());
            medicationAdministration.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.fromCode(request.getStatus()));
            medicationAdministration.setPatient(getPatient(request.getPatientUuid()));
            DrugOrder drugOrder = getDrugOrder(request.getOrderUuid());
            medicationAdministration.setDrugOrder(drugOrder);
            medicationAdministration.setEncounter(resolveEncounter(request.getEncounterUuid(), drugOrder));
            medicationAdministration.setDrug(getDrug(request.getDrugUuid()));
            medicationAdministration.setDosingInstructions(request.getDosingInstructions());
            medicationAdministration.setDose(request.getDose());
            medicationAdministration.setDoseUnits(getConcept(request.getDoseUnits()));
            medicationAdministration.setRoute(getConcept(request.getRoute()));
            medicationAdministration.setSite(getConcept(request.getSite()));
        }
        else {
            medicationAdministration.setUuid(existingMedicationAdministration.getUuid());
        }
        List<MedicationAdministrationPerformer> providers = new ArrayList<>();
        if (request.getProviders() != null) {
            providers.addAll(buildPerformers(request.getProviders(), true));
            if (existingMedicationAdministration !=null && existingMedicationAdministration.getPerformers() !=null){
                providers.addAll(existingMedicationAdministration.getPerformers());
            }
        }
        medicationAdministration.setPerformers(new HashSet<>(providers));
        List<MedicationAdministrationNote> notes = new ArrayList<>();
        if (request.getNotes() != null) {
            notes.addAll(buildNotes(request.getNotes(), true));
            if (existingMedicationAdministration !=null && existingMedicationAdministration.getNotes() !=null){
                notes.addAll(existingMedicationAdministration.getNotes());
            }
        }
        medicationAdministration.setNotes(new HashSet<>(notes));
        return medicationAdministration;
    }

    /**
     * FHIR PUT semantics: replace the scalar fields and the performer list. Notes are an audit trail
     * and are not modified by PUT; they are appended only through the PATCH amend-note flow. The
     * legacy REST update keeps using {@link #mapRequestToMedicationAdministration(
     * MedicationAdministrationRequest, MedicationAdministration)}, which appends performers and notes.
     */
    public MedicationAdministration mapRequestToMedicationAdministrationForReplace(MedicationAdministrationRequest request,
                                                                                   MedicationAdministration existingMedicationAdministration) {
        existingMedicationAdministration.setAdministeredDateTime(toDate(request.getAdministeredDateTime()));
        existingMedicationAdministration.setStatus(org.hl7.fhir.r4.model.MedicationAdministration.MedicationAdministrationStatus.fromCode(request.getStatus()));
        existingMedicationAdministration.setPatient(getPatient(request.getPatientUuid()));
        DrugOrder drugOrder = getDrugOrder(request.getOrderUuid());
        existingMedicationAdministration.setDrugOrder(drugOrder);
        existingMedicationAdministration.setEncounter(resolveEncounter(request.getEncounterUuid(), drugOrder));
        existingMedicationAdministration.setDrug(getDrug(request.getDrugUuid()));
        existingMedicationAdministration.setStatusReason(getConcept(request.getStatusReason()));
        existingMedicationAdministration.setDosingInstructions(request.getDosingInstructions());
        existingMedicationAdministration.setDose(request.getDose());
        existingMedicationAdministration.setDoseUnits(getConcept(request.getDoseUnits()));
        existingMedicationAdministration.setRoute(getConcept(request.getRoute()));
        existingMedicationAdministration.setSite(getConcept(request.getSite()));

        // Replace performers; the performers set has cascade="all, delete-orphan" so removed rows are
        // deleted. Hibernate requires the persistent collection to be mutated in place, never replaced.
        List<MedicationAdministrationPerformer> performers = buildPerformers(request.getProviders(), false);
        if (existingMedicationAdministration.getPerformers() == null) {
            existingMedicationAdministration.setPerformers(new HashSet<>(performers));
        } else {
            existingMedicationAdministration.getPerformers().clear();
            existingMedicationAdministration.getPerformers().addAll(performers);
        }

        return existingMedicationAdministration;
    }

    private List<MedicationAdministrationPerformer> buildPerformers(List<MedicationAdministrationPerformerRequest> providerRequests,
                                                                    boolean copyUuid) {
        List<MedicationAdministrationPerformer> performers = new ArrayList<>();
        if (providerRequests == null) {
            return performers;
        }
        for (MedicationAdministrationPerformerRequest performer : providerRequests) {
            MedicationAdministrationPerformer newProvider = new MedicationAdministrationPerformer();
            if (copyUuid && StringUtils.isNotBlank(performer.getUuid())) {
                newProvider.setUuid(performer.getUuid());
            }
            newProvider.setActor(Context.getProviderService().getProviderByUuid(performer.getProviderUuid()));
            newProvider.setFunction(getConcept(performer.getFunction()));
            performers.add(newProvider);
        }
        return performers;
    }

    private List<MedicationAdministrationNote> buildNotes(List<MedicationAdministrationNoteRequest> noteRequests, boolean copyUuid) {
        List<MedicationAdministrationNote> notes = new ArrayList<>();
        if (noteRequests == null) {
            return notes;
        }
        for (MedicationAdministrationNoteRequest note : noteRequests) {
            MedicationAdministrationNote newNote = new MedicationAdministrationNote();
            if (copyUuid && StringUtils.isNotBlank(note.getUuid())) {
                newNote.setUuid(note.getUuid());
            }
            newNote.setAuthor(Context.getProviderService().getProviderByUuid(note.getAuthorUuid()));
            newNote.setText(note.getText());
            newNote.setRecordedTime(note.getRecordedTimeAsLocaltime());
            notes.add(newNote);
        }
        return notes;
    }

    /**
     * B5: scheduled administrations are created from a MedicationRequest without an explicit
     * context, so derive the encounter from the order. This keeps the standard FHIR
     * {@code context} search parameter meaningful.
     */
    private Encounter resolveEncounter(String encounterUuid, DrugOrder drugOrder) {
        if (StringUtils.isNotBlank(encounterUuid)) {
            return Context.getEncounterService().getEncounterByUuid(encounterUuid);
        }
        return drugOrder != null ? drugOrder.getEncounter() : null;
    }

    private DrugOrder getDrugOrder(String orderUuid) {
        if (StringUtils.isBlank(orderUuid)) {
            return null;
        }
        Order order = Context.getOrderService().getOrderByUuid(orderUuid);
        return order instanceof DrugOrder ? (DrugOrder) order : null;
    }

    private Patient getPatient(String patientUuid) {
        return StringUtils.isBlank(patientUuid) ? null : Context.getPatientService().getPatientByUuid(patientUuid);
    }

    private Drug getDrug(String drugUuid) {
        return StringUtils.isBlank(drugUuid) ? null : Context.getConceptService().getDrugByUuid(drugUuid);
    }

    private Concept getConcept(String conceptName) {
        return StringUtils.isBlank(conceptName) ? null : Context.getConceptService().getConceptByName(conceptName);
    }

    private Date toDate(Long epochSeconds) {
        return epochSeconds == null ? null : new Date(TimeUnit.SECONDS.toMillis(epochSeconds));
    }

    public MedicationAdministrationResponse mapMedicationAdministrationToResponse(org.hl7.fhir.r4.model.MedicationAdministration fhirMedicationAdministration) {
        MedicationAdministration openmrsMedicationAdministration = (MedicationAdministration) medicationAdministrationTranslator.toOpenmrsType(fhirMedicationAdministration);
        MedicationAdministrationResponse response =  MedicationAdministrationResponse.createFrom(openmrsMedicationAdministration);
        return response;
    }


}
