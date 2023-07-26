package org.bahmni.module.ipd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class IPDActivator extends BaseModuleActivator {
	
	private final Log log = LogFactory.getLog(this.getClass());
	
	public void startup() {
		log.info("Starting Basic Module");
	}
	
	public void shutdown() {
		log.info("Shutting down Basic Module");
	}
	
}
