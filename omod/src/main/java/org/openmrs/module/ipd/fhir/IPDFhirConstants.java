package org.openmrs.module.ipd.fhir;

public final class IPDFhirConstants {

	private IPDFhirConstants() {
	}

	/** Search parameter name for MedicationAdministration category filter (e.g. ?category=emergency). */
	public static final String SP_MEDICATION_ADMIN_CATEGORY = "category";

	/** Code used by the frontend to request only emergency/adhoc medication administrations. */
	public static final String MEDICATION_ADMIN_CATEGORY_EMERGENCY = "emergency";
}
