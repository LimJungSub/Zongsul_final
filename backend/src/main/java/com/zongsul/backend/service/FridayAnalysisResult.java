package com.zongsul.backend.service;

import java.util.Map;
import java.util.List;

public class FridayAnalysisResult {

    private Map<String, Double> ratios;
    private String leastPopular;
    private List<String> related;

    public FridayAnalysisResult(Map<String, Double> ratios,
                                String leastPopular,
                                List<String> related) {
        this.ratios = ratios;
        this.leastPopular = leastPopular;
        this.related = related;
    }

    public Map<String, Double> getRatios() {
        return ratios;
    }

    public String getLeastPopular() {
        return leastPopular;
    }

    public List<String> getRelated() {
        return related;
    }
}
