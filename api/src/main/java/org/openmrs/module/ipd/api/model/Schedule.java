package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Order;

import javax.persistence.*;
import java.time.LocalDate;

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
	 * Should we rename it to "subject"
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "for_reference_id", referencedColumnName = "reference_id")
	private Reference forReference;  // actor in fhir reference

	/**
	 * Should we rename it to "author"
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "by_reference_id", referencedColumnName = "reference_id")
	private Reference byReference; // actor in fhir reference

	@Column(name = "active", nullable = false)
	private boolean active = Boolean.TRUE;

	@OneToOne
	@JoinColumn(name = "service_category_id", referencedColumnName = "concept_id")
	private Concept serviceCategory; // null not in use

	@OneToOne
	@JoinColumn(name = "service_type_id", referencedColumnName = "concept_id")
	private Concept serviceType;

	@OneToOne
	@JoinColumn(name = "speciality_id", referencedColumnName = "concept_id")
	private Concept speciality; // null not in use

	@OneToOne
	@JoinColumn(name = "order_id", referencedColumnName = "order_id")
	private Order order;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "comments")
	private String comments;

}
