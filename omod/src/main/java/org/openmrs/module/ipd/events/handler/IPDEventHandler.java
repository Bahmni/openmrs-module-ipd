package org.openmrs.module.ipd.events.handler;

import org.openmrs.module.ipd.events.model.IPDEvent;

public interface IPDEventHandler {
    void handleEvent(IPDEvent event);
}