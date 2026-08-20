package com.aurum.controller;

import com.aurum.dto.AssetResponse;
import com.aurum.exception.AssetNotFoundException;
import com.aurum.exception.DuplicateAssetSymbolException;
import com.aurum.service.AssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @Test
    void createAsset_shouldReturn201() throws Exception {
        AssetResponse response = new AssetResponse(
                1L,
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        when(assetService.createAsset(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "name": "Apple Inc.",
                                  "currentPrice": 225.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.name").value("Apple Inc."));
    }

    @Test
    void getAllAssets_shouldReturn200() throws Exception {
        AssetResponse response = new AssetResponse(
                1L,
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        when(assetService.getAllAssets())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }

    @Test
    void getAssetById_shouldReturn404WhenMissing() throws Exception {
        when(assetService.getAssetById(999L))
                .thenThrow(new AssetNotFoundException(999L));

        mockMvc.perform(get("/api/assets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Asset not found with id: 999"));
    }

    @Test
    void createAsset_shouldReturn409ForDuplicateSymbol() throws Exception {
        when(assetService.createAsset(any()))
                .thenThrow(new DuplicateAssetSymbolException("AAPL"));

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "name": "Apple Inc.",
                                  "currentPrice": 225.50
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createAsset_shouldReturn400ForInvalidPrice() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "AAPL",
                                  "name": "Apple Inc.",
                                  "currentPrice": -10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currentPrice")
                        .value("Current price must be greater than zero"));

        verifyNoInteractions(assetService);
    }
}