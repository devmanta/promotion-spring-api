package com.dndn.promotions.controller;

import com.dndn.promotions.model.UserDrawResultEntity;
import com.dndn.promotions.model.UserEntity;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.service.PromotionService;
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
    private final PromotionRepository promotionRepository;

    @Operation(summary = "사용자(핸드폰번호) 등록", description = "사용자(핸드폰번호) db에 등록하고, 등록된 값 리턴 - <strong>requestBody에 contact만 넣어서 요청주세요!!</strong>저거 필요한거만 빼는거 어케하는지 진짜 모르겠어ㅠㅠ 아마도 안되는거같아 모두 같은 클래스써서..")
    @PostMapping(value = "/user")
    public ResponseEntity<UserEntity> insertNewUser(HttpServletRequest request, @RequestBody UserEntity userEntity) throws Exception {
        //front에서 암호화한 값 가져와서 그냥 그대로 그값 저장하면 됨
        promotionService.insertUser(userEntity);
        return ResponseEntity.ok(userEntity);
    }

    @Operation(summary = "핸드폰번호 DB에 존재여부 확인", description = "해당 핸드폰번호가 db에 등록돠있는지 확인. 없으면 그냥 쌩 null 리턴함..")
    @GetMapping(value = "/user/{contact}")
    public ResponseEntity<UserEntity> getUserByContact(HttpServletRequest request, @PathVariable String contact) throws Exception {
        UserEntity user = UserEntity.builder().contact(contact).build();
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

            List<UserDrawResultEntity> list = promotionService.getDrawResult();
            for(UserDrawResultEntity r : list) {
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
        @ApiResponse(responseCode = "200", description = "amount: 응모결과금액 / 응모결과 없으면 쌩 null return", content = @Content(schema = @Schema(implementation = UserDrawResultEntity.class)))
    })
    @GetMapping(value = {"/draw-history/{userId}"})
    public ResponseEntity<UserDrawResultEntity> getDrawHistoryForUser(@PathVariable Integer userId) {
        UserDrawResultEntity drawResultForUser = promotionService.getDrawResultForUser(userId);
        return ResponseEntity.ok(drawResultForUser);
    }

    @Operation(summary = "응모하기", description = "사용자별 응모진행하고 응모결과 받아오기 - <strong>requestBody에 id 값만 넣어서 요청해주세요!!!!!</strong> 저거 필요한거만 빼는거 어케하는지 진짜 모르겠어ㅠㅠ 아마도 안되는거같아 모두 같은 클래스써서.."
        + "<br /><br />여기서 하는일: 요청한 id(userId) 기준으로<br />"
        + "1. user 당첨결과 있으면 return null(당첨결과 있으면 이 api호출 안하겠지만 그냥 방어용)<br />"
        + "2. 당첨로직 통해서 user 당첨결과 선정 및 return")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "amount: 응모결과금액", content = @Content(schema = @Schema(implementation = UserDrawResultEntity.class)))
    })
    @PostMapping(value = {"/draw"})
    public ResponseEntity<UserDrawResultEntity> setDrawResult(@RequestBody UserEntity userEntity) {
        UserDrawResultEntity drawResultForUser = promotionService.getDrawResultForUser(userEntity.getId());
        if(drawResultForUser != null) { // 이건 그냥 방어코드 응모했던사람은 front에서 이 api 호출 안할거지만.. 어케 호출 한다는 가정하에
            return null;
        }

//        당첨 로직 :
//        전체 인원 중 당첨 확률로 계산해 당첨
//        660,000원 (10/1,060, 약 0.9%)
//        66,000원 (50/1,060, 약 4.7%)
//        6,600원 (1,000/1,060, 약 94.3%)
//
//        500명 응오한것 중에 495명이 3등, 4명이 2등 1명이 1등
//        660,000원 (9/560, 약 1.6%)
//        66,000 (46/560, 약 8.2%)
//        6,600 (505/560, 약 90.2%)


        return null;
    }

    @Operation(summary = "카카오톡 공유하기 콜백 url", description = "카카오톡에서 공유 성공하면 호출할거..  공유하기 할때 serverCallbackArgs: {id: 사용자 id}, 만 넣어서 호출해주세요 !!!"
        + "<br /><br />여기서 하는일: 카카오에서 보내준 userId 기준으로<br />"
        + "1. db에서 user 존재여부 확인<br />"
        + "2. user 당첨결과 삭제<br />"
        + "3. user가 당첨되었던 당첨금액의 총 당첨자 수 -1")
    @PostMapping(value = {"/kakao-share"})
    public void kakaoShareCallBack(@RequestBody UserEntity userFromRequestBody) {
        log.info("kakaoShareCallBack CALL START, userVO={}", userFromRequestBody);
        boolean isSucceed = promotionService.removeDrawResultAsUserSharedByKakaoTalk(userFromRequestBody);
        if(!isSucceed) {
            log.info("kakaoShareCallBack userFromDb is null OR userDrawCnt > 3, userVO={}", userFromRequestBody);
        }
    }

}
