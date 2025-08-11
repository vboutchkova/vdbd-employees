package org.employeeprojects;

import org.employeeprojects.services.EmployeePairsAnalyzer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@AutoConfigureMockMvc
@SpringBootTest
class FileUploadIntegrationTest {

    @Autowired
    private MockMvc mvc;

    //@MockBean
    @Autowired
    private EmployeePairsAnalyzer service;

    @Test
    public void shouldUploadAndFindMaxPair() throws Exception {
        String csvContent = "143, 12, 2013-11-01, 2014-01-05 \n" +
                "218, 10, 2012-05-16, NULL \n" +
                "143, 10, 2009-01-01, 2011-04-27\n" +
                "219,10, 2012-08-10,NULL\n" +
                "217,12,2012-12-31,2014-01-02 \n" +
                "217,10, 2021-05-14,NULL";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        this.mvc.perform(multipart("/api/employee-pairs/analyze").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maximal pair is 218, 219 with total time on common projects: ")));;
    }

    @Test
    public void shouldUploadAndNoMaxPairFound() throws Exception {
        String csvContent = "143, 12, 2013-11-01, 2014-01-05";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        this.mvc.perform(multipart("/api/employee-pairs/analyze").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No maximal pair found")));;
    }

    @Test
    public void shouldUploadAndFindMaxPairAndSkipCorrectHeader() throws Exception {
        String csvContent = "EmpID, ProjectID, DateFrom, DateTo\n" +
                "143, 12, 2013-11-01, 2014-01-05\n" +
                "333, 12, 2013-11-01, 2014-01-05";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        this.mvc.perform(multipart("/api/employee-pairs/analyze").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maximal pair is 143, 333")));;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should400WhenMissingFile() throws Exception {
        byte[] emptyContent = new byte[0];
        MultipartFile multipartFile = new MockMultipartFile(
                "file", // parameter name
                "empty.txt", // original filename
                "text/plain", // content type
                emptyContent // content
        );

        this.mvc.perform(multipart("/api/employee-pairs/analyze")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldUploadAndFindMaxPairWithDiffFormat() throws Exception {
        String csvContent = "143, 10, 01.01.2021, 15.03.2021\n" +
                "218, 10, 10.02.2021, 20.04.2021\n" +
                "143, 12, 01.06.2021, 15.08.2021\n" +
                "218, 12, 01.07.2021, 31.07.2021\n" +
                "501, 10, 01.01.2021, 31.01.2021\n" +
                "602, 13, 01.03.2021, NULL\n" +
                "143, 13, 01.03.2021, 15.04.2021\n" +
                "218, 14, 01.01.2022, 28.02.2022\n" +
                "501, 14, 15.01.2022, 20.01.2022";

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        this.mvc.perform(multipart("/api/employee-pairs/analyze")
                        .file(multipartFile)
                        .param("dateFormat", "dd.MM.yyyy"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maximal pair is")));;
    }

    @Test
    public void shouldUploadAndFindMaxMultipleProjectPeriods() throws Exception {
        String csvContent = "143, 12, 2013-12-01, 2025-05-01\n" +
                "217,12,2012-12-31,2014-01-02\n" +
                "217,12, 2025-05-14,2025-08-11\n" +
                "143, 12, 2025-08-10,2025-08-11\n" +
                "143, 88, 2013-10-01, 2013-11-01\n" +
                "217,88,2013-10-01, 2013-11-01";

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.csv",
                "text/csv", csvContent.getBytes());

        this.mvc.perform(multipart("/api/employee-pairs/analyze")
                        .file(multipartFile)
                        .param("dateFormat", "yyyy-MM-dd"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maximal pair is 143, 217")))
                .andExpect(jsonPath("$.time").value(67))
                .andExpect(jsonPath("$.allCommonProjects['88']").value(32))
                .andExpect(jsonPath("$.allCommonProjects['12']").value(35));
    }

}
