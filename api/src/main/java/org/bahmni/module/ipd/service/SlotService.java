package org.bahmni.module.ipd.service;

import org.bahmni.module.ipd.model.Slot;
import org.bahmni.module.ipd.util.PrivilegeConstants;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.springframework.stereotype.Service;

@Service
public interface SlotService extends OpenmrsService {

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SCHEDULES })
	Slot getSlot(Integer slotId) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	Slot saveSlot(Slot slot) throws APIException;

//	@Authorized({ PrivilegeConstants.EDIT_IPD_SLOTS })
	void purgeSlot(Slot slot) throws APIException;
}
