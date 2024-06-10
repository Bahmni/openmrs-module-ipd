package org.openmrs.module.ipd.api.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.events.model.ConfigDetail;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ConfigLoader {
    private List<ConfigDetail> configs = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String CONFIG_FILE_NAME = "eventsConfig.json";

    public List<ConfigDetail> getConfigs() {
        if (configs.isEmpty()) {
            loadConfiguration();
        }
        return this.configs;
    }

    private void loadConfiguration() {
        String routeConfigurationFileLocation = Context.getAdministrationService().getGlobalProperty("ipd.eventsConfig.filepath");
        try {
            if (StringUtils.isBlank(routeConfigurationFileLocation)) {
                File file = new File(this.getClass().getClassLoader().getResource(CONFIG_FILE_NAME).toURI());
                routeConfigurationFileLocation = file.getAbsolutePath();
            }

            File routeConfigurationFile = new FileSystemResource(routeConfigurationFileLocation).getFile();
            this.configs = objectMapper.readValue(routeConfigurationFile, new TypeReference<List<ConfigDetail>>() {});
        } catch (IOException exception) {
            log.error("Failed to load configuration for file : " + routeConfigurationFileLocation, exception);
        }
        catch (URISyntaxException exception) {
            log.error("Failed to find file : " + CONFIG_FILE_NAME, exception);
        }
    }
}
