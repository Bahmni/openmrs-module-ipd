package org.openmrs.module.ipd.model;

import lombok.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.DrugOrder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrder {

    private DrugOrder drugOrder;
    private DrugOrderSchedule drugOrderSchedule;

    public static IPDDrugOrder createFrom(DrugOrder drugOrder,DrugOrderSchedule drugOrderSchedule){
        return IPDDrugOrder.builder().
                drugOrder(drugOrder).
                drugOrderSchedule(drugOrderSchedule).
                build();
    }
}
