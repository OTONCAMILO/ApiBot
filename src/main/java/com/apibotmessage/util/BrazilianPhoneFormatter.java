package com.apibotmessage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BrazilianPhoneFormatter {

    private static final Logger log = LoggerFactory.getLogger(BrazilianPhoneFormatter.class);
    private static final String BRAZIL_COUNTRY_CODE = "55";

    /**
     * Normaliza número para formato E.164 exigido pela Meta:
     * - Apenas dígitos
     * - Com código do país (55)
     * - Com DDD
     * - Com 9º dígito para celulares (9 dígitos após DDD)
     *
     * Exemplos de entrada → saída:
     * "11999999999"       → "5511999999999"
     * "5511999999999"     → "5511999999999"
     * "+55 11 99999-9999" → "5511999999999"
     * "(11) 9999-9999"    → "551199999999" (fixo 8 dígitos - mantém)
     * "11 99999-9999"     → "5511999999999"
     */
    public String toE164(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Telefone não pode ser vazio");
        }

        // Remove tudo que não é dígito
        String digits = raw.replaceAll("\\D", "");

        // Se já começa com 55, assume que já está no formato correto
        if (digits.startsWith(BRAZIL_COUNTRY_CODE)) {
            return validateAndFixNinthDigit(digits);
        }

        // Se tem 10 ou 11 dígitos (DDD + número), adiciona 55
        if (digits.length() == 10 || digits.length() == 11) {
            return validateAndFixNinthDigit(BRAZIL_COUNTRY_CODE + digits);
        }

        // Se tem 12 ou 13 dígitos e começa com 55 (ex: 551199999999)
        if ((digits.length() == 12 || digits.length() == 13) && digits.startsWith(BRAZIL_COUNTRY_CODE)) {
            return validateAndFixNinthDigit(digits);
        }

        log.warn("Formato de telefone não reconhecido: {} (dígitos: {})", raw, digits);
        // Tenta melhor esforço: se tem 11+ dígitos, assume Brasil
        if (digits.length() >= 11) {
            return validateAndFixNinthDigit(digits);
        }

        throw new IllegalArgumentException("Formato de telefone inválido: " + raw);
    }

    /**
     * Garante que celulares tenham 9 dígitos após o DDD (nono dígito).
     * Se tem 12 dígitos (55 + DDD + 8 dígitos), insere '9' após o DDD.
     * Se tem 13 dígitos, assume que já está correto.
     */
    private String validateAndFixNinthDigit(String withCountryCode) {
        // Formato esperado: 55 + DDD(2) + número(8 ou 9) = 12 ou 13 dígitos
        if (withCountryCode.length() == 12) {
            // 55 + DDD(2) + 8 dígitos = fixo ou celular sem nono dígito
            // Insere '9' após o DDD (posição 4)
            String ddd = withCountryCode.substring(2, 4);
            String number = withCountryCode.substring(4);
            log.debug("Inserindo nono dígito para: {} -> 55{}9{}", withCountryCode, ddd, number);
            return withCountryCode.substring(0, 4) + "9" + number;
        }
        if (withCountryCode.length() == 13) {
            // 55 + DDD(2) + 9 dígitos = já correto
            return withCountryCode;
        }
        // Outros tamanhos: retorna como está (pode ser número internacional, short code, etc)
        return withCountryCode;
    }

    /** Verifica se é celular brasileiro (9 dígitos após DDD) */
    public boolean isMobile(String e164) {
        if (e164 == null || !e164.startsWith(BRAZIL_COUNTRY_CODE)) return false;
        if (e164.length() != 13) return false;
        // DDDs de celular: 11-99 (todos podem ter celular)
        return true;
    }

    /** Formata para exibição amigável: +55 (11) 99999-9999 */
    public String formatForDisplay(String e164) {
        if (e164 == null || e164.length() < 10) return e164;
        if (e164.startsWith(BRAZIL_COUNTRY_CODE)) {
            String rest = e164.substring(2);
            if (rest.length() == 11) { // celular
                return String.format("+55 (%s) %s-%s", rest.substring(0, 2), rest.substring(2, 7), rest.substring(7));
            } else if (rest.length() == 10) { // fixo
                return String.format("+55 (%s) %s-%s", rest.substring(0, 2), rest.substring(2, 6), rest.substring(6));
            }
        }
        return e164;
    }
}