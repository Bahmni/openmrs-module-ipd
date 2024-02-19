package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.openmrs.module.ipd.api.BaseIntegrationTest;
import org.openmrs.module.ipd.api.dao.WardDAO;
import org.openmrs.module.ipd.api.model.AdmittedPatient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class HibernateWardDAOIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WardDAO wardDAO;

    @Autowired
    private SessionFactory sessionFactory;


    @Test
    public void shouldGetAdmittedPatients() {
        List<AdmittedPatient> assignments= wardDAO.getAdmittedPatientsByLocation(null,null,null);

    }
}
