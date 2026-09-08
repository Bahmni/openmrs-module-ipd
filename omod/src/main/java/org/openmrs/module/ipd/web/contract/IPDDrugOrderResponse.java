/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;

import lombok.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.ipd.web.model.IPDDrugOrder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrderResponse {

    private EncounterTransaction.DrugOrder drugOrder;
    private EncounterTransaction.Provider provider;
    private DrugOrderScheduleResponse drugOrderSchedule;

    public static IPDDrugOrderResponse createFrom(IPDDrugOrder ipdDrugOrder) {
        IPDDrugOrderResponse ipdDrugOrderResponse= IPDDrugOrderResponse.builder().
                drugOrder(ipdDrugOrder.getBahmniDrugOrder().getDrugOrder())
                .provider(ipdDrugOrder.getBahmniDrugOrder().getProvider())
                .build();
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
