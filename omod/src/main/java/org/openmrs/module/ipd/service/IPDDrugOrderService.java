package org.openmrs.module.ipd.service;

import org.openmrs.module.ipd.api.model.IPDDrugOrder;

import java.util.Date;
import java.util.List;

public interface IPDDrugOrderService {

    List<IPDDrugOrder> getPrescribedOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);
}
