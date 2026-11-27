package org.openmrs.module.ipd.fhir;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class ModuleContextRegistrationTest {

    @Test
    public void webModuleApplicationContext_registersControllers() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context)
                .loadBeanDefinitions(new ClassPathResource("webModuleApplicationContext.xml"));

        assertTrue(context.containsBeanDefinition("IPDMedicationAdministrationController")
                || context.containsBeanDefinition("iPDMedicationAdministrationController"));
        assertTrue(context.containsBeanDefinition("IPDScheduleController")
                || context.containsBeanDefinition("iPDScheduleController"));
        assertTrue(context.containsBeanDefinition("IPDVisitController")
                || context.containsBeanDefinition("iPDVisitController"));
        assertTrue(context.containsBeanDefinition("IPDWardController")
                || context.containsBeanDefinition("iPDWardController"));
        assertTrue(context.containsBeanDefinition("IPDCareTeamController")
                || context.containsBeanDefinition("iPDCareTeamController"));
    }
}
