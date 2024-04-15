package org.openmrs.module.ipd.api.events.handler;

import org.openmrs.module.ipd.api.events.model.IPDEvent;

public interface IPDEventHandler {
    void handleEvent(IPDEvent event);
}