package com.dndn.promotions.controller;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.repository.TestRepository;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestRepository testRepository;

    @GetMapping(value = "/test")
    public ResponseEntity<List<TestEntity>> test() {
        List<TestEntity> testEntity = testRepository.testSelect();
        return ResponseEntity.ok(testEntity);
    }

    @GetMapping(value = "/test1")
    public ResponseEntity<TestEntity> test1() {
        return ResponseEntity.ok(TestEntity.builder().no(2).name("haha").build());
    }

    @GetMapping(value = "/excel")
    public void downloadExcel(HttpServletResponse response) {
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("게시판글들");
            int rowNo = 0;

            Row headerRow = sheet.createRow(rowNo++);
            headerRow.createCell(0).setCellValue("번호");
            headerRow.createCell(1).setCellValue("이름");
            headerRow.createCell(2).setCellValue("날짜");

            List<TestEntity> list = testRepository.testSelect();
            for (TestEntity t : list) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(t.getNo());
                row.createCell(1).setCellValue(t.getName());
                row.createCell(2).setCellValue(t.getCreateDate().toString());
            }

            response.setContentType("ms-vnd/excel");
            response.setHeader("Content-Disposition", "attachment;filename=test.xlsx");

            workbook.write(response.getOutputStream());
        } catch(Exception e) {
            log.error("excelDownload ERROR", e);
        }

    }

}
