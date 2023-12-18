package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private Long effectiveDateTime;

    public Date getEffectiveDateTimeAsLocaltime() {
        return this.effectiveDateTime != null ? new Date(TimeUnit.SECONDS.toMillis(this.effectiveDateTime)): null;
    }

}
