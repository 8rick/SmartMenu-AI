package com.smartmenu.ai.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite completa de testes unitarios para {@link UsernameValidator}.
 *
 * <p>Organizacao por blocos (@Nested) seguindo as seguintes estrategias:
 * <ol>
 *   <li><b>Caminho Feliz</b>  - entradas plenamente validas.</li>
 *   <li><b>Edge Cases</b>     - limites exatos de tamanho e entradas degeneradas.</li>
 *   <li><b>Fail Fast</b>      - falhas rapidas por violacao de cada regra especifica.</li>
 *   <li><b>Dados Legados</b>  - entradas complexas com sublinhados e numeros no meio.</li>
 * </ol>
 *
 * <p>Convencao de nomenclatura dos metodos:
 * <pre>
 *   deveria[Retornar][Resultado]_quando[Condicao]
 * </pre>
 */
@DisplayName("UsernameValidator - Suite de Testes Unitarios")
class UsernameValidatorTest {

    // =========================================================================
    // 1. CAMINHO FELIZ (Happy Path)
    //    Objetivo: garantir que usernames perfeitamente validos sejam aceitos.
    //    Usa @ParameterizedTest + @ValueSource para eliminar codigo repetitivo.
    // =========================================================================
    @Nested
    @DisplayName("1. Caminho Feliz - Entradas Validas")
    class CaminhoFeliz {

        /**
         * Testa multiplos usernames validos de forma concisa.
         * Cada valor eh executado como um caso de teste independente pelo JUnit 5.
         */
        @ParameterizedTest(name = "[{index}] username valido: \"{0}\"")
        @ValueSource(strings = {
                "John1234",        // letras + numeros, sem sublinhado
                "alice_doe",       // letras + sublinhado no meio
                "Bob_2024",        // letra maiuscula + sublinhado no meio + numero
                "ALLCAPS1234",     // apenas maiusculas + numeros
                "lowercase_ok",   // apenas minusculas + sublinhado no meio
                "Mix3d_CaSe",      // mistura de casos + sublinhado + numero
                "a_b_c_d_e_1234"  // varios sublinhados no meio
        })
        @DisplayName("Deve retornar TRUE para usernames validos")
        void deveriaTerResultadoTrue_quandoUsernameValido(String username) {
            assertTrue(UsernameValidator.isValid(username),
                    "Esperado TRUE para username valido: " + username);
        }
    }

    // =========================================================================
    // 2. EDGE CASES (Casos de Borda)
    //    Objetivo: testar os limites exatos das regras, evitando off-by-one errors.
    //    Casos criticos: tamanhos 3, 4, 25 e 26 chars; strings vazias e nulas.
    // =========================================================================
    @Nested
    @DisplayName("2. Edge Cases - Limites e Entradas Degeneradas")
    class EdgeCases {

        // --- Limites de tamanho: exatamente no limite inferior ---

        @Test
        @DisplayName("Deve retornar FALSE para username com 3 caracteres (abaixo do minimo)")
        void deveriaRetornarFalse_quandoTamanho3() {
            // "Abc" tem 3 chars - viola a Regra 1 (minimo 4)
            assertFalse(UsernameValidator.isValid("Abc"),
                    "3 chars esta abaixo do minimo de 4");
        }

        @Test
        @DisplayName("Deve retornar TRUE para username com exatamente 4 caracteres (limite inferior)")
        void deveriaRetornarTrue_quandoTamanho4() {
            // "Ab1c" tem exatamente 4 chars - no limite minimo valido
            assertTrue(UsernameValidator.isValid("Ab1c"),
                    "4 chars eh o tamanho minimo valido");
        }

        @Test
        @DisplayName("Deve retornar TRUE para username com exatamente 25 caracteres (limite superior)")
        void deveriaRetornarTrue_quandoTamanho25() {
            // 25 chars: 1 letra + 23 chars do meio + 1 char final
            String username25 = "A" + "a".repeat(23) + "1"; // "A" + 23x"a" + "1" = 25 chars
            assertEquals25chars(username25);
            assertTrue(UsernameValidator.isValid(username25),
                    "25 chars eh o tamanho maximo valido");
        }

