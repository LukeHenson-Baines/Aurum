package com.aurum.controller;

import com.aurum.dto.PortfolioResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.service.PortfolioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioService portfolioService;

    @Test
    void createPortfolio_shouldReturn201Created() throws Exception {
        PortfolioResponse response = new PortfolioResponse(
                1L,
                "Long-Term Investments",
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );

        when(portfolioService.createPortfolio(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/portfolios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Long-Term Investments"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Long-Term Investments"));
    }

    @Test
    void getAllPortfolios_shouldReturn200AndPortfolioList() throws Exception {
        PortfolioResponse response = new PortfolioResponse(
                1L,
                "Growth Portfolio",
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );

        when(portfolioService.getAllPortfolios())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Growth Portfolio"));
    }

    @Test
    void getPortfolioById_shouldReturn200WhenPortfolioExists() throws Exception {
        PortfolioResponse response = new PortfolioResponse(
                1L,
                "Growth Portfolio",
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );

        when(portfolioService.getPortfolioById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/portfolios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Growth Portfolio"));
    }

    @Test
    void updatePortfolio_shouldReturn200AndUpdatedPortfolio() throws Exception {
        PortfolioResponse response = new PortfolioResponse(
                1L,
                "Updated Portfolio",
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );

        when(portfolioService.updatePortfolio(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/portfolios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Portfolio"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Portfolio"));
    }

    @Test
    void deletePortfolio_shouldReturn204NoContent() throws Exception {
        doNothing().when(portfolioService).deletePortfolio(1L);

        mockMvc.perform(delete("/api/portfolios/1"))
                .andExpect(status().isNoContent());

        verify(portfolioService).deletePortfolio(1L);
    }

    @Test
    void getPortfolioById_shouldReturn404WhenPortfolioDoesNotExist() throws Exception {
        when(portfolioService.getPortfolioById(999L))
                .thenThrow(new PortfolioNotFoundException(999L));

        mockMvc.perform(get("/api/portfolios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Portfolio not found with id: 999"));
    }

    @Test
    void createPortfolio_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": ""
                        }
                        """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.name")
                .value("Portfolio name is required"));

        verifyNoInteractions(portfolioService);
    }
}