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
