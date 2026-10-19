package io.github.teggr.teavmshowcase.currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ConversionApiTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ConversionController(new ConversionService()))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsTheDocumentedResponseForAValidRequest() throws Exception {
        mockMvc.perform(post("/api/conversions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100,"from":"GBP","to":"EUR"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.from").value("GBP"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.rate").value(1.17))
                .andExpect(jsonPath("$.result").value(117.00))
                .andExpect(jsonPath("$.asOf").value("2026-10-18"))
                .andExpect(jsonPath("$.history.length()").value(7))
                .andExpect(jsonPath("$.history[0].date").value("2026-10-12"))
                .andExpect(jsonPath("$.history[0].rate").value(1.15245))
                .andExpect(jsonPath("$.history[6].date").value("2026-10-18"))
                .andExpect(jsonPath("$.history[6].rate").value(1.17));
    }

    @Test
    void returnsStableErrorsForInvalidRequests() throws Exception {
        expectError("{\"amount\":0,\"from\":\"GBP\",\"to\":\"EUR\"}", "INVALID_AMOUNT");
        expectError("{\"amount\":-1,\"from\":\"GBP\",\"to\":\"EUR\"}", "INVALID_AMOUNT");
        expectError("{\"amount\":1000000.01,\"from\":\"GBP\",\"to\":\"EUR\"}", "AMOUNT_TOO_LARGE");
        expectError("{\"amount\":1.001,\"from\":\"GBP\",\"to\":\"EUR\"}", "INVALID_PRECISION");
        expectError("{\"amount\":1,\"from\":\"CAD\",\"to\":\"EUR\"}", "UNSUPPORTED_CURRENCY");
        expectError("{\"amount\":1,\"from\":\"GBP\",\"to\":\"CAD\"}", "UNSUPPORTED_CURRENCY");
        expectError("{\"amount\":1,\"to\":\"EUR\"}", "UNSUPPORTED_CURRENCY");
        expectError("{\"from\":\"GBP\",\"to\":\"EUR\"}", "INVALID_AMOUNT");
        expectError("{\"amount\":\"10\",\"from\":\"GBP\",\"to\":\"EUR\"}", "INVALID_AMOUNT");
        expectError("null", "MALFORMED_JSON");
    }

    @Test
    void handlesMalformedJsonWithoutReturningAnExceptionDetail() throws Exception {
        mockMvc.perform(post("/api/conversions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.message").value("Request body must be valid conversion JSON."));
    }

    private void expectError(String body, String code) throws Exception {
        mockMvc.perform(post("/api/conversions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