        @Test
        @DisplayName("Deve retornar FALSE para username com 26 caracteres (acima do maximo)")
        void deveriaRetornarFalse_quandoTamanho26() {
            // 26 chars: 1 letra + 24 chars do meio + 1 char final
            String username26 = "A" + "a".repeat(24) + "1"; // = 26 chars
            assertFalse(UsernameValidator.isValid(username26),
                    "26 chars esta acima do maximo de 25");
        }

        // --- Entradas degeneradas ---

        @Test
        @DisplayName("Deve retornar FALSE para string vazia")
        void deveriaRetornarFalse_quandoStringVazia() {
            assertFalse(UsernameValidator.isValid(""),
                    "String vazia nao e um username valido");
        }

        @Test
        @DisplayName("Deve retornar FALSE para string com apenas espacos em branco")
        void deveriaRetornarFalse_quandoApenasEspacos() {
            assertFalse(UsernameValidator.isValid("    "),
                    "String em branco nao e um username valido");
        }

        /**
         * @NullSource injeta automaticamente o valor null como parametro do teste.
         * Garante que o metodo nao lance NullPointerException e retorne false.
         */
        @ParameterizedTest(name = "Entrada nula deve retornar FALSE")
        @NullSource
        @DisplayName("Deve retornar FALSE para entrada null (sem NullPointerException)")
        void deveriaRetornarFalse_quandoNull(String username) {
            assertFalse(UsernameValidator.isValid(username),
                    "Null nao deve causar excecao e deve retornar FALSE");
        }

        /** Metodo auxiliar para deixar o teste de 25 chars mais legivel. */
        private void assertEquals25chars(String s) {
            assert s.length() == 25 : "String auxiliar deve ter 25 chars, tinha: " + s.length();
        }
    }

    // =========================================================================
    // 3. FAIL FAST - Violacoes especificas de cada regra
    //    Objetivo: garantir que a validacao falhe IMEDIATAMENTE ao encontrar
    //    a violacao de uma regra especifica, sem ambiguidade.
    // =========================================================================
    @Nested
    @DisplayName("3. Fail Fast - Violacao das Regras de Negocio")
    class FailFast {

        // --- Regra 2: Deve comecar com uma letra ---

        /**
         * Testa que usernames iniciados com digitos ou underscores sao rejeitados.
         * A Regra 2 exige que o PRIMEIRO caractere seja obrigatoriamente uma letra.
         */
        @ParameterizedTest(name = "[{index}] Deve falhar: inicio invalido \"{0}\"")
        @ValueSource(strings = {
                "1username",   // comeca com digito
                "9abcdef",     // comeca com numero
                "_username",   // comeca com sublinhado
                "_1234abc"     // comeca com sublinhado + numero
        })
        @DisplayName("Regra 2: Deve retornar FALSE quando NAO inicia com letra")
        void deveriaFalhar_regra2_quandoNaoIniciaComLetra(String username) {
            assertFalse(UsernameValidator.isValid(username),
                    "Viola Regra 2: username NAO pode iniciar com '" + username.charAt(0) + "'");
        }

        // --- Regra 3: Apenas letras, numeros e sublinhado ---

        /**
         * Testa caracteres especiais que NAO fazem parte do conjunto [a-zA-Z0-9_].
         * Cada entrada eh um username que parece valido mas contem 1 char ilegal.
         */
        @ParameterizedTest(name = "[{index}] Deve falhar: char especial em \"{0}\"")
        @ValueSource(strings = {
                "user@name",   // arobase (@) - comum em emails
                "user-name",   // hifen (-) - frequente em slugs
                "user!name",   // exclamacao (!) - char especial
                "user name",   // espaco em branco
                "user.name",   // ponto (.) - comum em dominios
                "user#tag",    // hashtag (#)
                "user$money",  // cifrao ($)
                "user%rate",   // percentual (%)
                "user+plus",   // mais (+)
                "us<er>name"   // tags HTML
        })
        @DisplayName("Regra 3: Deve retornar FALSE quando contem caractere especial invalido")
        void deveriaFalhar_regra3_quandoContemCaractereEspecial(String username) {
            assertFalse(UsernameValidator.isValid(username),
                    "Viola Regra 3: username contem caractere especial invalido em: " + username);
        }

        // --- Regra 4: Nao pode terminar com sublinhado ---

