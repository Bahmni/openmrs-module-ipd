package org.openmrs.module.ipd.api;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public class IntegrationTestBeanExcluder implements BeanFactoryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(IntegrationTestBeanExcluder.class);

    private static final String[] EXCLUDED_CLASS_PREFIXES = {
            "org.openmrs.module.ipd.web.",
            "org.openmrs.module.ipd.fhir.",
            "org.openmrs.module.emrapi.",
            "org.openmrs.module.bahmniemrapi.",
            "org.openmrs.module.bahmnimapping.",
            "org.openmrs.module.metadatamapping.",
            "org.bahmni.module.bahmnicore.",
            "org.bahmni.module.bahmnicommons.",
            "org.bahmni.module.obsrelationship."
    };

    private static final String[] EXCLUDED_RESOURCE_MARKERS = {
            "emrapi-api",
            "bahmnicore-api",
            "bahmni-emr-api",
            "bahmni-mapping",
            "metadatamapping-api",
            "obs-relationship"
    };

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            return;
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        Set<String> toRemove = new HashSet<>();
        for (String beanName : beanNames) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            if (isExcluded(definition)) {
                log.debug("Excluding bean '{}' (class={}, resource={})",
                        beanName, definition.getBeanClassName(), definition.getResourceDescription());
                toRemove.add(beanName);
            }
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String beanName : beanNames) {
                if (!toRemove.contains(beanName)
                        && referencesRemoved(beanFactory.getBeanDefinition(beanName), toRemove, beanFactory)) {
                    log.debug("Excluding bean '{}' because it references a removed or missing bean", beanName);
                    toRemove.add(beanName);
                    changed = true;
                }
            }
        }

        for (String beanName : toRemove) {
            registry.removeBeanDefinition(beanName);
        }
        log.debug("IntegrationTestBeanExcluder removed {} bean definition(s)", toRemove.size());
    }

    private boolean isExcluded(BeanDefinition definition) {
        String className = definition.getBeanClassName();
        if (className != null && startsWithAny(className, EXCLUDED_CLASS_PREFIXES)) {
            return true;
        }
        String resource = definition.getResourceDescription();
        return resource != null && containsAny(resource, EXCLUDED_RESOURCE_MARKERS);
    }

    private boolean referencesRemoved(BeanDefinition definition, Set<String> removed,
                                      ConfigurableListableBeanFactory beanFactory) {
        if (definition instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abstractDefinition = (AbstractBeanDefinition) definition;
            if (abstractDefinition.getFactoryBeanName() != null
                    && isRemovedOrMissing(abstractDefinition.getFactoryBeanName(), removed, beanFactory)) {
                return true;
            }
            String[] dependsOn = abstractDefinition.getDependsOn();
            if (dependsOn != null) {
                for (String dependency : dependsOn) {
                    if (isRemovedOrMissing(dependency, removed, beanFactory)) {
                        return true;
                    }
                }
            }
        }
        for (PropertyValue propertyValue : definition.getPropertyValues().getPropertyValueList()) {
            if (valueReferencesRemoved(propertyValue.getValue(), removed, beanFactory)) {
                return true;
            }
        }
        ConstructorArgumentValues arguments = definition.getConstructorArgumentValues();
        for (ConstructorArgumentValues.ValueHolder holder : arguments.getGenericArgumentValues()) {
            if (valueReferencesRemoved(holder.getValue(), removed, beanFactory)) {
                return true;
            }
        }
        for (ConstructorArgumentValues.ValueHolder holder : arguments.getIndexedArgumentValues().values()) {
            if (valueReferencesRemoved(holder.getValue(), removed, beanFactory)) {
                return true;
            }
        }
        return false;
    }

    private boolean valueReferencesRemoved(Object value, Set<String> removed,
                                           ConfigurableListableBeanFactory beanFactory) {
        if (value == null) {
            return false;
        }
        if (value instanceof RuntimeBeanReference) {
            return isRemovedOrMissing(((RuntimeBeanReference) value).getBeanName(), removed, beanFactory);
        }
        if (value instanceof BeanDefinition) {
            return referencesRemoved((BeanDefinition) value, removed, beanFactory);
        }
        if (value instanceof Collection) {
            for (Object element : (Collection<?>) value) {
                if (valueReferencesRemoved(element, removed, beanFactory)) {
                    return true;
                }
            }
            return false;
        }
        if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (valueReferencesRemoved(entry.getKey(), removed, beanFactory)
                        || valueReferencesRemoved(entry.getValue(), removed, beanFactory)) {
                    return true;
                }
            }
            return false;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (valueReferencesRemoved(Array.get(value, i), removed, beanFactory)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRemovedOrMissing(String beanName, Set<String> removed,
                                       ConfigurableListableBeanFactory beanFactory) {
        return removed.contains(beanName) || !beanFactory.containsBean(beanName);
    }

    private boolean startsWithAny(String value, String[] prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String value, String[] markers) {
        for (String marker : markers) {
            if (value.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
