/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.web.util;

import org.openmrs.annotation.AddOnStartup;

public class PrivilegeConstants {

    @AddOnStartup(description = "Edit Medication Tasks description")
    public static final String EDIT_MEDICATION_TASKS = "Edit Medication Tasks";
    @AddOnStartup(description = "Delete Medication Tasks description")
    public static final String DELETE_MEDICATION_TASKS = "Delete Medication Tasks";
    @AddOnStartup(description = "Edit adhoc medication tasks description")
    public static final String EDIT_ADHOC_MEDICATION_TASKS = "Edit adhoc medication tasks";
    @AddOnStartup(description = "Edit Medication Administration description")
    public static final String EDIT_MEDICATION_ADMINISTRATION = "Edit Medication Administration";
    @AddOnStartup(description = "Get Medication Administration description")
    public static final String GET_MEDICATION_ADMINISTRATION = "Get Medication Administration";
    @AddOnStartup(description = "Get Medication Tasks description")
    public static final String GET_MEDICATION_TASKS = "Get Medication Tasks";
}
