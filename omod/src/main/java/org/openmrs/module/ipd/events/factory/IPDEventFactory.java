package org.openmrs.module.ipd.events.factory;

import org.openmrs.module.ipd.events.model.IPDEventType;
import org.openmrs.module.ipd.events.handler.IPDEventHandler;

public interface IPDEventFactory {
    IPDEventHandler createEventHandler(IPDEventType eventType);
}