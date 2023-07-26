package org.bahmni.module.ipd.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseOpenmrsMetadata;

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
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "reference_id")
	private Integer id;
	
	@Column(name = "target_type")
	private String type;
	
	@Column(name = "target_uuid")
	private String targetUuid;
	
	@Column(name = "reference")
	private String reference;
}
