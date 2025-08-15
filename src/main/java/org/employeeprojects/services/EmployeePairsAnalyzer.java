package org.employeeprojects.services;

import lombok.extern.slf4j.Slf4j;
import org.employeeprojects.dto.EmployeeProjects;
import org.employeeprojects.dto.Pair;
import org.employeeprojects.dto.response.MaxPairResult;
import org.employeeprojects.projecttimes.PeriodCalculations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeePairsAnalyzer {

    @Autowired
    private ProjectsFileParser parser;

    public MaxPairResult analyzePairs(MultipartFile file, String dateFormat) {
        LocalDate dateForNull = LocalDate.now();

        Map<String, EmployeeProjects> employeesFromFile = parser.processFile(file, dateForNull, dateFormat);

        Map<Pair, Map<String, Long>> pairsToProjects = this.findCommonProjects(employeesFromFile);

        Map.Entry<Pair, Map<String, Long>> maxEntry =
                pairsToProjects.entrySet().stream()
                        .max(Comparator.comparingLong(e ->
                                e.getValue().values().stream().mapToLong(Long::longValue).sum()
                        ))
                        .orElse(null);

        if (maxEntry != null) {
            Pair maxPair = maxEntry.getKey();
            long totalDays = maxEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
            return getResult(maxPair, totalDays, pairsToProjects.get(maxPair));

        } else {
            return getResult("No maximal pair found");
        }
    }

    private MaxPairResult getResult(Pair pair, Long allTme, Map<String, Long> allCommonProjects) {
        String resutlMsg = "Maximal pair is " + pair.getFirst() + ", " + pair.getSecond() + " with total time on common projects: " + allTme;
        return new MaxPairResult(resutlMsg, pair.getFirst(), pair.getSecond(), allTme, allCommonProjects);
    }

    private MaxPairResult getResult(String message) {
        return new MaxPairResult(message, null, null, null, null);
    }

    private Map<Pair, Map<String, Long>> findCommonProjects(Map<String, EmployeeProjects> employeesFromFile) {
        Map<Pair, Map<String, Long>> pairsToProjects = new HashMap<>();

        // Sort keys once to ensure consistent pair ordering and avoid (a,b)/(b,a) duplication.
        // The sort here has complexity O(n log n), but the following algorithm is quadratic O(n²), thus the sort does not increase the total complexity.
        List<String> employeesIds = employeesFromFile.keySet().stream().sorted().toList();
        for (int i = 0; i < employeesIds.size() - 1; i++) {
            for (int j = i + 1; j < employeesIds.size(); j++) {

                Pair pair = Pair.builder()
                        .first(employeesIds.get(i))
                        .second(employeesIds.get(j))
                        .build();

                EmployeeProjects emp1Projects = employeesFromFile.get(pair.getFirst());
                EmployeeProjects emp2Projects = employeesFromFile.get(pair.getSecond());

                Map<String, Long> commonProjects =
                        emp1Projects.getProjectIds().stream()
                                .filter(emp2Projects.getProjectIds()::contains)
                                .collect(Collectors.toMap(
                                        projectId -> projectId,
                                        projectId -> {
                                            var periods1 = emp1Projects.getProjectPeriods(projectId);
                                            var periods2 = emp2Projects.getProjectPeriods(projectId);
                                            return PeriodCalculations.getCommonDaysFromPeriods(periods1, periods2);
                                        }
                                ));
                pairsToProjects.put(pair, commonProjects);
            }
        }
        return pairsToProjects;

    }

}
