package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
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
    public void shouldGetAdmittedPatientsByLocation() {

        Location location= Context.getService(LocationService.class).getLocationByUuid("7779d653-393b-4118-9c83-a3715b82d4ac");
        List<AdmittedPatient> admittedPatients= wardDAO.getAdmittedPatientsByLocation(location,0,10);

        Assertions.assertEquals(0, admittedPatients.size());

    }
}
