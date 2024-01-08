package org.openmrs.module.ipd.api.service;

import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface SlotService extends OpenmrsService {

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot getSlot(Integer slotId) throws APIException;

	//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot getSlotByUUID(String uuid) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot saveSlot(Slot slot) throws APIException;

	List<Slot> getSlotsBySubjectReferenceIdAndForDateAndServiceType(Reference subject, LocalDate forDate, Concept serviceType);

	List<Slot> getSlotsBySubjectReferenceIdAndServiceType(Reference subject, Concept serviceType);

	List<Slot> getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType, List<String> orderUuids);

	void voidSlot(Slot slot,String voidReason);

	List<Slot> getSlotsByPatientAndVisitAndServiceType(Reference subject, Visit visit, Concept serviceType);

	List<Slot> getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(Reference reference, LocalDateTime localStartDate, LocalDateTime localEndDate);
}
