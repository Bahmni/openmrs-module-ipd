package org.openmrs.module.ipd.factory;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MarkdownType;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.apiext.translators.MedicationAdministrationTranslator;
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

    private MedicationAdministrationTranslator medicationAdministrationTranslator;

    @Autowired
    public MedicationAdministrationFactory(MedicationAdministrationTranslator medicationAdministrationTranslator) {
        this.medicationAdministrationTranslator = medicationAdministrationTranslator;
    }

    public MedicationAdministration createMedicationAdministrationFrom(MedicationAdministrationRequest request) {
        MedicationAdministration medicationAdministration = new MedicationAdministration();

        medicationAdministration.setEffective(new DateTimeType(request.getEffectiveDateTimeAsLocaltime()));

        medicationAdministration.setStatus(MedicationAdministration.MedicationAdministrationStatus.fromCode(request.getStatus()));
        medicationAdministration
                .setSubject(new Reference("Patient/"+request.getPatientUuid()));

        MedicationAdministration.MedicationAdministrationPerformerComponent performer= new MedicationAdministration.MedicationAdministrationPerformerComponent();
        performer.setActor(new Reference("Practitioner/"+request.getProviderUuid()));
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
        medicationAdministration.setSupportingInformation(supportedInfo);

        return medicationAdministration;
    }

    public MedicationAdministrationResponse createFrom(MedicationAdministration medicationAdministration) {
        String patientUuid = null;
        Provider provider = null;

        if(medicationAdministration.getSubject() !=null){
            patientUuid = medicationAdministration.getSubject().getReference().split("/")[1];
        }
        if(medicationAdministration.getPerformer() !=null){
            provider = Context.getProviderService().getProviderByUuid(medicationAdministration.getPerformer().get(0).getActor().getReference().split("/")[1]);
        }
        return MedicationAdministrationResponse.builder()
                .uuid(medicationAdministration.getId())
                .notes(medicationAdministration.getNote().get(0).getText())
                .administeredDateTime(medicationAdministration.getEffectiveDateTimeType().getValue())
                .status(medicationAdministration.getStatus().toCode())
               // .orderUuid(medicationAdministration.getRequest().getReference())
                .patientUuid(patientUuid)
                .provider(ConversionUtil.convertToRepresentation(provider, Representation.REF))
                .build();
    }


}
