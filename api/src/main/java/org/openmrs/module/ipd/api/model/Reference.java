/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.openmrs.module.ipd.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.OpenmrsObject;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "ipd_reference")
public class Reference extends BaseOpenmrsMetadata {

	private static final long serialVersionUID = 1L;

	public Reference() {
		setName("");
	}

	public Reference(String type, String targetUuid) {
		this.type = type;
		this.targetUuid = targetUuid;
		this.setName(type + "/" + targetUuid);
	}
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "reference_id")
	private Integer id;
	
	@Column(name = "target_type", nullable = false)
	private String type;
	
	@Column(name = "target_uuid", nullable = false)
	private String targetUuid;
}
