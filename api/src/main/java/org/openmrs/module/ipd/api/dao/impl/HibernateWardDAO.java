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

    private static String generateGroupByClauseForSorting(String sortBy) {
        String groupBy = " GROUP BY assignment.patient, v ";

        switch (sortBy) {
            case "bedNumber":
                groupBy += " ORDER BY assignment.bed.bedNumber ";
                break;
            default:
                groupBy += " ORDER BY assignment.startDatetime desc ";
                break;
        }
        return groupBy;
    }

    @Override
    public List<AdmittedPatient> getAdmittedPatientsByLocation(Location location, String sortBy) {
        Session session = this.sessionFactory.getCurrentSession();
        try {
            String queryString = "select NEW org.openmrs.module.ipd.api.model.AdmittedPatient(assignment," +
                    "(COUNT(DISTINCT o.orderId) - COUNT (DISTINCT s.order.orderId)), careTeam)" +
                    "from org.openmrs.module.bedmanagement.entity.BedPatientAssignment assignment " +
                    "JOIN org.openmrs.Visit v on v.patient = assignment.patient " +
                    "JOIN org.openmrs.Encounter e on e.visit = v " +
                    "LEFT JOIN CareTeam careTeam on careTeam.visit = v " +
                    "JOIN org.openmrs.module.bedmanagement.entity.BedLocationMapping locmap on locmap.bed = assignment.bed " +
                    "JOIN org.openmrs.Location l on locmap.location = l " +
                    "LEFT JOIN org.openmrs.Order o on o.encounter = e " +
                    "LEFT JOIN Slot s on s.order = o " +
                    "where assignment.endDatetime is null and v.stopDatetime is null and l.parentLocation = :location  ";

            String groupBy = generateGroupByClauseForSorting(sortBy);


            String finalQuery = queryString + groupBy;

            Query query = session.createQuery(finalQuery);

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

    @Override
    public List<AdmittedPatient> searchAdmittedPatients(Location location, List<String> searchKeys, String searchValue, String sortBy) {
        try {
            Session session = sessionFactory.getCurrentSession();

            String selectQuery = "select NEW org.openmrs.module.ipd.api.model.AdmittedPatient(assignment," +
                    "(COUNT(DISTINCT o.orderId) - COUNT (DISTINCT s.order.orderId))) " +
                    "from org.openmrs.module.bedmanagement.entity.BedPatientAssignment assignment " +
                    "JOIN org.openmrs.Visit v on v.patient = assignment.patient " +
                    "JOIN org.openmrs.Patient p on assignment.patient = p " +
                    "JOIN org.openmrs.Person pr on pr.personId=p.patientId " +
                    "JOIN org.openmrs.Encounter e on e.visit = v " +
                    "JOIN org.openmrs.module.bedmanagement.entity.BedLocationMapping locmap on locmap.bed = assignment.bed " +
                    "JOIN org.openmrs.Location l on locmap.location = l " +
                    "LEFT JOIN org.openmrs.Order o on o.encounter = e " +
                    "LEFT JOIN Slot s on s.order = o ";

            // Construct additional joins and where clause based on search keys
            StringBuilder additionalJoins = new StringBuilder("");
            StringBuilder whereClause = new StringBuilder("");
            generateSQLSearchConditions(searchKeys,additionalJoins,whereClause);

            // Construct group by clause
            String groupBy = generateGroupByClauseForSorting(sortBy);

            // Create query
            Query query = session.createQuery(selectQuery + additionalJoins + whereClause + groupBy);

            // Set parameters
            query.setParameter("location", location);
            setQueryParameters(query, searchKeys, searchValue);

            return query.getResultList();
        } catch (Exception ex) {
            log.error("Exception at WardDAO searching ", ex.getMessage());
            return new ArrayList<>();
        }
    }

    private void generateSQLSearchConditions(List<String> searchKeys,StringBuilder additionalJoins,StringBuilder whereClause) {
        whereClause.append("where (assignment.endDatetime is null and v.stopDatetime is null and l.parentLocation = :location)");
        if (searchKeys != null && !searchKeys.isEmpty()) {
            whereClause.append(" and (");
            for (int i = 0; i < searchKeys.size(); i++) {
                switch (searchKeys.get(i)) {
                    case "bedNumber":
                        whereClause.append(" assignment.bed.bedNumber LIKE :bedNumber ");
                        break;
                    case "patientIdentifier":
                        additionalJoins.append(" JOIN p.identifiers pi ");
                        whereClause.append(" pi.identifier LIKE :patientIdentifier ");
                        break;
                    case "patientName":
                        additionalJoins.append(" JOIN pr.names prn ");
                        whereClause.append(" (prn.givenName LIKE :patientName or prn.middleName LIKE :patientName or prn.familyName LIKE :patientName) ");
                        break;
                }
                if (i < searchKeys.size() - 1) {
                    whereClause.append(" or ");
                }
            }
            whereClause.append(" ) ");
        }
    }

    private void setQueryParameters(Query query, List<String> searchKeys, String searchValue) {
        if (searchKeys != null && searchValue != null && !searchValue.isEmpty()) {
            if (searchKeys.contains("bedNumber")) {
                query.setParameter("bedNumber", "%" + searchValue + "%");
            }
            if (searchKeys.contains("patientIdentifier")) {
                query.setParameter("patientIdentifier", "%" + searchValue + "%");
            }
            if (searchKeys.contains("patientName")) {
                query.setParameter("patientName", "%" + searchValue + "%");
            }
        }
    }

    private StringBuilder appendORIfMoreSearchKeysPresent(int i,int size,StringBuilder whereClause){
        if (size==(i+1)){
            return whereClause;
        }
        return whereClause.append(" or ");
    }



}
