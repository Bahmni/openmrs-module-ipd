package org.openmrs.module.ipd.web.service;

import org.openmrs.api.APIException;

/**
 * Raised when a note amendment conflicts with the current server state (the administration is
 * acknowledged/locked, or the client amended a note that is no longer the latest). It extends
 * {@link APIException} so the legacy REST controller keeps answering 400 unchanged, while the FHIR
 * provider can map it to 409.
 */
public class AmendmentConflictException extends APIException {

    public AmendmentConflictException(String message) {
        super(message);
    }
}
