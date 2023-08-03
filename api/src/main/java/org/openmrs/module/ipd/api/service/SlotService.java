package org.openmrs.module.ipd.api.service;

import org.openmrs.Concept;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface SlotService extends OpenmrsService {

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot getSlot(Integer slotId) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot saveSlot(Slot slot) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	void purgeSlot(Slot slot) throws APIException;

	List<Slot> getSlotByForReferenceAndForDateAndServiceType(String forReference, LocalDate forDate, Concept serviceType);
}
