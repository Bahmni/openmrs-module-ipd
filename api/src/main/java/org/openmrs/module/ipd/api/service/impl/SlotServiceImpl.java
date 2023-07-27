package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SlotServiceImpl extends BaseOpenmrsService implements SlotService {
	
	private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
	
	private final SlotDAO slotDAO;

	@Autowired
	public SlotServiceImpl(SlotDAO slotDAO) {
		this.slotDAO = slotDAO;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Slot getSlot(Integer slotId) throws APIException {
		return slotDAO.getSlot(slotId);
	}
	
	@Override
	public Slot saveSlot(Slot slot) throws APIException {
		return slotDAO.saveSlot(slot);
	}

	@Override
	public void purgeSlot(Slot slot) throws APIException {
		slotDAO.purgeSlot(slot);
	}
}