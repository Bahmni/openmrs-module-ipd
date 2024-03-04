package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Provider;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "care_team_participant")
public class CareTeamParticipant extends BaseChangeableOpenmrsData {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "care_team_participant_id")
    private Integer careTeamParticipantId;

    /**
     * FHIR:member
     * Indicates Who is involved .
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    /**
     * FHIR:function
     * @see <a href="https://hl7.org/fhir/valueset-participant-role.html">
     *     		https://hl7.org/fhir/valueset-participant-role.html
     *     	</a>
     * i.e. performer, verifier, witness
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "role")
    private Concept role;

    /**
     * FHIR:coverage.coveragePeriod.start
     * Starting time with inclusive boundary
     */
    @Column(name = "start_time")
    private Date startTime;

    /**
     * FHIR:coverage.coveragePeriod.end
     * Ending time with inclusive boundary, if not ongoing
     */
    @Column(name = "end_time")
    private Date endTime;

    @Override
    public Integer getId() {
        return getCareTeamParticipantId();
    }

    @Override
    public void setId(Integer careTeamParticipantId) {
      this.careTeamParticipantId=careTeamParticipantId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Concept getRole() {
        return role;
    }

    public void setRole(Concept role) {
        this.role = role;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            CareTeamParticipant other = (CareTeamParticipant) obj;
            return Objects.equals(this.getUuid(), other.getUuid());
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hash = Objects.hash(new Object[]{this.getUuid()});
        return hash;
    }

}
