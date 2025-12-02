package com.zongsul.backend.api;

import com.zongsul.backend.service.AnalysisService;
import com.zongsul.backend.service.FridayAnalysisResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
@CrossOrigin(
        origins = "http://zongsul-frontend.s3-website.ap-northeast-2.amazonaws.com",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("images") List<MultipartFile> images
    ) throws IOException {

        analysisService.analyzeFridayImages(images);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "count", images.size()
                )
        );
    }

    @GetMapping("/result")
    public FridayAnalysisResult getResult() {
        return analysisService.getLatestResult();
    }
}
