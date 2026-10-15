package com.apibotmessage.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application.properties")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /webhook - deve retornar challenge quando token válido")
    void verify_sucesso() throws Exception {
        mockMvc.perform(get("/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "test_verify_token_123")
                        .param("hub.challenge", "challenge_12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge_12345"));
    }

    @Test
    @DisplayName("GET /webhook - deve retornar 403 quando token inválido")
    void verify_tokenInvalido() throws Exception {
        mockMvc.perform(get("/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "token_errado")
                        .param("hub.challenge", "challenge_12345"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /webhook - deve retornar 403 quando mode inválido")
    void verify_modeInvalido() throws Exception {
        mockMvc.perform(get("/webhook")
                        .param("hub.mode", "unsubscribe")
                        .param("hub.verify_token", "test_verify_token_123")
                        .param("hub.challenge", "challenge_12345"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /webhook - deve retornar 200 imediatamente")
    void receive_retorna200Imediato() throws Exception {
        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [{
                    "id": "123",
                    "changes": [{
                      "field": "messages",
                      "value": {
                        "messaging_product": "whatsapp",
                        "metadata": {"display_phone_number": "15551234567", "phone_number_id": "123456789"},
                        "messages": [{
                          "from": "5511999999999",
                          "id": "wamid.test",
                          "timestamp": "1234567890",
                          "type": "text",
                          "text": {"body": "Olá"}
                        }]
                      }
                    }]
                  }]
                }
                """;

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }
}