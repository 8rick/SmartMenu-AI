package com.smartmenu.ai.controller;

import com.smartmenu.ai.dto.GeneratePromoDishRequest;
import com.smartmenu.ai.dto.PageResponse;
import com.smartmenu.ai.dto.PromoDishResponse;
import com.smartmenu.ai.service.AiRecipeGeneratorService;
import com.smartmenu.ai.service.PromoDishService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promo-dishes")
public class PromoDishController {

    private final PromoDishService promoDishService;
    private final AiRecipeGeneratorService aiRecipeGeneratorService;

    public PromoDishController(
            PromoDishService promoDishService,
            AiRecipeGeneratorService aiRecipeGeneratorService
    ) {
        this.promoDishService = promoDishService;
        this.aiRecipeGeneratorService = aiRecipeGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<PromoDishResponse> generate(@Valid @RequestBody GeneratePromoDishRequest request) {
        PromoDishResponse response = aiRecipeGeneratorService.generate(request.ingredientIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public PromoDishResponse findById(@PathVariable Long id) {
        return promoDishService.findById(id);
    }

    @GetMapping
    public PageResponse<PromoDishResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(promoDishService.findAll(pageable));
    }
}
