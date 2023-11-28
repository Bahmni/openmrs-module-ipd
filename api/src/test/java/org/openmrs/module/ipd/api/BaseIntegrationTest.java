package org.openmrs.module.ipd.api;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"})
public abstract class BaseIntegrationTest extends BaseModuleWebContextSensitiveTest {
}