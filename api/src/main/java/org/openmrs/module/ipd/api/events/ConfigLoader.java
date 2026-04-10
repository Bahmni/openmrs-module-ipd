/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.api.events.model.ConfigDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ConfigLoader {
    private List<ConfigDetail> configs = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ipd.events_config.file_path:/etc/bahmni_config/openmrs/apps/ipdDashboard/eventsConfig.json}")
    private String eventsConfigurationFileLocation;

    public List<ConfigDetail> getConfigs() {
        if (configs.isEmpty()) {
            loadConfiguration();
        }
        return this.configs;
    }

    private void loadConfiguration() {
        try {
            File routeConfigurationFile = new FileSystemResource(eventsConfigurationFileLocation).getFile();
            this.configs = objectMapper.readValue(routeConfigurationFile, new TypeReference<List<ConfigDetail>>() {});
        } catch (IOException exception) {
            log.error("Failed to load configuration for file : " + eventsConfigurationFileLocation, exception);
        }
    }
}
