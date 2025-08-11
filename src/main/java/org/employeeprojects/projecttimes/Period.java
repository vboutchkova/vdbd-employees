package org.employeeprojects.projecttimes;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Period {

    LocalDate start;
    LocalDate end;

    public Period(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Project start should be before or equal to the project end date");
        }
        this.start = start;
        this.end = end;
    }
}
