package org.employeeprojects.projecttimes;

import org.employeeprojects.dto.EmployeeProjectPeriods;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeriodCalculationTest {

    @Test
    void testNoOverlap() {
        Period p1 = new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        Period p2 = new Period(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 10));

        long days = PeriodCalculations.getCommonDays(p1, p2);
        assertEquals(0, days);
    }

    @Test
    void testFullOverlap() {
        Period p1 = new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        Period p2 = new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));

        long days = PeriodCalculations.getCommonDays(p1, p2);
        assertEquals(10, days);
    }

    @Test
    void testPartialOverlap() {
        Period p1 = new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));
        Period p2 = new Period(LocalDate.of(2020, 1, 5), LocalDate.of(2020, 1, 15));

        long days = PeriodCalculations.getCommonDays(p1, p2);
        assertEquals(6, days);
    }

    @Test
    void testReversedOrder() {
        Period p1 = new Period(LocalDate.of(2020, 1, 5), LocalDate.of(2020, 1, 15));
        Period p2 = new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10));

        long days = PeriodCalculations.getCommonDays(p1, p2);
        assertEquals(6, days);
    }

    @Test
    void testMultiplePeriods() {
        EmployeeProjectPeriods e1 = EmployeeProjectPeriods.builder()
                .employeeId("E1")
                .projectId("P1")
                .periods(List.of(
                        new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 10)),
                        new Period(LocalDate.of(2020, 2, 1), LocalDate.of(2020, 2, 5))
                ))
                .build();

        EmployeeProjectPeriods e2 = EmployeeProjectPeriods.builder()
                .employeeId("E2")
                .projectId("P1")
                .periods(List.of(
                        new Period(LocalDate.of(2020, 1, 5), LocalDate.of(2020, 1, 15)),
                        new Period(LocalDate.of(2020, 2, 3), LocalDate.of(2020, 2, 4))
                ))
                .build();

        long totalDays = PeriodCalculations.getCommonDaysFromPeriods(e1, e2);
        assertEquals(8, totalDays);
    }
}
