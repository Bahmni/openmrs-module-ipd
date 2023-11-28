package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Location;

import javax.persistence.*;
import java.time.LocalDateTime;

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
	@ManyToOne
	@JoinColumn(name = "location_id", referencedColumnName = "location_id")
	private Location location; // bed location for patient
	
	/**
	 * The Service Category of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "service_category_id", referencedColumnName = "concept_id")
	private Concept serviceCategory; // null not in use
	
	/**
	 * The Service Type of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "service_type_id", referencedColumnName = "concept_id", nullable = false)
	private Concept serviceType; // as per schedule service type
	
	/**
	 * The Speciality of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "speciality_id", referencedColumnName = "concept_id")
	private Concept speciality; // null not in sue
	
	/**
	 * The Appointment Type of the Schedule
	 */
	@OneToOne
	@JoinColumn(name = "appointment_type_id", referencedColumnName = "concept_id")
	private Concept appointmentType; // null not in use
	
	/**
	 * The entity that belongs to a Schedule
	 */
	@ManyToOne
	@JoinColumn(name = "schedule_id", referencedColumnName = "schedule_id", nullable = false)
	private Schedule schedule;
	
	/**
	 * The Start Date the Slot
	 */
	@Column(name = "start_date_time", nullable = false)
	private LocalDateTime startDateTime; // slot start time
	
	/**
	 * The End Date the Slot
	 */
	@Column(name = "end_date_time")
	private LocalDateTime endDateTime; // can be null for now
	
	/**
	 * Any Comment for the Slot
	 */
	@Column(name = "comments")
	private String comments; // null not is use
	
	/**
	 * The current status of the slot.
	 */
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private SlotStatus status = SlotStatus.SCHEDULED;

	@Column(name = "overbooked", nullable = false)
	private Boolean overbooked = Boolean.FALSE; // not is use
}


