package org.employeeprojects.services;

import lombok.extern.slf4j.Slf4j;
import org.employeeprojects.dto.EmployeeProjectPeriods;
import org.employeeprojects.dto.EmployeeProjects;
import org.employeeprojects.dto.EmployeeToProjectParsedLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ProjectsFileParser {

    @Value("${my.property.name:yyyy-MM-dd}")
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String UTC = "UTC";


    public Map<String, EmployeeProjects> processFile(MultipartFile file, LocalDate dateForNull, String dateFormat) {
        Map<String, EmployeeProjects> employeesToProjects = new HashMap<>();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("The input file should not be empty.");
        }
        DateTimeFormatter formatter = getFormatter(dateFormat);
        int idx = -1;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;
            boolean checkHeaderLine = true;
            while ((line = bufferedReader.readLine()) != null) {

                if (checkHeaderLine && isHeaderRow(line)) {
                    checkHeaderLine = false;
                    continue;
                }

                EmployeeToProjectParsedLine parsedEmployeeLine = parseCsvLine(line, dateForNull, formatter, idx+1);

                String employeeId = parsedEmployeeLine.employeeId();
                EmployeeProjects employeeProjects = employeesToProjects.get(employeeId);

                if (employeeProjects == null) {
                    employeesToProjects.put(employeeId, EmployeeProjectPeriods.mapToEmployeeProjects(parsedEmployeeLine));
                } else {
                    String currentLineProjectId = parsedEmployeeLine.projectId();
                    EmployeeProjectPeriods projectPeriods = employeeProjects.getProjectPeriods(currentLineProjectId);
                    if (projectPeriods == null) {
                        projectPeriods = EmployeeProjectPeriods.getProjectPeriods(parsedEmployeeLine);
                        employeeProjects.setProjectPeriods(currentLineProjectId, projectPeriods);
                    } else {
                        projectPeriods.addNewPeriod(parsedEmployeeLine.from(), parsedEmployeeLine.to());
                    }
                }
                idx++;
            }

            return employeesToProjects;

        } catch (IOException e) {
            log.error("Failed to read the input file: {} and date for null {}, message {}", file.getOriginalFilename(), dateForNull, e.getMessage());
            throw new IllegalArgumentException("Failed to read the input file: " + file.getOriginalFilename() + ", " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error occurred while reading from file: {} and date for null {}, message {}", file.getOriginalFilename(), dateForNull, e.getMessage(), e);
            throw new IllegalArgumentException("Unexpected error occurred while reading from file: " + file.getOriginalFilename() + ", " + e.getMessage(), e);
        }
    }

    private boolean isHeaderRow(String line) {
        String[] parts = Arrays.stream(line.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        return parts[0].contains("EmpID") && parts[1].contains("ProjectID") && parts[2].contains("DateFrom") && parts[3].contains("DateTo");
    }

    private DateTimeFormatter getFormatter(String dateFormat) {
        if (dateFormat == null || dateFormat.isEmpty()) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of(UTC));
    }

    private EmployeeToProjectParsedLine parseCsvLine(String csvDataLine, LocalDate dateForNull, DateTimeFormatter formatter, int idx) {
        String[] parts = null;
        LocalDate dateFrom = null;
        LocalDate dateTo = null;
        try {
            parts = Arrays.stream(csvDataLine.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);

            dateFrom = LocalDate.parse(parts[2], formatter);
            dateTo = parts[3].equalsIgnoreCase("NULL")
                    ? dateForNull
                    : LocalDate.parse(parts[3], formatter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse data row [" + idx + "]: " + csvDataLine);
        }
        return new EmployeeToProjectParsedLine(parts[0], parts[1], dateFrom, dateTo);
    }
}
