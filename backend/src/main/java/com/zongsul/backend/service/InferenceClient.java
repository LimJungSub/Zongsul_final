package com.zongsul.backend.service;

import java.util.List;

/**
 * InferenceClient
 * - 외부 추론 서비스(예: EC2의 YOLO 엔드포인트) 혹은 내부 가짜 구현을 추상화합니다.
 */
public interface InferenceClient {
    List<FoodResult> infer(byte[] imageBytes, String filename);

    /** 음식 이름과 남김율(0~1) 결과를 운반하기 위한 단순 구조체 */
    record FoodResult(String food, double remainingRatio) {}
}
