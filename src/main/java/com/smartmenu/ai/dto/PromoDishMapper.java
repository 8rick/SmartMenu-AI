package com.smartmenu.ai.dto;

import com.smartmenu.ai.model.PromoDish;

public final class PromoDishMapper {

    private PromoDishMapper() {
    }

    public static PromoDishResponse toResponse(PromoDish promoDish) {
        return new PromoDishResponse(
                promoDish.getId(),
                promoDish.getName(),
                promoDish.getMarketingDescription(),
                promoDish.getOriginalPrice(),
                promoDish.getPromoPrice(),
                promoDish.getCreatedAt()
        );
    }
}
