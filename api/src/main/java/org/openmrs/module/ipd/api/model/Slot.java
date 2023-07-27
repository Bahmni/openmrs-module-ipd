package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Order;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_slot")
public class Slot extends BaseChangeableOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	public enum SlotStatus {
		SCHEDULED,
		MISSED,
		COMPLETED,
		CANCELLED
	}
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "slot_id")
	private Integer id;
	
	/**
	 * The location Where schedule occurs
	 */
	@ManyToOne(optional = false)
	@JoinColumn(referencedColumnName = "location_id")
	private Location location;
	
	/**
	 * The Service Category of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "service_category_id", referencedColumnName = "concept_id")
	private Concept serviceCategoryId;
	
	/**
	 * The Service Type of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "service_type_id", referencedColumnName = "concept_id")
	private Concept serviceTypeId;
	
	/**
	 * The Speciality of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "speciality_id", referencedColumnName = "concept_id")
	private Concept specialityId;
	
	/**
	 * The Appointment Type of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "appointment_type_id", referencedColumnName = "concept_id")
	private Concept appointmentTypeId;
	
	/**
	 * The entity that belongs to a Schedule
	 */
	@ManyToOne
	@JoinColumn(name = "schedule_reference_id", referencedColumnName = "schedule_id")
	private Schedule schedule;
	
	/**
	 * The Start Date the Slot
	 */
	@Column(name = "start_date", nullable = false)
	private Date startDate;
	
	/**
	 * The End Date the Slot
	 */
	@Column(name = "end_date", nullable = false)
	private Date endDate;
	
	/**
	 * Any Comment for the Slot
	 */
	@Column(name = "comment")
	private String comment;
	
	/**
	 * The current status of the slot.
	 */
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SlotStatus status;
	
	/**
	 * Order of the Slot
	 */
	@OneToOne
	@JoinColumn(name = "order_id", referencedColumnName = "order_id")
	private Order orderId;
}


