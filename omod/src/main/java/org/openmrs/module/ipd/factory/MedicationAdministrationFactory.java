package org.openmrs.module.ipd.factory;

import org.hl7.fhir.r4.model.*;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.contract.MedicationAdministrationResponse;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class MedicationAdministrationFactory {

    private final ConceptService conceptService;
    private final ReferenceService referenceService;
    private SlotService slotService;

    @Autowired
    public MedicationAdministrationFactory(ConceptService conceptService, ReferenceService referenceService,SlotService slotService) {
        this.conceptService = conceptService;
        this.referenceService = referenceService;
        this.slotService = slotService;
    }

    public MedicationAdministration createMedicationAdministrationFrom(MedicationAdministrationRequest request) {
        MedicationAdministration medicationAdministration = new MedicationAdministration();

        medicationAdministration.setEffective(new DateTimeType(request.getEffectiveDateTime()));

        medicationAdministration.setStatus(MedicationAdministration.MedicationAdministrationStatus.valueOf(request.getStatus()));
        medicationAdministration
                .setSubject(new Reference("Patient/"+request.getPatientUuid()));

        MedicationAdministration.MedicationAdministrationPerformerComponent performer= new MedicationAdministration.MedicationAdministrationPerformerComponent();
        performer.setActor(new Reference("Provider/"+request.getProviderUuid()));
        List<MedicationAdministration.MedicationAdministrationPerformerComponent> performers=new ArrayList<>();
        performers.add(performer);
        medicationAdministration.setPerformer(performers);

        medicationAdministration.setRequest(new Reference("MedicationRequest/"+request.getOrderUuid()));

        Annotation annotation= new Annotation(new MarkdownType(request.getNotes()));
        List<Annotation> annotations= new ArrayList<>();
        annotations.add(annotation);
        medicationAdministration.setNote(annotations);

        List<Reference> supportedInfo= new ArrayList<>();
        supportedInfo.add(new Reference("Slot/"+request.getSlotUuid()));

        return medicationAdministration;
    }

    public MedicationAdministrationResponse createFrom(MedicationAdministration medicationAdministration) {
        String patientUuid = null;
        String providerUuid = null;
        String slotUuid = medicationAdministration.getSupportingInformation().get(0).getReference().split("/")[1];
        if(medicationAdministration.getSubject() !=null){
            patientUuid = medicationAdministration.getSubject().getReference().split("/")[1];
        }
        if(medicationAdministration.getPerformer() !=null){
            providerUuid = medicationAdministration.getPerformer().get(0).getActor().getReference().split("/")[1];
        }
        return MedicationAdministrationResponse.builder()
                .uuid(medicationAdministration.getId())
                .notes(medicationAdministration.getNote().get(0).getText())
                .effectiveDateTime(medicationAdministration.getEffectiveDateTimeType().getValue())
                .status(medicationAdministration.getStatus().toString())
                .order(ConversionUtil.convertToRepresentation(medicationAdministration.getRequest(), Representation.FULL))
                .slot(ConversionUtil.convertToRepresentation(slotService.getSlotByUUID(slotUuid), Representation.FULL))
                .patientUuid(patientUuid)
                .providerUuid(providerUuid)
                .build();
    }



}
