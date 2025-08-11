package org.employeeprojects.dto;

import lombok.Builder;
import lombok.Data;
import org.employeeprojects.projecttimes.Period;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
public class EmployeeProjectPeriods {

    String employeeId;
    String projectId;
    List<Period> periods;

    public void addNewPeriod(LocalDate start, LocalDate end) {
        periods.add(new Period(start, end));
    }

    public static EmployeeProjects mapToEmployeeProjects(EmployeeToProjectParsedLine parsedEmployeeLine) {
        String key = parsedEmployeeLine.projectId();
        EmployeeProjectPeriods value = getProjectPeriods(parsedEmployeeLine);

        Map<String, EmployeeProjectPeriods> projects = new HashMap<>();
        projects.put(key, value);

        return EmployeeProjects.builder()
                .employeeId(parsedEmployeeLine.employeeId())
                .projects(projects).build();
    }


    public static EmployeeProjectPeriods getProjectPeriods(EmployeeToProjectParsedLine parsedEmployeeLine) {
        List<Period> projectPeriods = new ArrayList<>();
        projectPeriods.add(new Period(parsedEmployeeLine.from(), parsedEmployeeLine.to()));

        return EmployeeProjectPeriods.builder()
                .employeeId(parsedEmployeeLine.employeeId())
                .projectId(parsedEmployeeLine.projectId())
                .periods(projectPeriods)
                .build();
    }
}
