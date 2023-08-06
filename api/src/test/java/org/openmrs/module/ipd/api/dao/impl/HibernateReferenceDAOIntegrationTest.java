package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.ReferenceDAO;
import org.openmrs.module.ipd.api.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class HibernateReferenceDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReferenceDAO referenceDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void shouldSaveAndGetThePatientReference() {
        Reference openmrsReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference reference = referenceDAO.saveReference(openmrsReference);

        Optional<Reference> savedReference = referenceDAO.getReferenceByTypeAndTargetUUID(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");

        Assertions.assertTrue(savedReference.isPresent());
        Assertions.assertEquals("2c33920f-7aa6-0000-998a-60412d8ff7d5", savedReference.get().getTargetUuid());
        Assertions.assertEquals("org.openmrs.Patient", savedReference.get().getType());
        Assertions.assertEquals("org.openmrs.Patient/2c33920f-7aa6-0000-998a-60412d8ff7d5", savedReference.get().getName());

        sessionFactory.getCurrentSession().delete(reference);
    }
}