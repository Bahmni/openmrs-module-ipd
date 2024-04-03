package org.openmrs.module.ipd.events.handler.impl;

import org.openmrs.module.ipd.events.ConfigLoader;
import org.openmrs.module.ipd.events.model.ConfigDetail;
import org.openmrs.module.ipd.events.model.IPDEvent;
import org.openmrs.module.ipd.events.handler.IPDEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShiftStartTaskEventHandler implements IPDEventHandler {

    @Autowired
    ConfigLoader configLoader;

    @Override
    public void handleEvent(IPDEvent event) {
        List<ConfigDetail> configList = configLoader.getConfigs();
        ConfigDetail eventConfig = configList.stream()
                .filter(config -> config.getType().equals(event.getIpdEventType().name()))
                .findFirst()
                .orElse(null);

        //create task based on configuration
    }
}