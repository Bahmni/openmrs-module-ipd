package org.openmrs.module.ipd.fhir;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Guards the production wiring: fhir2 collects resource providers from the ROOT application context,
 * so the omod's moduleApplicationContext.xml must register the provider package. Loads the real file
 * (bean definitions only, nothing is instantiated).
 */
public class ModuleContextRegistrationTest {

    @Test
    public void moduleApplicationContext_registersFhirProviderAndItsCollaborators() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context).loadBeanDefinitions(new ClassPathResource("moduleApplicationContext.xml"));

        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirR4ResourceProvider"));
        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirRequestMapper"));
        assertTrue(context.containsBeanDefinition("medicationAdministrationAcknowledgementTaskHandler"));
        assertTrue(context.containsBeanDefinition("acknowledgementFhirTaskServicePostProcessor"));
        assertTrue(context.containsBeanDefinition("IPDMedicationAdministrationServiceImpl")
                || context.containsBeanDefinition("iPDMedicationAdministrationServiceImpl"));
        assertTrue(context.containsBeanDefinition("IPDVisitServiceImpl")
                || context.containsBeanDefinition("iPDVisitServiceImpl"));
    }
}
