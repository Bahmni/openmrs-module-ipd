package org.openmrs.module.ipd.contract;

import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.ipd.api.model.Annotation;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class MedicationAdministrationNoteResponse {
    private String uuid;
    private Object author;
    private Date recordedTime;
    private String text;

    public static MedicationAdministrationNoteResponse createFrom(Annotation openmrsAnnotation) {
        return MedicationAdministrationNoteResponse.builder()
                .uuid(openmrsAnnotation.getUuid())
                .author(ConversionUtil.convertToRepresentation(openmrsAnnotation.getAuthor(), Representation.REF))
                .recordedTime(openmrsAnnotation.getRecordedTime())
                .text(openmrsAnnotation.getText())
                .build();
    }
}
