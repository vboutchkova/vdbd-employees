package org.employeeprojects.projecttimes;

import org.employeeprojects.dto.EmployeeProjectPeriods;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PeriodCalculations {

    public static Long getCommonDaysFromPeriods(EmployeeProjectPeriods epp1, EmployeeProjectPeriods epp2) {

        return epp1.getPeriods().stream()
                .flatMapToLong(p1 -> epp2.getPeriods().stream()
                        .mapToLong(p2 -> getCommonDays(p1, p2)))
                .sum();
    }

    static long getCommonDays(Period p1, Period p2) {
        if (p1 == null || p2 == null) return 0;

        LocalDate overlapStart = p1.getStart().isAfter(p2.getStart()) ? p1.getStart() : p2.getStart();
        LocalDate overlapEnd = p1.getEnd().isBefore(p2.getEnd()) ? p1.getEnd() : p2.getEnd();

        if (overlapStart.isAfter(overlapEnd)) return 0;

        return ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }
}
