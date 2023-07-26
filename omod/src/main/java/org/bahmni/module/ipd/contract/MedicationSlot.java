package org.bahmni.module.ipd.contract;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class MedicationSlot {
    private String uuid;
    private String orderId;
    private String serviceType;
    private String status;
    private Date startTime;
    private Date endTime;
    private String notes;
}
