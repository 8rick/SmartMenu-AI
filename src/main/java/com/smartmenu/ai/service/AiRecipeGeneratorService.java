package com.smartmenu.ai.service;

import com.smartmenu.ai.dto.PromoDishMapper;
import com.smartmenu.ai.dto.PromoDishResponse;
import com.smartmenu.ai.infra.exception.BusinessException;
import com.smartmenu.ai.infra.exception.ResourceNotFoundException;
import com.smartmenu.ai.model.Ingredient;
import com.smartmenu.ai.model.PromoDish;
import com.smartmenu.ai.repository.IngredientRepository;
import com.smartmenu.ai.repository.PromoDishRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiRecipeGeneratorService {

    static final String SYSTEM_PROMPT = """
            Você é um chef especialista em marketing gastronômico. Vou te passar uma lista de ingredientes \
            que estão sobrando no restaurante. Crie uma ideia de prato promocional usando esses itens, \
            um nome chamativo e uma descrição curta e persuasiva para vender no iFood.
            """;

    private final ChatClient chatClient;
    private final IngredientRepository ingredientRepository;
    private final PromoDishRepository promoDishRepository;

    public AiRecipeGeneratorService(
            ChatClient.Builder chatClientBuilder,
            IngredientRepository ingredientRepository,
            PromoDishRepository promoDishRepository
    ) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.ingredientRepository = ingredientRepository;
        this.promoDishRepository = promoDishRepository;
    }

    @Transactional
    public PromoDishResponse generate(List<Long> ingredientIds) {
        List<Ingredient> ingredients = loadIngredients(ingredientIds);
        GeneratedPromoDish generated = askAi(ingredients);
        validateGenerated(generated);

        PromoDish promoDish = new PromoDish(
                generated.name().trim(),
                generated.marketingDescription().trim(),
                generated.originalPrice(),
                generated.promoPrice()
        );
        return PromoDishMapper.toResponse(promoDishRepository.save(promoDish));
    }

    private List<Ingredient> loadIngredients(List<Long> ingredientIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(ingredientIds);
        List<Ingredient> ingredients = ingredientRepository.findAllById(uniqueIds);

        if (ingredients.size() != uniqueIds.size()) {
            Set<Long> foundIds = ingredients.stream().map(Ingredient::getId).collect(Collectors.toSet());
            List<Long> missing = uniqueIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException("Ingredientes não encontrados: " + missing);
        }
        return ingredients;
    }

    private GeneratedPromoDish askAi(List<Ingredient> ingredients) {
        String userPrompt = buildUserPrompt(ingredients);
        GeneratedPromoDish generated = chatClient.prompt()
                .user(userPrompt)
                .call()
                .entity(GeneratedPromoDish.class);

        if (generated == null) {
            throw new BusinessException("A IA não retornou um prato promocional válido");
        }
        return generated;
    }

    private String buildUserPrompt(List<Ingredient> ingredients) {
        String items = ingredients.stream()
                .map(ingredient -> "- %s (quantidade: %s, validade: %s)".formatted(
                        ingredient.getName(),
                        ingredient.getQuantity(),
                        ingredient.getExpirationDate()
                ))
                .collect(Collectors.joining("\n"));

        return """
                Ingredientes disponíveis:
                %s

                Responda apenas com um JSON contendo:
                - name: nome chamativo do prato
                - marketingDescription: descrição curta e persuasiva para o iFood
                - originalPrice: preço original em reais (número decimal)
                - promoPrice: preço promocional em reais, menor que o original (número decimal)
                """.formatted(items);
    }

    private void validateGenerated(GeneratedPromoDish generated) {
        if (generated.name() == null || generated.name().isBlank()
                || generated.marketingDescription() == null || generated.marketingDescription().isBlank()
                || generated.originalPrice() == null || generated.promoPrice() == null) {
            throw new BusinessException("A IA retornou dados incompletos para o prato promocional");
        }
        if (generated.originalPrice().compareTo(BigDecimal.ZERO) <= 0
                || generated.promoPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Os preços gerados pela IA devem ser positivos");
        }
        if (generated.promoPrice().compareTo(generated.originalPrice()) >= 0) {
            throw new BusinessException("O preço promocional deve ser menor que o preço original");
        }
    }

    public record GeneratedPromoDish(
            String name,
            String marketingDescription,
            BigDecimal originalPrice,
            BigDecimal promoPrice
    ) {
    }
}
