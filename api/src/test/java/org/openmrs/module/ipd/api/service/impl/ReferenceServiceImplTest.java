package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.dao.ReferenceDAO;
import org.openmrs.module.ipd.api.model.Reference;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceServiceImplTest {

    @InjectMocks
    private ReferenceServiceImpl referenceService;

    @Mock
    private ReferenceDAO referenceDAO;

    @Test
    public void shouldInvokeSaveReferenceWithGivenReference() {
        Reference openmrsReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        Reference expectedOpenmrsReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        expectedOpenmrsReference.setId(1);

        Mockito.when(referenceDAO.saveReference(openmrsReference)).thenReturn(expectedOpenmrsReference);

        referenceService.saveReference(openmrsReference);

        Mockito.verify(referenceDAO, Mockito.times(1)).saveReference(openmrsReference);
    }

    @Test
    public void shouldInvokeGetReferenceByTargetUUIDAndTypeWithGivenTargetUUIDAndType() {
        String typeName = Patient.class.getTypeName();
        String targetUuid = "2c33920f-7aa6-0000-998a-60412d8ff7d5";
        Reference expectedOpenmrsReference = new Reference(Patient.class.getTypeName(), "2c33920f-7aa6-0000-998a-60412d8ff7d5");
        expectedOpenmrsReference.setId(1);

        Mockito.when(referenceDAO.getReferenceByTypeAndTargetUUID(typeName, targetUuid)).thenReturn(Optional.of(expectedOpenmrsReference));

        referenceService.getReferenceByTypeAndTargetUUID(typeName, targetUuid);

        Mockito.verify(referenceDAO, Mockito.times(1)).getReferenceByTypeAndTargetUUID(typeName, targetUuid);
    }
}