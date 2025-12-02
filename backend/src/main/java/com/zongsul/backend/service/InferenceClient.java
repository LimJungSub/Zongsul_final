package com.zongsul.backend.service;

import java.util.Map;

public interface InferenceClient {
    Map<String, Double> infer(byte[] imageBytes, String filename);
}
