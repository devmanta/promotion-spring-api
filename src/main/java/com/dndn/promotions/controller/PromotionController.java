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
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.CrossOrigin;
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

    @Operation(summary = "????????????!!", description = "???????????? ?????? ?????? ??? ??????!!"
        + "<br /><br />requestBody: <strong>{contact : '???????????? ?????????'}</strong>   ??? ????????? ??????????????????!!!<br /><br />")
    @PostMapping(value = "/draw")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "1. amount??? ?????? <br />"
                                                        + "    1-1. amount !=0 ????????? ???????????? ?????? ????????????<br />"
                                                        + "    1-2. amount == 0(?????????) && soldOut == true ?????? ?????? ????????????!!<br />"
                                                        + "    1-3. amount == 0(?????????) && soldOut == false??? ??????????????? ?????? ?????????~ ?????? ???????????? ??????!!<br />"
                                                        + "2. amount??? ?????? <br />"
                                                        + "    2-1. '?????? ??????????????????'??? ?????? ???????????? ????????????<br />"
                                                        + "    2-2. amount??? ?????? ????????? drawCnt > 4 ??? ????????? ???????????? ?????? ?????????!!  drawCnt < 4??? ?????? ??????????????????!!"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR !! ?????? ????????? ???????????????.. ???????????? ????????? ?????? ????????? ????????? ?????????..")
    })
    public ResponseEntity<UserEntity> doUserDraw(HttpServletRequest request, @RequestBody UserEntity userEntity) throws Exception {
        return promotionService.doUserDraw(userEntity);
    }


    @GetMapping(value = "/XPPFpsdineHPqmA7ZhUiFWIarzGGIlk8")
    public void downloadExcelForDrawResult(HttpServletRequest request, HttpServletResponse response) {
//    public ResponseEntity<InputStreamResource> downloadExcelForDrawResult(HttpServletRequest req) {
        String clientIp = promotionService.getClientIp(request);
        log.info("======IP=======");
        log.info(clientIp);
        log.info("===============");

        if(!"116.44.67.124".equals(clientIp)) {
            return;
        }

        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("????????????");
            int rowNo = 0;

            Row headerRow = sheet.createRow(rowNo++);
            headerRow.createCell(0).setCellValue("??????");
            headerRow.createCell(1).setCellValue("?????????");
            headerRow.createCell(2).setCellValue("????????????");
            headerRow.createCell(3).setCellValue("????????????");

            List<UserDrawResultEntity> list = promotionService.getDrawResult();
            for(UserDrawResultEntity r : list) {
                Row row = sheet.createRow(rowNo++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(promotionService.decryptContact(r.getContact()));
                row.createCell(2).setCellValue(r.getAmount());
                row.createCell(3).setCellValue(r.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            response.setContentType("ms-vnd/excel");
            response.setHeader("Content-Disposition", "attachment;filename=draw_result.xlsx");

            workbook.write(response.getOutputStream());

//            File file = new File(System.getProperty("user.dir") + "/z.xlsx");
//            log.info("=====FILE FILE FILEF ILFL EIFELIFELIF =======");
//            log.info(file.getAbsolutePath());
//            log.info("=====FILE FILE FILEF ILFL EIFELIFELIF =======");
//
//            if(!file.exists()) {
//                file.delete();
//            }
//
//            file.createNewFile();
//            OutputStream fileOut = new FileOutputStream(file);
//            workbook.write(fileOut);
//            fileOut.close();
//
//            try(POIFSFileSystem fs = new POIFSFileSystem()) {
//
//                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
//                Encryptor encryptor = info.getEncryptor();
//                encryptor.confirmPassword(myNum);
//
//                try(OPCPackage opc = OPCPackage.open(file, PackageAccess.READ_WRITE);
//                    OutputStream os = encryptor.getDataStream(fs)) {
//                    opc.save(os);
//                }
//
//                // Write out the encrypted version
//                try(FileOutputStream fos = new FileOutputStream(file)) {
//                    fs.writeFilesystem(fos);
//                }
//            }
//
//            InputStream in = new FileInputStream(file);
//            file.delete();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Disposition", "attachment;filename=draw_result.xlsx");
//
//            return ResponseEntity
//                .ok()
//                .headers(headers)
//                .body(new InputStreamResource(in));
        } catch(Exception e) {
            log.error("excelDownload ERROR", e);
        }
    }

    @Operation(summary = "???????????? ???????????? ?????? url", description = "?????????????????? ?????? ???????????? ????????????..  ???????????? ?????? <strong>serverCallbackArgs: {id: ????????? id}</strong> ??? ????????? ?????????????????? !!!"
                                                                + "<br /><br />????????? ?????????: ??????????????? ????????? userId ????????????<br />"
                                                                + "1. db?????? user ???????????? ??????<br />"
                                                                + "2. user ???????????? ??????<br />"
                                                                + "3. user??? ??????????????? ??????????????? ??? ????????? ??? -1")
    @PostMapping(value = {"/kakao-share"})
//    public void kakaoShareCallBack(@RequestBody UserEntity userFromRequestBody) {
    public void kakaoShareCallBack(@RequestBody Map<String, Object> requestBody) {
        log.info("==============================================================");
        log.info("kakaoShareCallBack CALL START, requestBodyFromKakaoTalk={}", requestBody);
        log.info("==============================================================");

        if(!"MemoChat".equals(requestBody.get("CHAT_TYPE"))) {
            UserEntity userFromRequestBody = new UserEntity();
            userFromRequestBody.setContact(String.valueOf(requestBody.get("contact")));
            boolean isSucceed = promotionService.removeDrawResultAsUserSharedByKakaoTalk(userFromRequestBody);
            if(!isSucceed) {
                log.info("==============================================================");
                log.info("kakaoShareCallBack userFromDb is null OR userDrawCnt > 4, userVO={}", userFromRequestBody);
                log.info("==============================================================");
            }
        } else{
            log.info("==============================================================");
            log.info("kakaoShareCallBack user shared to him/herself {}", requestBody);
            log.info("==============================================================");
        }
    }


    @Operation(summary = "???????????? ???????????? ??????", description = "???????????? ?????? ?????? ??????????????? ??????????????? ???????????? api<br /><br />requestBody??? ?????? ?????? ??????????????????!!!<br /><strong>{contact: 'awfwafwkejnwkf=='}</strong>")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "<strong>{success : true}</strong> ??? ?????? ??????! <strong>{success : false}</strong> ??? ?????? ?????? ?????????(????????? callBack ?????? ???????????????)"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR !! ?????? ????????? ???????????????.. ???????????? ????????? ?????? ????????? ????????? ?????????..")
    })
    @PostMapping(value = {"/share"})
    public ResponseEntity<Map<String, Boolean>> isKakaoShareSucceed(@RequestBody Map<String, String> reqBody) {
        Map<String, Boolean> result = new HashMap<>();
        String contact = reqBody.get("contact");
        result.put("success", promotionService.isKakaoShareSucced(contact));
        result.put("done", promotionService.isDrawFinishedPerUser(contact));
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/test")
    public void test() throws Exception{
        File initialFile = new File(System.getProperty("user.dir") + "/z.xlsx");
        InputStream input = new FileInputStream(initialFile);

        int i = 0;
        while((i = input.read()) != -1) {
            System.out.write(i);
        }
    }
}
