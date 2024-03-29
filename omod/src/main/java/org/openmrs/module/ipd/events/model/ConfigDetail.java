package org.openmrs.module.ipd.events.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDetail {
    private String type;
    private List<TaskDetail> tasks;
}
