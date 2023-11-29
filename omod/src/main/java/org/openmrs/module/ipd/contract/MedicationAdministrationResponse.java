package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.MedicationAdministration;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.factory.SlotFactory;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationResponse {

    private String uuid;
    private String notes;
    private Date effectiveDateTime;
    private Object order;
    private String status;
    private Object slot;
    private String patientUuid;
    private String providerUuid;

}

