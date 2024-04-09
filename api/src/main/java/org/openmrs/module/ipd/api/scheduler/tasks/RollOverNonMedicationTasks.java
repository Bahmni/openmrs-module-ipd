package org.openmrs.module.ipd.api.scheduler.tasks;

import org.openmrs.module.ipd.api.events.IPDEventManager;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RollOverNonMedicationTasks extends AbstractTask {

    private ApplicationContext context;

    @Override
    public void execute() {
        IPDEventManager eventManager = context.getBean(IPDEventManager.class);
        IPDEventType eventType = eventManager.getEventTypeForEncounter(String.valueOf(IPDEventType.ROLLOVER_TASK));
        if (eventType != null) {
            IPDEvent ipdEvent = new IPDEvent(null, null, eventType);
            eventManager.processEvent(ipdEvent);
        }
    }
}
