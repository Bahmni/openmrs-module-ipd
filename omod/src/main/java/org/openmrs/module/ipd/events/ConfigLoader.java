package org.openmrs.module.ipd.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.ipd.events.model.ConfigDetail;
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

    @Value("${config-file.path}")
    private String routeConfigurationFileLocation;

    public List<ConfigDetail> getConfigs() {
        if (configs.isEmpty()) {
            loadConfiguration();
        }
        return this.configs;
    }

    private void loadConfiguration() {
        try {
            File routeConfigurationFile = new FileSystemResource(routeConfigurationFileLocation).getFile();
            this.configs = objectMapper.readValue(routeConfigurationFile, new TypeReference<List<ConfigDetail>>() {});
        } catch (IOException exception) {
            log.error("Failed to load configuration for file : " + routeConfigurationFileLocation, exception);
        }
    }
}
