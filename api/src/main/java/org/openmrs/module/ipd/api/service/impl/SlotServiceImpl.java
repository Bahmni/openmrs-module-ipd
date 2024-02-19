package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.ServiceType;
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
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SlotServiceImpl extends BaseOpenmrsService implements SlotService {
	
	private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
	
	private final SlotDAO slotDAO;
	private ConceptService conceptService;

	@Autowired
	public SlotServiceImpl(SlotDAO slotDAO, ConceptService conceptService) {

		this.slotDAO = slotDAO;
		this.conceptService = conceptService;
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
	public void voidSlot(Slot slot, String voidReason) throws APIException  {
		slot.setVoided(true);
		slot.setVoidedBy(Context.getAuthenticatedUser());
		slot.setDateVoided(new Date());
		slot.setVoidReason(voidReason);
		slotDAO.saveSlot(slot);
	}


	@Override
	public List<Slot> getSlotsByPatientAndVisitAndServiceType(Reference subject, Visit visit, Concept serviceType) {
		return slotDAO.getSlotsByPatientAndVisitAndServiceType(subject, visit, serviceType);
	}
    @Override
    public List<Slot> getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(Reference subject, LocalDateTime localStartDate, LocalDateTime localEndDate, Visit visit){
		Concept asNeededPlaceholderServiceType = conceptService.getConceptByName(ServiceType.AS_NEEDED_PLACEHOLDER.conceptName());
		return slotDAO.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(subject, localStartDate, localEndDate, visit, asNeededPlaceholderServiceType);
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(Reference subject, LocalDateTime localStartDate, LocalDateTime localEndDate, Visit visit) {
		return slotDAO.getSlotsBySubjectIncludingAdministeredTimeFrame(subject, localStartDate, localEndDate, visit);
	}

	@Override
	public List<Slot> getSlotsForPatientListByTime(List<String> patientUuidList, LocalDateTime localStartDate, LocalDateTime localEndDate) {
		return slotDAO.getSlotsForPatientListByTime(patientUuidList, localStartDate, localEndDate);
	}

	@Override
	public List<Slot> getImmediatePreviousSlotsForPatientListByTime(List<String> patientUuidList, LocalDateTime localStartDate) {
		return slotDAO.getImmediatePreviousSlotsForPatientListByTime(patientUuidList, localStartDate);
	}

	public List<Object[]> getSlotDurationForPatientsByOrder(List<Order> orders, List<Concept> serviceTypes) {
		return slotDAO.getSlotDurationForPatientsByOrder(orders, serviceTypes);
	}
}
