package org.openmrs.module.ipd.fhir.providers.r4;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.MedicationAdministration;

/**
 * Delegates to another bundle provider and sets the derived {@code category} on each
 * MedicationAdministration of the page being returned (paging stays lazy).
 */
class CategoryDecoratingBundleProvider implements IBundleProvider {

    private final IBundleProvider delegate;

    private final Consumer<MedicationAdministration> categoryApplier;

    CategoryDecoratingBundleProvider(IBundleProvider delegate, Consumer<MedicationAdministration> categoryApplier) {
        this.delegate = delegate;
        this.categoryApplier = categoryApplier;
    }

    @Override
    public IPrimitiveType<Date> getPublished() {
        return delegate.getPublished();
    }

    @Override
    public List<IBaseResource> getResources(int fromIndex, int toIndex) {
        List<IBaseResource> resources = delegate.getResources(fromIndex, toIndex);
        if (resources != null) {
            for (IBaseResource resource : resources) {
                if (resource instanceof MedicationAdministration) {
                    categoryApplier.accept((MedicationAdministration) resource);
                }
            }
        }
        return resources;
    }

    @Override
    public String getUuid() {
        return delegate.getUuid();
    }

    @Override
    public Integer preferredPageSize() {
        return delegate.preferredPageSize();
    }

    @Override
    public Integer size() {
        return delegate.size();
    }

    @Override
    public String getCurrentPageId() {
        return delegate.getCurrentPageId();
    }

    @Override
    public String getNextPageId() {
        return delegate.getNextPageId();
    }

    @Override
    public String getPreviousPageId() {
        return delegate.getPreviousPageId();
    }
}
