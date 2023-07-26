package org.bahmni.module.ipd;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"})
public abstract class BaseIntegrationTest extends BaseWebControllerTest {
}
