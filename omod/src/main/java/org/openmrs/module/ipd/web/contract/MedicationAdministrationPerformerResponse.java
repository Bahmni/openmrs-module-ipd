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
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class MedicationAdministrationPerformerResponse {
    private String uuid;
    private Object provider;
    private String function;

    public static MedicationAdministrationPerformerResponse createFrom(MedicationAdministrationPerformer openmrsMedicationAdministrationPerformer) {
        String function = openmrsMedicationAdministrationPerformer.getFunction() != null ? openmrsMedicationAdministrationPerformer.getFunction().getDisplayString() : null;
        return MedicationAdministrationPerformerResponse.builder()
                .uuid(openmrsMedicationAdministrationPerformer.getUuid())
                .provider(ConversionUtil.convertToRepresentation(openmrsMedicationAdministrationPerformer.getActor(), Representation.REF))
                .function(function)
                .build();
    }
}
