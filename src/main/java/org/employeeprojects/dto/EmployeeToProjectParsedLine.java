package org.employeeprojects.dto;

import java.time.LocalDate;

public record EmployeeToProjectParsedLine(
        String employeeId,
        String projectId,
        LocalDate from,
        LocalDate to) {
}