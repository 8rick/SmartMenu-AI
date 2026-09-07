package com.smartmenu.ai.service;

import com.smartmenu.ai.dto.PromoDishMapper;
import com.smartmenu.ai.dto.PromoDishResponse;
import com.smartmenu.ai.infra.exception.ResourceNotFoundException;
import com.smartmenu.ai.repository.PromoDishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromoDishService {

    private final PromoDishRepository promoDishRepository;

    public PromoDishService(PromoDishRepository promoDishRepository) {
        this.promoDishRepository = promoDishRepository;
    }

    @Transactional(readOnly = true)
    public PromoDishResponse findById(Long id) {
        return promoDishRepository.findById(id)
                .map(PromoDishMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Prato promocional não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PromoDishResponse> findAll(Pageable pageable) {
        return promoDishRepository.findAll(pageable).map(PromoDishMapper::toResponse);
    }
}
