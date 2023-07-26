package org.bahmni.module.ipd.model;

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
	
	/**
	 * The entity who benefits from the performance of the service specified in the task (e.g., the
	 * patient).
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "for_reference_id", referencedColumnName = "reference_id")
	private Reference forReference;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "by_reference_id", referencedColumnName = "reference_id")
	private Reference byReference;
	
	/**
	 * Whether this schedule is in active use
	 */
	@Column(name = "active", nullable = false)
	private boolean active = Boolean.TRUE;
	
	/**
	 * The Service Type of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "service_type_id", referencedColumnName = "concept_id")
	private Concept serviceType;
	
	/**
	 * The Start Date the Schedule
	 */
	@Column(name = "start_date", nullable = false)
	private Date startDate;
	
	/**
	 * The End Date the Schedule
	 */
	@Column(name = "end_date", nullable = false)
	private Date endDate;
	
	/**
	 * Any Comment for the Schedule
	 */
	@Column(name = "comments")
	private String comments;
	
}
