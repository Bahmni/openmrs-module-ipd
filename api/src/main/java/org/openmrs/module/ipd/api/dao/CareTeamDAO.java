package org.openmrs.module.ipd.api.dao;

import org.openmrs.Visit;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.Schedule;

public interface CareTeamDAO {

    CareTeam saveCareTeam(CareTeam careTeam) throws DAOException;

    CareTeam getCareTeamByVisit(Visit visit) throws DAOException;

}
