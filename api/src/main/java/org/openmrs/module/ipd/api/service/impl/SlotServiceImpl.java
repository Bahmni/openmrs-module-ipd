package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Concept;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
	public Slot getSlotByUUID(String uuid) throws APIException {
		return slotDAO.getSlotByUUID(uuid);
	}

	@Override
	public Slot saveSlot(Slot slot) throws APIException {
		return slotDAO.saveSlot(slot);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Slot> getSlotsBySubjectReferenceIdAndForDateAndServiceType(Reference subject, LocalDate forDate, Concept serviceType) {
		return slotDAO.getSlotsBySubjectReferenceIdAndForDateAndServiceType(subject, forDate, serviceType);
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIdAndServiceType(Reference subject, Concept serviceType) {
		return slotDAO.getSlotsBySubjectReferenceIdAndServiceType(subject, serviceType);
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType, List<String> orderUuids) {
		return slotDAO.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(subject, serviceType, orderUuids);
	}
    @Override
    public List<Slot> getSlotsBySubjectReferenceIdAndForTheGivenTimeFrameAndServiceType(Reference subject, LocalDateTime localStartDate, LocalDateTime localEndDate, Concept serviceType) {
		return slotDAO.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrameAndServiceType(subject, localStartDate, localEndDate, serviceType);
	}
}
