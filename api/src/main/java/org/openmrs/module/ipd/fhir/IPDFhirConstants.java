package org.openmrs.module.ipd.fhir;

public final class IPDFhirConstants {

	private IPDFhirConstants() {
	}

	/** Search parameter name for MedicationAdministration category filter (e.g. ?category=emergency). */
	public static final String SP_MEDICATION_ADMIN_CATEGORY = "category";

	/**
	 * Custom search parameter for a visit uuid. MedicationAdministration has no native visit
	 * parameter, so this is exposed as a Bahmni extension; the standard {@code context} parameter
	 * remains an Encounter reference.
	 */
	public static final String SP_MEDICATION_ADMIN_VISIT = "visit";

	/** Code used by the frontend to request only emergency/adhoc medication administrations. */
	public static final String MEDICATION_ADMIN_CATEGORY_EMERGENCY = "emergency";

	/** Extension on MedicationAdministration (scheduled only): valueString = uuid of the Slot being administered. */
	public static final String EXT_MEDICATION_ADMIN_SLOT = "http://fhir.openmrs.org/ext/ipd/medication-administration/slot";

	/** Extension on MedicationAdministration.note: valueString = uuid of the note being amended (validated, not trusted). */
	public static final String EXT_NOTE_PREVIOUS_NOTE_ID = "http://fhir.openmrs.org/ext/ipd/note/previous-note-id";

	/** Extension on MedicationAdministration.note: valueCodeableConcept whose coding code = concept uuid of the amendment reason. */
	public static final String EXT_NOTE_AMENDMENT_REASON = "http://fhir.openmrs.org/ext/ipd/note/amendment-reason";
}
