package org.openmrs.module.ipd.api.dao;

import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.api.db.DAOException;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotDAO {
	
	Slot getSlot(Integer slotId) throws DAOException;
	
	Slot saveSlot(Slot slot) throws DAOException;

	void purgeSlot(Slot slot) throws DAOException;
}