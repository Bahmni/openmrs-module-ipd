package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Location;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.hibernate.query.Query;
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
                    "GROUP BY assignment.patient, v");

            query.setParameter("location", location);
            return query.getResultList();
        }
        catch (Exception ex){
            System.out.println("Exception trace " + ex.getStackTrace());
        }
        return null;
    }

}
