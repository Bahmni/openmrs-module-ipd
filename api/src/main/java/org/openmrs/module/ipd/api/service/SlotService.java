package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

@Service
public interface SlotService extends OpenmrsService {

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot getSlot(Integer slotId) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot saveSlot(Slot slot) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	void purgeSlot(Slot slot) throws APIException;
}
