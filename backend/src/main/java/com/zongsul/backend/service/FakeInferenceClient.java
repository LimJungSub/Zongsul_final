package com.zongsul.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * InferenceClient의 간단한 가짜 구현체
 * - 실제 YOLO/EC2 연동 전까지 테스트용으로 사용합니다.
 */
@Service
@ConditionalOnProperty(name = "inference.mode", havingValue = "fake", matchIfMissing = true)
public class FakeInferenceClient implements InferenceClient {

    private static final List<String> FOODS = List.of("rice", "kimchi", "fish_cutlet", "soup");
    private final Random random = new Random();

    @Override
    public List<FoodResult> infer(byte[] imageBytes, String filename) {
        List<FoodResult> results = new ArrayList<>();
        for (String f : FOODS) {
            double r = Math.round(random.nextDouble(0.0, 0.9) * 100.0) / 100.0;
            results.add(new FoodResult(f, r));
        }
        return results;
    }
}
