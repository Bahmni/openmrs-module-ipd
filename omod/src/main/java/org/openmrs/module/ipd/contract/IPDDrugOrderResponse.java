package org.openmrs.module.ipd.contract;

import lombok.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.ipd.api.model.IPDDrugOrder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrderResponse {

    private VisitInfoResponse visit;
    private EncounterTransaction.DrugOrder drugOrder;
    private DrugOrderScheduleResponse drugOrderSchedule;

    public static IPDDrugOrderResponse createFrom(IPDDrugOrder ipdDrugOrder) {
        IPDDrugOrderResponse ipdDrugOrderResponse= IPDDrugOrderResponse.builder().
                visit(VisitInfoResponse.createFrom(ipdDrugOrder.getBahmniDrugOrder().getVisit())).
                drugOrder(ipdDrugOrder.getBahmniDrugOrder().getDrugOrder()).build();
        if (ipdDrugOrder.getDrugOrderSchedule() != null){
                ipdDrugOrderResponse.setDrugOrderSchedule(DrugOrderScheduleResponse.createFrom(ipdDrugOrder.getDrugOrderSchedule()));
        }
        return ipdDrugOrderResponse;
    }

    public boolean equals(Object otherOrder) {
        if (otherOrder == null) {
            return false;
        } else if (!(otherOrder instanceof IPDDrugOrderResponse)) {
            return false;
        } else {
            return super.equals(otherOrder);
        }
    }


}