package com.dndn.promotions.controller;

import com.dndn.promotions.model.UserDrawResultVO;
import com.dndn.promotions.model.UserVO;
import com.dndn.promotions.service.PromotionService;
import com.dndn.promotions.util.AesUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
    private final AesUtils aesUtils;

    @Operation(summary = "사용자(핸드폰번호) 등록", description = "사용자(핸드폰번호) db에 등록하고, 등록된 값 리턴 - requestBody에 contact만 넣어서 요청주세요!! 저거 필요한거만 빼는거 어케하는지 진짜 모르겠어ㅠㅠ 아마도 안되는거같아 모두 같은 클래스써서..")
    @PostMapping(value = "/user")
    public ResponseEntity<UserVO> insertNewUser(HttpServletRequest request, @RequestBody UserVO userVO) throws Exception {
        //front에서 암호화한 값 가져와서 그냥 그대로 그값 저장하면 됨
        promotionService.insertUser(userVO);
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "핸드폰번호 DB에 존재여부 확인", description = "해당 핸드폰번호가 db에 등록돠있는지 확인. 없으면 그냥 쌩 null 리턴함..")
    @GetMapping(value = "/user/{contact}")
    public ResponseEntity<UserVO> getUserByContact(HttpServletRequest request, @PathVariable String contact) throws Exception {
        UserVO user = UserVO.builder().contact(contact).build();
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

    @Operation(summary = "응모결과 확인", description = "해당 사용자가 응모한 결과 받아오기 (응모결과 있으면 재응모X)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "amount: 응모결과금액", content = @Content(schema = @Schema(implementation = UserDrawResultVO.class)))
    })
    @GetMapping(value = {"/draw-history/{userId}"})
    public ResponseEntity<UserDrawResultVO> getDrawHistoryForUser(@PathVariable Integer userId) {
        UserDrawResultVO drawResultForUser = promotionService.getDrawResultForUser(userId);
        return ResponseEntity.ok(drawResultForUser);
    }

    @Operation(summary = "응모하기", description = "사용자별 응모진행하고 응모결과 받아오기 - requestBody에 id 값만 넣어서 요청해주세요!!!!! 저거 필요한거만 빼는거 어케하는지 진짜 모르겠어ㅠㅠ 아마도 안되는거같아 모두 같은 클래스써서..")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "amount: 응모결과금액", content = @Content(schema = @Schema(implementation = UserDrawResultVO.class)))
    })
    @PostMapping(value = {"/draw"})
    public ResponseEntity<UserDrawResultVO> setDrawResult(@RequestBody UserVO userVO) {
        UserDrawResultVO drawResultForUser = promotionService.getDrawResultForUser(userVO.getId());
        if(drawResultForUser != null) {
            return null;
        }


        return null;
    }
}
