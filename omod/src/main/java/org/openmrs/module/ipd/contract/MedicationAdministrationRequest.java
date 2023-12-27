package org.openmrs.module.ipd.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationAdministrationRequest {

    private String uuid;
    private String patientUuid;
    private String encounterUuid;
    private String orderUuid;
    private List<MedicationAdministrationPerformerRequest> providers;
    private List<MedicationAdministrationNoteRequest> notes;
    private String status;
    private String statusReason;
    private String drugUuid;
    private String dosingInstructions;
    private Double dose;
    private String doseUnitsUuid;
    private String routeUuid;
    private String siteUuid;
    private Long administeredDateTime;
    private String slotUuid;
    private String scheduleUuid;

    public Date getAdministeredDateTimeAsLocaltime() {
        return this.administeredDateTime != null ? new Date(TimeUnit.SECONDS.toMillis(this.administeredDateTime)): null;
    }

}
