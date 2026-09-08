/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.service;

import org.hl7.fhir.r4.model.MedicationAdministration;
import org.openmrs.module.fhirExtension.model.Task;
import org.openmrs.module.ipd.api.model.MedicationAdministrationNote;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationAcknowledgementRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationNoteRequest;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;

public interface IPDMedicationAdministrationService {

    MedicationAdministration saveScheduledMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministration updateAdhocMedicationAdministration(String uuid,MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministration saveAdhocMedicationAdministration(MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministrationNote amendNote(String medicationAdministrationUuid, MedicationAdministrationNoteRequest noteRequest);

    Task acknowledge(String medicationAdministrationUuid, MedicationAdministrationAcknowledgementRequest acknowledgementRequest);
}
