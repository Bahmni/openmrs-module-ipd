package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Location;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.openmrs.module.ipd.api.model.WardPatientsSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

@Repository
public class HibernateWardDAO implements WardDAO {

    private static final Logger log = LoggerFactory.getLogger(HibernateWardDAO.class);

    private final SessionFactory sessionFactory;

    @Autowired
    public HibernateWardDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AdmittedPatient> getAdmittedPatients(Location location) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Query query = session.createQuery("select NEW org.openmrs.module.ipd.api.model.AdmittedPatient(assignment," +
                    "(COUNT(DISTINCT o.orderId) - COUNT (DISTINCT s.order.orderId)))" +
                    "from org.openmrs.module.bedmanagement.entity.BedPatientAssignment assignment " +
                    "JOIN org.openmrs.Visit v on v.patient = assignment.patient " +
                    "JOIN org.openmrs.Encounter e on e.visit = v " +
                    "JOIN org.openmrs.module.bedmanagement.entity.BedLocationMapping locmap on locmap.bed = assignment.bed " +
                    "JOIN org.openmrs.Location l on locmap.location = l " +
                    "LEFT JOIN org.openmrs.Order o on o.encounter = e " +
                    "LEFT JOIN Slot s on s.order = o " +
                    "where assignment.endDatetime is null and v.stopDatetime is null and l.parentLocation = :location  " +
                    "GROUP BY assignment.patient, v " +
                    "ORDER BY assignment.startDatetime desc");

            query.setParameter("location", location);
            return query.getResultList();
        }
        catch (Exception ex){
            log.error("Exception at WardDAO getAdmittedPatients ",ex.getStackTrace());
        }
        return new ArrayList<>();
    }

    @Override
    public WardPatientsSummary getWardPatientSummary(Location location) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            Query query = session.createQuery(
                    "select NEW org.openmrs.module.ipd.api.model.WardPatientsSummary(COUNT(assignment)) " +
                            "from org.openmrs.module.bedmanagement.entity.BedPatientAssignment assignment " +
                            "JOIN org.openmrs.module.bedmanagement.entity.BedLocationMapping locmap on locmap.bed = assignment.bed " +
                            "JOIN org.openmrs.Location l on locmap.location = l " +
                            "JOIN org.openmrs.Visit v on v.patient = assignment.patient " +
                            "where assignment.endDatetime is null and v.stopDatetime is null and l.parentLocation = :location");
            query.setParameter("location", location);
            return (WardPatientsSummary) query.getSingleResult();
        } catch (Exception e) {
            log.error("Exception at WardDAO getAdmittedPatients ",e.getStackTrace());
        }
        return new WardPatientsSummary();
    }

}