        /**
         * Testa que usernames terminados com '_' sao sempre rejeitados,
         * independentemente do restante da string estar correto.
         */
        @ParameterizedTest(name = "[{index}] Deve falhar: termina com '_' em \"{0}\"")
        @ValueSource(strings = {
                "username_",     // sublinhado simples no final
                "John_Doe_",     // sublinhado duplo no final
                "valid_name_",   // nome valido corrompido no final
                "abcd_"          // tamanho minimo mas termina com sublinhado
        })
        @DisplayName("Regra 4: Deve retornar FALSE quando termina com sublinhado")
        void deveriaFalhar_regra4_quandoTerminaComSublinhado(String username) {
            assertFalse(UsernameValidator.isValid(username),
                    "Viola Regra 4: username NAO pode terminar com '_': " + username);
        }
    }

    // =========================================================================
    // 4. DADOS LEGADOS (Legacy Data)
    //    Objetivo: simular dados vindos de sistemas antigos ou importacoes que
    //    possuem sublinhados no meio, numeros e misturas de caso.
    //    Garante que o Regex [a-zA-Z0-9_]+ NAO rejeite casos validos complexos.
    // =========================================================================
    @Nested
    @DisplayName("4. Dados Legados - Entradas Complexas e Historicas")
    class DadosLegados {

        /**
         * Testa usernames tipicos de sistemas legados, com:
         * - Sublinhados multiplos no meio da string
         * - Numeros intercalados
         * - Mistura de maiusculas e minusculas
         * - Formatos de ID de sistemas antigos
         */
        @ParameterizedTest(name = "[{index}] Dado legado valido: \"{0}\"")
        @ValueSource(strings = {
                "user_123_abc",      // sublinhado duplo no meio + numeros
                "Sys_Usr_2019",      // formato de usuario de sistema legado
                "Admin_001_bkp",     // admin com numero de sequencia e sufixo
                "OLD_SYSTEM_V2",     // convencao de nomenclatura antiga em maiusculas
                "migrated_user01",   // prefixo de migracao + numero
                "backup_db_2020",    // nome de backup com ano
                "Legacy_Data_99x",   // sufixo alfanumerico de sistema legado
                "Srv_Node_A1B2",     // id de servidor com alternancia alfanumerica
                "Db_Read_Write3",    // nome de role de banco de dados legado
                "Api_V2_Usr_Mgr1"    // nome de servico complexo com multiplos underscores
        })
        @DisplayName("Deve retornar TRUE para dados legados com sublinhados no meio e numeros")
        void deveriaRetornarTrue_quandoDadoLegadoValido(String username) {
            assertTrue(UsernameValidator.isValid(username),
                    "Dado legado deveria ser aceito pelo validador: " + username);
        }

        /**
         * Caso especifico: sequencias de sublinhados consecutivos no meio.
         * O Regex [a-zA-Z0-9_]+ permite isso pois sublinhado esta no conjunto.
         */
        @Test
        @DisplayName("Deve retornar TRUE para username com sublinhados consecutivos no meio")
        void deveriaRetornarTrue_quandoSublinhados_ConsecutivosNoMeio() {
            // "Abc__1234" - dois sublinhados consecutivos no meio, sem violar as regras
            assertTrue(UsernameValidator.isValid("Abc__1234"),
                    "Sublinhados consecutivos no MEIO sao permitidos pela Regra 3");
        }

        /**
         * Caso de regressao: garante que o parser nao confunde 'o' (letra O) com '0' (zero)
         * em dados de sistemas antigos que misturavam os dois.
         */
        @Test
        @DisplayName("Deve retornar TRUE para username legado com letras O e numeros 0 misturados")
        void deveriaRetornarTrue_quandoLetrasENumerosParecidos() {
            // "PrefO0x_Mix1" - mistura letra O (maiusculo), numero 0, e mais
            assertTrue(UsernameValidator.isValid("PrefO0x_Mix1"),
                    "Mistura de letras O e numeros 0 deve ser aceita");
        }

        /**
         * Caso de regressao: garante que dados importados com numeros no inicio do SUFIXO
         * (mas nao no inicio do username) sejam aceitos corretamente.
         */
        @Test
        @DisplayName("Deve retornar TRUE para usuario legado terminando com numero apos sublinhado")
        void deveriaRetornarTrue_quandoTerminaComNumeroAposSublinhado() {
            // "Legacy_usr_1" - termina com numero '1', que eh valido (nao eh '_')
            assertTrue(UsernameValidator.isValid("Legacy_usr_1"),
                    "Terminar com numero apos sublinhado eh valido pela Regra 4");
        }
    }
}
