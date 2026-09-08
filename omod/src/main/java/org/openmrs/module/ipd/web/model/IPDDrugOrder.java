/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.model;

import lombok.*;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrder {

    private BahmniDrugOrder bahmniDrugOrder;
    private DrugOrderSchedule drugOrderSchedule;

    public static IPDDrugOrder createFrom(BahmniDrugOrder bahmniDrugOrder,DrugOrderSchedule drugOrderSchedule){
        return IPDDrugOrder.builder().
                bahmniDrugOrder(bahmniDrugOrder).
                drugOrderSchedule(drugOrderSchedule).
                build();
    }
}
