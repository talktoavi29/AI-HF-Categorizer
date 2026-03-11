package com.ai.hf_categorizer.controller;

import com.ai.hf_categorizer.service.ClassificationService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping("/classify")
    public Map<String, Object> classify(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return classificationService.classify(text);
    }
}