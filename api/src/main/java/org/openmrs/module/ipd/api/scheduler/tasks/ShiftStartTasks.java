package org.openmrs.module.ipd.api.scheduler.tasks;

import org.openmrs.module.ipd.api.events.IPDEventManager;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ShiftStartTasks extends AbstractTask implements ApplicationContextAware {

    private ApplicationContext context;
//    IPDEventManager eventManager;
    @Override
    public void execute() {
        IPDEventManager eventManager = context.getBean(IPDEventManager.class);
        System.out.println("Started Shift Schedule");
        System.out.println("Shift Start Task"+IPDEventType.SHIFT_START_TASK);
        IPDEventType eventType = eventManager.getEventTypeForEncounter("SHIFT_START_TASK");
        System.out.println("eventType"+ eventType);
        if (eventType != null) {
            IPDEvent ipdEvent = new IPDEvent(null, null, eventType);
            eventManager.processEvent(ipdEvent);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
}
