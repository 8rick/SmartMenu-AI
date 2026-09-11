package com.smartmenu.ai.util;

/**
 * Utilitario de validacao de nomes de usuario baseado no desafio
 * CodelandUsernameValidation.
 *
 * <p>Regras de negocio aplicadas:
 * <ul>
 *   <li>Regra 1: A string deve ter entre 4 e 25 caracteres (inclusive).</li>
 *   <li>Regra 2: Deve comecar obrigatoriamente com uma letra (a-z ou A-Z).</li>
 *   <li>Regra 3: Pode conter apenas letras, numeros e o caractere sublinhado (_).</li>
 *   <li>Regra 4: Nao pode terminar com o caractere sublinhado (_).</li>
 * </ul>
 *
 * <p>A implementacao utiliza uma unica expressao regular que encapsula todas as
 * quatro regras de forma atomica e eficiente.
 */
public class UsernameValidator {

    /**
     * Regex que implementa todas as 4 regras simultaneamente:
     * <ul>
     *   <li>{@code ^[a-zA-Z]}        - Ancora no inicio e exige uma letra como 1o caractere (Regra 2).</li>
     *   <li>{@code [a-zA-Z0-9_]{2,23}} - Entre 2 e 23 chars do conjunto permitido no meio (Regra 3).
     *                                    Junto com o 1o e o ultimo char, garante total de 4-25 (Regra 1).</li>
     *   <li>{@code [a-zA-Z0-9]}      - Ancora no final sem sublinhado (Regra 4).</li>
     *   <li>{@code $}                - Fim da string.</li>
     * </ul>
     *
     * <p><b>Atencao:</b> Exige no minimo 4 chars (1+2+1) e no maximo 25 (1+23+1).
     */
    private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9_]{2,23}[a-zA-Z0-9]$";

    // Construtor privado: classe utilitaria nao deve ser instanciada.
    private UsernameValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Valida se o nome de usuario fornecido atende a todas as regras de negocio.
     *
     * @param username o nome de usuario a ser validado; pode ser {@code null}.
     * @return {@code true} se o username for valido; {@code false} caso contrario.
     */
    public static boolean isValid(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return username.matches(USERNAME_REGEX);
    }
}
