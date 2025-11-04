package com.zongsul.backend.api;

import com.zongsul.backend.domain.plate.PlateItem;
import com.zongsul.backend.domain.plate.PlateResult;
import com.zongsul.backend.domain.plate.PlateResultRepository;
import com.zongsul.backend.service.InferenceClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/upload")
@Validated
public class UploadController {

    private final InferenceClient inferenceClient;
    private final PlateResultRepository plateResultRepository;

    public UploadController(InferenceClient inferenceClient, PlateResultRepository plateResultRepository) {
        this.inferenceClient = inferenceClient;
        this.plateResultRepository = plateResultRepository;
    }

    public record PlateAnalysisResponse(String plate_id, Map<String, Double> result) {}

    // 프론트는 multipart form data 로 파일을 전송, 필드 이름은 files 로 여러 장을 보낼 수 있음
    // 한 파일은 한 사람의 식판으로 간주함
    // 이미지는 디스크에 저장하지 않고 분석 결과만 DB에 적재하도록 함
    // POST /upload로 동작
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<PlateAnalysisResponse> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        // 응답으로 돌려줄 식판별 분석 결과 리스트를 준비함
        List<PlateAnalysisResponse> responses = new ArrayList<>();

        // 업로드된 파일들을 하나씩 처리함
        for (MultipartFile f : files) {
            // 원본 파일명이 있으면 쓰고 없으면 임의의 식별자를 만들고 사용함
            String original = Optional.ofNullable(f.getOriginalFilename()).orElse(UUID.randomUUID().toString());
            // 확장자를 제거한 값을 plateId 로 사용함
            String plateId = original.replaceAll("\\..*$", "");

            /*
            * 아마 여기에 OpenCV 로직을 넣어서 잔반량을 픽셀단위로 계산해야할듯
            * */

            // 추론 모듈을 호출하여 음식 이름과 남김 비율 목록을 얻음
            var inferred = inferenceClient.infer(f.getBytes(), original);

            // 분석 결과를 DB에 기록하기 위해 루트 엔티티를 만들고 자식 항목을 연결함
            PlateResult pr = new PlateResult(plateId);
            for (InferenceClient.FoodResult fr : inferred) {
                pr.addItem(new PlateItem(fr.food(), fr.remainingRatio()));
            }
            // 저장 시 영속성 전이로 자식 항목까지 함께 insert 됨
            plateResultRepository.save(pr);

            // 프론트로 돌려줄 응답 맵을 구성함
            Map<String, Double> map = new LinkedHashMap<>();
            for (InferenceClient.FoodResult fr : inferred) {
                map.put(fr.food(), fr.remainingRatio());
            }
            responses.add(new PlateAnalysisResponse(plateId, map));
        }
        // 모든 파일 처리 후 결과를 반환함
        return responses;
    }
}
