package org.openmrs.module.ipd.web.service;

import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;

import java.util.Date;
import java.util.List;

public interface IPDVisitService {

    /**
     * Returns the drug orders of the visit and of the OPD visit immediately preceding it, plus every other drug order of
     * the patient (from any visit) that was still effective at some point since the visit started, newest first.
     * For a closed visit, orders of other visits that were placed after it ended are left out.
     * <p>
     * {@code numberOfVisits}, {@code startDate}, {@code endDate} and {@code getEffectiveOrdersOnly} are only passed on to
     * the visit query. They are not applied to the patient-wide lookup, so they do not narrow the orders from other visits.
     */
    List<IPDDrugOrder> getPrescribedOrders(String visitUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);
    List<Slot> getMedicationSlots(String visitUuid, ServiceType serviceType);
}
