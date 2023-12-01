package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationRequest {

    private String patientUuid;
    private String orderUuid;
    private String providerUuid;
    private String notes;
    private String status;
    private String slotUuid;
        private Date effectiveDateTime;
}
