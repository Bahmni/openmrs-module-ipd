package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_schedule")
public class Schedule extends BaseChangeableOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "schedule_id")
	private Integer id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "for_reference_id", referencedColumnName = "reference_id")
	private Reference forReference;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "by_reference_id", referencedColumnName = "reference_id")
	private Reference byReference;

	@Column(name = "active", nullable = false)
	private boolean active = Boolean.TRUE;

	@OneToOne
	@JoinColumn(name = "service_type_id", referencedColumnName = "concept_id")
	private Concept serviceType;

	@Column(name = "start_date", nullable = false)
	private Date startDate;

	@Column(name = "end_date", nullable = false)
	private Date endDate;

	@Column(name = "comments")
	private String comments;
	
}
