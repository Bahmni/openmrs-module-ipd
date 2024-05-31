package org.openmrs.module.ipd.api.events.factory;

import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;

public interface IPDEventFactory {
    IPDEventHandler createEventHandler(IPDEventType eventType);
}