package org.openmrs.module.ipd.api.dao;

import org.openmrs.Concept;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.api.db.DAOException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotDAO {
	
	Slot getSlot(Integer slotId) throws DAOException;
	
	Slot saveSlot(Slot slot) throws DAOException;

    List<Slot> getSlotsByForReferenceIdAndForDateAndServiceType(Reference reference, LocalDate forDate, Concept serviceType);
}
