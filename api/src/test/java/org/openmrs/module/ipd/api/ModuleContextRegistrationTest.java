package org.openmrs.module.ipd.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class ModuleContextRegistrationTest {

    @Test
    public void moduleApplicationContext_registersMovedServicesAndFhirProvider() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context)
                .loadBeanDefinitions(new ClassPathResource("moduleApplicationContext.xml"));

        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirR4ResourceProvider"));
        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirRequestMapper"));
        assertTrue(context.containsBeanDefinition("IPDMedicationAdministrationServiceImpl")
                || context.containsBeanDefinition("iPDMedicationAdministrationServiceImpl"));
        assertTrue(context.containsBeanDefinition("IPDVisitServiceImpl")
                || context.containsBeanDefinition("iPDVisitServiceImpl"));
        assertTrue(context.containsBeanDefinition("IPDTransactionHandler")
                || context.containsBeanDefinition("iPDTransactionHandler"));
        assertTrue(context.containsBeanDefinition("MedicationAdministrationFactory")
                || context.containsBeanDefinition("medicationAdministrationFactory"));
        assertTrue(context.containsBeanDefinition("AcknowledgementTaskMapper")
                || context.containsBeanDefinition("acknowledgementTaskMapper"));
    }
}
