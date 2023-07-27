package org.openmrs.module.ipd.api.util;

import org.openmrs.annotation.AddOnStartup;

public class PrivilegeConstants {

    @AddOnStartup(description = "Able to save ipd schedules")
    public static final String EDIT_IPD_SCHEDULES = "Edit IPD Schedules";

    @AddOnStartup(description = "Able to save ipd slots")
    public static final String EDIT_IPD_SLOTS = "Edit IPD Slots";

}
