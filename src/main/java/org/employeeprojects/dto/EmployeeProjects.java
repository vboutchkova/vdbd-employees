package org.employeeprojects.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class EmployeeProjects {
    private String employeeId;
    @NonNull
    private Map<String, EmployeeProjectPeriods> projects;

    public Set<String> getProjectIds() {
        return projects.keySet();
    }

    public EmployeeProjectPeriods getProjectPeriods(String projectId) {
        return projects.get(projectId);
    }

    public EmployeeProjectPeriods setProjectPeriods(String projectId, EmployeeProjectPeriods periods) {
        return projects.put(projectId, periods);
    }
}
