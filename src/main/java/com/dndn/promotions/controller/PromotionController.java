package com.dndn.promotions.controller;

import com.dndn.promotions.model.UserDrawResultEntity;
import com.dndn.promotions.model.UserEntity;
import com.dndn.promotions.repository.PromotionRepository;
import com.dndn.promotions.service.PromotionService;
import com.dndn.promotions.util.DrawUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/api")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

    private final DrawUtils drawUtils;

    @Value("${dndn.excel}")
    private String myNum;

    @Operation(summary = "응모하기!!", description = "응모하기 버튼 클릭 시 호출!!"
        + "<br /><br />requestBody: <strong>{contact : '암호화된 연락처'}</strong>   만 보내서 호출해주세요!!!<br /><br />")
    @PostMapping(value = "/draw")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "1. amount가 있고 <br />"
                                                        + "    1-1. amount !=0 이라면 당첨금액 화면 보여주기<br />"
                                                        + "    1-2. amount == 0(꽝꽝꽝) && soldOut == true 이면 당첨 소진된거!!<br />"
                                                        + "    1-3. amount == 0(꽝꽝꽝) && soldOut == false면 당첨금액이 진짜 꽝인거~ 다시 응모하기 가능!!<br />"
                                                        + "2. amount가 없고 <br />"
                                                        + "    2-1. '이미 응모하셨네요'가 있는 공유하기 모달노출<br />"
                                                        + "    2-2. amount가 없고 그리고 drawCnt > 4 면 더이상 카카오톡 공유 못하게!!  drawCnt < 4면 카톡 공유가능하게!!"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR !! 내가 뭔가를 잘못했거나.. 요청값이 제대로 맞지 않아서 에러가 났거나..")
    })
    public ResponseEntity<UserEntity> doUserDraw(HttpServletRequest request, @RequestBody UserEntity userEntity) throws Exception {
        return promotionService.doUserDraw(userEntity);
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
            headerRow.createCell(3).setCellValue("당첨날짜");

            List<UserDrawResultEntity> list = promotionService.getDrawResult();
            for(UserDrawResultEntity r : list) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getContact());
                row.createCell(2).setCellValue(r.getAmount());
                row.createCell(3).setCellValue(r.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            File file = new File(System.getProperty("user.dir") + "/z.xlsx");
            file.createNewFile();
            OutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            try(POIFSFileSystem fs = new POIFSFileSystem()) {

                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor encryptor = info.getEncryptor();
                encryptor.confirmPassword(myNum);

                try(OPCPackage opc = OPCPackage.open(file, PackageAccess.READ_WRITE);
                    OutputStream os = encryptor.getDataStream(fs)) {
                    opc.save(os);
                }

                // Write out the encrypted version
                try(FileOutputStream fos = new FileOutputStream(file)) {
                    fs.writeFilesystem(fos);
                }
            }

            InputStream in = new FileInputStream(file);
            file.delete();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment;filename=draw_result.xlsx");

            return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
        } catch(Exception e) {
            log.error("excelDownload ERROR", e);
            return null;
        }
    }

    @Operation(summary = "카카오톡 공유하기 콜백 url", description = "카카오톡에서 공유 성공하면 호출할거..  공유하기 할때 <strong>serverCallbackArgs: {id: 사용자 id}</strong> 만 넣어서 호출해주세요 !!!"
                                                                + "<br /><br />여기서 하는일: 카카오에서 보내준 userId 기준으로<br />"
                                                                + "1. db에서 user 존재여부 확인<br />"
                                                                + "2. user 당첨결과 삭제<br />"
                                                                + "3. user가 당첨되었던 당첨금액의 총 당첨자 수 -1")
    @PostMapping(value = {"/kakao-share"})
    public void kakaoShareCallBack(@RequestBody UserEntity userFromRequestBody) {
        log.info("==============================================================");
        log.info("kakaoShareCallBack CALL START, userVO={}", userFromRequestBody);
        log.info("==============================================================");

        boolean isSucceed = promotionService.removeDrawResultAsUserSharedByKakaoTalk(userFromRequestBody);
        if(!isSucceed) {
            log.info("==============================================================");
            log.info("kakaoShareCallBack userFromDb is null OR userDrawCnt > 4, userVO={}", userFromRequestBody);
            log.info("==============================================================");
        }
    }


    @Operation(summary = "공유하기 성공여부 확인", description = "사용자가 카톡 공유 성공했는지 실패했는지 확인하는 api<br /><br />requestBody에 아래 처럼 호출해주세요!!!<br /><strong>{contact: 'awfwafwkejnwkf=='}</strong>")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "<strong>{success : true}</strong> 면 공유 성공! <strong>{success : false}</strong> 면 공유 아직 안된거(카톡이 callBack 아직 호출안한거)"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR !! 내가 뭔가를 잘못했거나.. 요청값이 제대로 맞지 않아서 에러가 났거나..")
    })
    @PostMapping(value = {"/share"})
    public ResponseEntity<Map<String, Boolean>> isKakaoShareSucceed(@RequestBody Map<String, String> reqBody) {
        Map<String, Boolean> result = new HashMap<>();
        boolean isSucceed = promotionService.isKakaoShareSucced(reqBody.get("contact"));
        result.put("success", isSucceed);
        return ResponseEntity.ok(result);
    }

}
