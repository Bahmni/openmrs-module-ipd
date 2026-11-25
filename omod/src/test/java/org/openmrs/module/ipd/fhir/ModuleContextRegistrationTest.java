package org.openmrs.module.ipd.fhir;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Guards the production wiring: fhir2 collects resource providers from the ROOT application context,
 * which OpenMRS 2.8.x builds from both {@code classpath*:/moduleApplicationContext.xml} and
 * {@code classpath*:/webModuleApplicationContext.xml}. The omod's
 * {@code webModuleApplicationContext.xml} must therefore register the FHIR provider package. Loads
 * the real file (bean definitions only, nothing is instantiated).
 */
public class ModuleContextRegistrationTest {

    @Test
    public void webModuleApplicationContext_registersFhirProviderAndItsCollaborators() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context)
                .loadBeanDefinitions(new ClassPathResource("webModuleApplicationContext.xml"));

        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirR4ResourceProvider"));
        assertTrue(context.containsBeanDefinition("medicationAdministrationFhirRequestMapper"));
        assertTrue(context.containsBeanDefinition("IPDMedicationAdministrationServiceImpl")
                || context.containsBeanDefinition("iPDMedicationAdministrationServiceImpl"));
        assertTrue(context.containsBeanDefinition("IPDVisitServiceImpl")
                || context.containsBeanDefinition("iPDVisitServiceImpl"));
    }

    @Test
    public void webModuleApplicationContext_registersWebBeans() {
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context)
                .loadBeanDefinitions(new ClassPathResource("webModuleApplicationContext.xml"));

        assertTrue(context.containsBeanDefinition("IPDMedicationAdministrationController")
                || context.containsBeanDefinition("iPDMedicationAdministrationController"));
        assertTrue(context.containsBeanDefinition("IPDTransactionHandler")
                || context.containsBeanDefinition("iPDTransactionHandler"));
        assertTrue(context.containsBeanDefinition("MedicationAdministrationFactory")
                || context.containsBeanDefinition("medicationAdministrationFactory"));
        assertTrue(context.containsBeanDefinition("AcknowledgementTaskMapper")
                || context.containsBeanDefinition("acknowledgementTaskMapper"));
    }
}
