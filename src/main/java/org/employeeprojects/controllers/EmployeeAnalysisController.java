package org.employeeprojects.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.employeeprojects.dto.response.MaxPairResult;
import org.employeeprojects.services.EmployeePairsAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RequestMapping(path = "/api/employee-pairs")
@RestController
@AllArgsConstructor
@Slf4j
public class EmployeeAnalysisController {

    @Autowired
    private EmployeePairsAnalyzer analyzer;

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/analyze")
    public ResponseEntity<MaxPairResult> uploadCSVFile(@RequestParam("file") MultipartFile file, @RequestParam(required = false) String dateFormat) {
        MaxPairResult result = analyzer.analyzePairs(file, dateFormat);
        log.info("RETURNING result: {}, for file {} and dateFormat {}", result, (file != null) ? file.getName() : "isnull", dateFormat);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> getTest() {
        return new ResponseEntity<>("This is SGH Longest Period Application " + new Date(), HttpStatus.OK);
    }
}
