package org.employeeprojects.dto.response;

import java.util.Map;

public record MaxPairResult(String maxPairResultMessage, String employee1, String employee2,
                            Long time, Map<String, Long> allCommonProjects) {
}
