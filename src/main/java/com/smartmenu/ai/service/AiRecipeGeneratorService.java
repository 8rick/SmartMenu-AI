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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiRecipeGeneratorService {

    static final String SYSTEM_PROMPT = """
            Você é um chef especialista em aproveitamento de alimentos e marketing gastronômico.
            Crie uma sugestão de prato usando os ingredientes informados. Priorize o uso dos itens
            com vencimento mais próximo. Não sugira ingredientes como disponíveis se não estiverem
            na lista. O nome e a descrição devem ser claros, apetitosos e adequados para um cardápio.
            """;

    private static final int MAX_AUTOMATIC_INGREDIENTS = 10;

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

    /** Generates a dish from explicitly selected ingredients. */
    public PromoDishResponse generate(List<Long> ingredientIds) {
        List<Ingredient> ingredients = loadIngredients(ingredientIds);
        return generateAndPersist(ingredients);
    }

    /**
     * Automatically selects positive stock expiring soon. Selection is deterministic;
     * the model only proposes the dish and marketing copy.
     */
    public PromoDishResponse generateFromExpiringStock(int withinDays, int limit) {
        if (withinDays < 0 || withinDays > 90) {
            throw new BusinessException("O período deve estar entre 0 e 90 dias");
        }
        if (limit < 1 || limit > MAX_AUTOMATIC_INGREDIENTS) {
            throw new BusinessException(
                    "A quantidade de ingredientes deve estar entre 1 e " + MAX_AUTOMATIC_INGREDIENTS
            );
        }

        LocalDate today = LocalDate.now();
        List<Ingredient> ingredients = ingredientRepository
                .findByExpirationDateBetweenAndQuantityGreaterThanOrderByExpirationDateAsc(
                        today, today.plusDays(withinDays), BigDecimal.ZERO
                )
                .stream()
                .limit(limit)
                .toList();

        if (ingredients.isEmpty()) {
            throw new BusinessException(
                    "Nenhum ingrediente com estoque disponível vence dentro do período informado"
            );
        }

        return generateAndPersist(ingredients);
    }

    private PromoDishResponse generateAndPersist(List<Ingredient> ingredients) {
        GeneratedPromoDish generated = askAi(ingredients);
        validateGenerated(generated);

        PromoDish promoDish = new PromoDish(
                generated.name().trim(),
                generated.marketingDescription().trim(),
                generated.originalPrice(),
                generated.promoPrice()
        );
        // Keep the external LLM request outside a database transaction.
        return PromoDishMapper.toResponse(promoDishRepository.save(promoDish));
    }

    private List<Ingredient> loadIngredients(List<Long> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            throw new BusinessException("Informe ao menos um ingrediente");
        }

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
                Ingredientes disponíveis no estoque:
                %s

                Crie uma sugestão usando prioritariamente os ingredientes que vencem primeiro.
                Não invente ingredientes disponíveis. Se a combinação for inadequada, explique isso
                na descrição e sugira uma preparação coerente.

                Retorne os campos:
                - name: nome do prato
                - marketingDescription: descrição comercial curta
                - originalPrice: preço original em reais
                - promoPrice: preço promocional em reais, menor que o original

                Os preços são sugestões do modelo, não representam custo real nem cálculo de margem.
                Não afirme que foram calculados com base no custo dos ingredientes.
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
