package org.openmrs.module.ipd.api.events;

import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.factory.IPDEventFactory;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IPDEventManager {
    @Autowired
    ConfigLoader configLoader;

    @Autowired
    IPDEventFactory eventFactory;

    public IPDEventType getEventTypeForEncounter(String type) {
        switch (type) {
            case "ADMISSION":
                return IPDEventType.PATIENT_ADMIT;
            case "SHIFT_START_TASK":
                return IPDEventType.SHIFT_START_TASK;
            case "ROLLOVER_TASK":
                return IPDEventType.ROLLOVER_TASK;
            default:
                return null;
        }
    }

    public void processEvent(IPDEvent event) {
        IPDEventHandler handler = eventFactory.createEventHandler(event.getIpdEventType());
        handler.handleEvent(event);
    }

}
