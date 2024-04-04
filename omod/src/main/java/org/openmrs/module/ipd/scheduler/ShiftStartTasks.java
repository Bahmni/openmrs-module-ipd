package org.openmrs.module.ipd.scheduler;

import org.openmrs.module.ipd.events.IPDEventManager;
import org.openmrs.module.ipd.events.model.IPDEvent;
import org.openmrs.module.ipd.events.model.IPDEventType;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;

public class ShiftStartTasks extends AbstractTask {

    @Autowired
    private IPDEventManager eventManager;

    @Override
    public void execute() {
        IPDEventType eventType = eventManager.getEventTypeForEncounter(String.valueOf(IPDEventType.SHIFT_START_TASK));
        if (eventType != null) {
            IPDEvent ipdEvent = new IPDEvent(null, null, eventType);
            eventManager.processEvent(ipdEvent);
        }
    }
}
