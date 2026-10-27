package org.openmrs.module.ipd.fhir.task;

import org.aopalliance.intercept.MethodInterceptor;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.module.fhir2.api.FhirTaskService;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Decorates fhir2's {@link FhirTaskService} so that {@code POST /Task} for a Task focusing a
 * MedicationAdministration is routed through the existing acknowledgement rules. fhir2 owns the
 * Task resource provider (only one provider per resource type is allowed), but that provider simply
 * calls {@code FhirTaskService.create(Task)}, which is the seam used here. Every other call, and every
 * Task that does not focus a MedicationAdministration, is passed to fhir2 untouched.
 * <p>
 * The handler is looked up lazily because a BeanPostProcessor must not trigger early initialisation
 * of other beans.
 */
@Component
public class AcknowledgementFhirTaskServicePostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof FhirTaskService) || isAlreadyDecorated(bean)) {
            return bean;
        }
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setInterfaces(FhirTaskService.class);
        proxyFactory.addAdvice(acknowledgementInterceptor());
        proxyFactory.addAdvice(new DecoratedMarker());
        return proxyFactory.getProxy(bean.getClass().getClassLoader());
    }

    MethodInterceptor acknowledgementInterceptor() {
        return invocation -> {
            if ("create".equals(invocation.getMethod().getName()) && invocation.getArguments().length == 1
                    && invocation.getArguments()[0] instanceof Task) {
                MedicationAdministrationAcknowledgementTaskHandler handler = applicationContext
                        .getBean(MedicationAdministrationAcknowledgementTaskHandler.class);
                Task task = (Task) invocation.getArguments()[0];
                if (handler.isAcknowledgement(task)) {
                    return handler.acknowledge(task);
                }
            }
            return invocation.proceed();
        };
    }

    private boolean isAlreadyDecorated(Object bean) {
        return bean instanceof Advised && ((Advised) bean).getAdvisors().length > 0 && hasMarker((Advised) bean);
    }

    private boolean hasMarker(Advised advised) {
        for (org.springframework.aop.Advisor advisor : advised.getAdvisors()) {
            if (advisor.getAdvice() instanceof DecoratedMarker) {
                return true;
            }
        }
        return false;
    }

    /** No-op advice used only to recognise a bean this post processor has already wrapped. */
    private static class DecoratedMarker implements MethodInterceptor {
        @Override
        public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }
}
