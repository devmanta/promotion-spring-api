package com.dndn.promotions.controller;

import com.dndn.promotions.model.TestEntity;
import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.service.PromotionService;
import com.dndn.promotions.util.CryptoGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping(value = {"/generator"})
    public ResponseEntity<Map<String, String>> cryptoGenerator(HttpServletRequest request) {
        HttpSession session = request.getSession();

        Map<String, String> publicKeyMap = CryptoGenerator.generatePairKey(session);
        return ResponseEntity.ok(publicKeyMap);
    }

    @PostMapping(value = "/user")
    public ResponseEntity<UserVO> insertNewUser(HttpServletRequest request, @RequestBody UserVO userVO) throws Exception {
        String decryptedContact = CryptoGenerator.decryptRSA(request.getSession(), userVO.getContact());
        userVO.setContact(decryptedContact);

        promotionService.insertUser(userVO);
        return ResponseEntity.ok(userVO);
    }

    @GetMapping(value = "/user/{contact}")
    public ResponseEntity<UserVO> getUserByContact(HttpServletRequest request, @PathVariable String contact) throws Exception {
        String decryptedContact = CryptoGenerator.decryptRSA(request.getSession(), contact);

        UserVO user = UserVO.builder().contact(decryptedContact).build();
        return ResponseEntity.ok(promotionService.getUser(user));
    }

    @GetMapping(value = "/excel")
    public ResponseEntity<InputStreamResource> downloadExcelForDrawResult() {
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("당첨결과");
            int rowNo = 0;

            Row headerRow = sheet.createRow(rowNo++);
            headerRow.createCell(0).setCellValue("번호");
            headerRow.createCell(1).setCellValue("연락처");
            headerRow.createCell(2).setCellValue("당첨금액");

            List<UserDrawResultVO> list = promotionService.getDrawResult();
            for(UserDrawResultVO r : list) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getContact());
                row.createCell(2).setCellValue(r.getAmount());
            }

            File file = new File(System.getProperty("user.dir") + "/z.xlsx");
            file.createNewFile();
            OutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            try (POIFSFileSystem fs = new POIFSFileSystem()) {

                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = info.getEncryptor();
                encryptor.confirmPassword("1234");

                try (OPCPackage opc = OPCPackage.open(file, PackageAccess.READ_WRITE);
                    OutputStream os = encryptor.getDataStream(fs)) {
                    opc.save(os);
                }

                // Write out the encrypted version
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fs.writeFilesystem(fos);
                }
            }

            InputStream in = new FileInputStream(file);
            file.delete();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment;filename=test.xlsx");

            return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
        } catch(Exception e) {
            log.error("excelDownload ERROR", e);
            return null;
        }
    }

}
