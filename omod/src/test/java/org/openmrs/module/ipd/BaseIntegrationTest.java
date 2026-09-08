/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"})
public abstract class BaseIntegrationTest extends BaseWebControllerTest {
}
