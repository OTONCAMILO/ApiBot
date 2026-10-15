package com.apibotmessage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BrazilianPhoneFormatterTest {

    private final BrazilianPhoneFormatter formatter = new BrazilianPhoneFormatter();

    @Nested
    @DisplayName("Formatação para E.164 (toE164)")
    class ToE164Tests {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "'11999999999', '5511999999999'",           // celular 9 dígitos sem 55 -> 13 dígitos
            "'1199999999', '5511999999999'",            // fixo 8 dígitos sem 55 -> adiciona 55 + insere 9º dígito = 13
            "'5511999999999', '5511999999999'",         // já com 55, celular 13 dígitos
            "'551199999999', '5511999999999'",          // já com 55, 12 dígitos -> INSERE 9º dígito
            "'+55 11 99999-9999', '5511999999999'",     // formatado com máscara celular
            "'(11) 99999-9999', '5511999999999'",       // formato brasileiro celular
            "'11 99999 9999', '5511999999999'",         // espaços celular
            "'11.99999.9999', '5511999999999'",         // pontos celular
        })
        void deveFormatarCorretamente(String input, String expected) {
            assertEquals(expected, formatter.toE164(input));
        }

        @ParameterizedTest(name = "{0} deve lançar exceção")
        @CsvSource({
            "''",
            "'   '",
            "'abc'",
            "'123'",
            "'119999999'",     // 9 dígitos apenas (incompleto)
        })
        void deveLancarExcecaoParaInvalidos(String input) {
            assertThrows(IllegalArgumentException.class, () -> formatter.toE164(input));
        }
    }

    @Nested
    @DisplayName("Inserção automática do nono dígito")
    class NinthDigitTests {

        @Test
        void deveInserirNonoDigitoQuandoTem12DigitosCom55() {
            // 55 + 11 + 8 dígitos = 12 dígitos -> insere 9 (assume celular sem 9º dígito)
            String input = "551199999999"; // 12 dígitos
            String result = formatter.toE164(input);
            assertEquals("5511999999999", result); // 13 dígitos
        }

        @Test
        void naoDeveAlterarQuandoJaTem13Digitos() {
            String input = "5511999999999"; // 13 dígitos
            String result = formatter.toE164(input);
            assertEquals("5511999999999", result);
        }
    }

    @Nested
    @DisplayName("Utilitários")
    class UtilTests {

        @Test
        void isMobile_deveRetornarTrueParaCelularValido() {
            assertTrue(formatter.isMobile("5511999999999"));
        }

        @Test
        void isMobile_deveRetornarFalseParaFixo() {
            assertFalse(formatter.isMobile("551199999999"));
        }

        @Test
        void isMobile_deveRetornarFalseParaNaoBrasil() {
            assertFalse(formatter.isMobile("15551234567")); // EUA
        }

        @Test
        void formatForDisplay_deveFormatarCelular() {
            assertEquals("+55 (11) 99999-9999", formatter.formatForDisplay("5511999999999"));
        }

        @Test
        void formatForDisplay_deveFormatarFixo() {
            assertEquals("+55 (11) 9999-9999", formatter.formatForDisplay("551199999999"));
        }
    }
}