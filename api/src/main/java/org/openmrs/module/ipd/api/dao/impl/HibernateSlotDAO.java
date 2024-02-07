package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.query.Query;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.dao.SlotDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class HibernateSlotDAO implements SlotDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateSlotDAO.class);
	
	private final SessionFactory sessionFactory;

	@Autowired
	public HibernateSlotDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Slot getSlot(Integer slotId) throws DAOException {
		return sessionFactory.getCurrentSession().get(Slot.class, slotId);
	}

	@Override
	public Slot getSlotByUUID(String uuid) throws DAOException {
		Slot s = (Slot)this.sessionFactory.getCurrentSession().createQuery("from Slot s where s.uuid = :uuid").setString("uuid", uuid).uniqueResult();
		return s;
	}

	@Override
	public Slot saveSlot(Slot slot) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(slot);
		return slot;
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIdAndForDateAndServiceType(Reference subject, LocalDate forDate, Concept serviceType) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Slot slot WHERE slot.schedule.subject=:subject and YEAR(slot.startDateTime)=:forYear and MONTH(slot.startDateTime)=:forMonth and DAY(slot.startDateTime)=:forDay and slot.serviceType=:serviceType and slot.voided=0");

		query.setParameter("subject", subject);
		query.setParameter("forYear", forDate.getYear());
		query.setParameter("forMonth", forDate.getMonthValue());
		query.setParameter("forDay", forDate.getDayOfMonth());
		query.setParameter("serviceType", serviceType);

		return query.getResultList();
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIdAndServiceType(Reference subject, Concept serviceType) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Slot slot WHERE slot.schedule.subject=:subject and slot.serviceType=:serviceType and slot.voided=0");

		query.setParameter("subject", subject);
		query.setParameter("serviceType", serviceType);

		return query.getResultList();
	}

	@Override
	public List<Slot> getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(Reference subject, Concept serviceType, List<String> orderUuids) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Slot slot " +
						"WHERE slot.schedule.subject=:subject and " +
						"slot.serviceType=:serviceType and"
						+ " slot.order.uuid IN :orderUuids and "
				         + "slot.voided = 0");

		query.setParameter("subject", subject);
		query.setParameter("serviceType", serviceType);
		query.setParameter("orderUuids", orderUuids);

		return query.getResultList();
	}

	@Override
	public List<Slot> getSlotsByPatientAndVisitAndServiceType(Reference subject, Visit visit, Concept serviceType) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("FROM Slot slot WHERE slot.schedule.subject=:subject and slot.schedule.visit=:visit and slot.serviceType=:serviceType");

		query.setParameter("subject", subject);
		query.setParameter("visit", visit);
		query.setParameter("serviceType", serviceType);

		return query.getResultList();
	}
    @Override
    public List<Slot> getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(Reference subject, LocalDateTime localStartDate, LocalDateTime localEndDate, Visit visit, Concept serviceType){
        Query query = sessionFactory.getCurrentSession()
                .createQuery("FROM Slot slot WHERE slot.schedule.subject=:subject and ((slot.startDateTime BETWEEN :startDate and :endDate) or (slot.serviceType = :serviceType)) and slot.voided=0 and slot.schedule.visit=:visit");

        query.setParameter("subject", subject);
        query.setParameter("startDate", localStartDate);
        query.setParameter("endDate", localEndDate);
		query.setParameter("visit", visit);
		query.setParameter("serviceType", serviceType);

        return query.getResultList();
    }

	@Override
	public List<Slot> getSlotsBySubjectIncludingAdministeredTimeFrame(Reference subject, LocalDateTime localStartDate, LocalDateTime localEndDate, Visit visit) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("SELECT slot FROM Slot slot " +
						"LEFT JOIN slot.medicationAdministration medAdmin " +
						"WHERE (slot.schedule.subject = :subject) AND slot.voided = 0 AND slot.schedule.visit=:visit AND " +
						"(((slot.startDateTime BETWEEN :startDateTime AND :endDateTime) AND " +
						"(medAdmin.administeredDateTime BETWEEN :startDate AND :endDate or medAdmin is null)) OR " +
						"(medAdmin is not null AND " +
						"(medAdmin.administeredDateTime BETWEEN :startDate AND :endDate)))");

		query.setParameter("subject", subject);
		query.setParameter("visit", visit);
		query.setParameter("startDateTime", localStartDate);
		query.setParameter("endDateTime", localEndDate);
		query.setParameter("startDate", DateTimeUtil.convertLocalDateTimeDate(localStartDate));
		query.setParameter("endDate", DateTimeUtil.convertLocalDateTimeDate(localEndDate));

		return query.getResultList();
	}
}
