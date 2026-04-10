/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import static org.openmrs.module.ipd.api.util.DateTimeUtil.convertLocalDateTimeToUTCEpoc;

@Builder
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@AllArgsConstructor
@NoArgsConstructor
public class MedicationSlotResponse {
    private Integer id;
    private String uuid;
    private String serviceType;
    private String status;
    private long startTime;
    private Object order;
    private Object medicationAdministration;
    private String notes;

    public static MedicationSlotResponse createFrom(Slot slot) {
        return MedicationSlotResponse.builder()
                .id(slot.getId())
                .uuid(slot.getUuid())
                .serviceType(slot.getServiceType().getName().getName())
                .status(slot.getStatus().name())
                .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                .order(ConversionUtil.convertToRepresentation(slot.getOrder(), Representation.FULL))
                .medicationAdministration(MedicationAdministrationResponse.createFrom((slot.getMedicationAdministration())))
                .notes(slot.getNotes())
                .build();
    }

    public static MedicationSlotResponse createFrom(Slot slot, Representation rep) {
        if (rep.equals(Representation.REF))
        {
            return MedicationSlotResponse.builder()
                    .id(slot.getId())
                    .uuid(slot.getUuid())
                    .serviceType(slot.getServiceType().getName().getName())
                    .status(slot.getStatus().name())
                    .startTime(convertLocalDateTimeToUTCEpoc(slot.getStartDateTime()))
                    .medicationAdministration(MedicationAdministrationResponse.createFrom((slot.getMedicationAdministration())))
                    .notes(slot.getNotes())
                    .build();
        }
        return MedicationSlotResponse.createFrom(slot);

    }
}
