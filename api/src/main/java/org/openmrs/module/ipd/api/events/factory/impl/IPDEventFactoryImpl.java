package org.openmrs.module.ipd.api.events.factory.impl;

import org.openmrs.module.ipd.api.events.handler.impl.ShiftStartTaskEventHandler;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.api.events.factory.IPDEventFactory;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.handler.impl.PatientAdmitEventHandler;
import org.openmrs.module.ipd.api.events.handler.impl.RolloverTaskEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IPDEventFactoryImpl implements IPDEventFactory {

    @Autowired
    PatientAdmitEventHandler patientAdmitEventHandler;

    @Autowired
    ShiftStartTaskEventHandler shiftStartTaskEventHandler;

    @Autowired
    RolloverTaskEventHandler rolloverTaskEventHandler;

    @Override
    public IPDEventHandler createEventHandler(IPDEventType eventType) {
        switch (eventType) {
            case PATIENT_ADMIT:
                return patientAdmitEventHandler;
            case SHIFT_START_TASK:
                return shiftStartTaskEventHandler;
            case ROLLOVER_TASK:
                return rolloverTaskEventHandler;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }

}
