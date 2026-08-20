package com.aurum.service;

import com.aurum.dto.AssetRequest;
import com.aurum.dto.AssetResponse;
import com.aurum.exception.AssetNotFoundException;
import com.aurum.exception.DuplicateAssetSymbolException;
import com.aurum.model.Asset;
import com.aurum.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository);
    }

    @Test
    void createAsset_shouldNormaliseSymbolAndSaveAsset() {
        AssetRequest request = new AssetRequest();
        request.setSymbol(" aapl ");
        request.setName("Apple Inc.");
        request.setCurrentPrice(new BigDecimal("225.50"));

        when(assetRepository.existsBySymbolIgnoreCase("AAPL"))
                .thenReturn(false);

        when(assetRepository.save(any(Asset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponse response = assetService.createAsset(request);

        assertEquals("AAPL", response.getSymbol());

        ArgumentCaptor<Asset> captor =
                ArgumentCaptor.forClass(Asset.class);

        verify(assetRepository).save(captor.capture());

        assertEquals("AAPL", captor.getValue().getSymbol());
        assertEquals("Apple Inc.", captor.getValue().getName());
    }

    @Test
    void createAsset_shouldThrowWhenSymbolAlreadyExists() {
        AssetRequest request = new AssetRequest();
        request.setSymbol("aapl");
        request.setName("Apple Inc.");
        request.setCurrentPrice(new BigDecimal("225.50"));

        when(assetRepository.existsBySymbolIgnoreCase("AAPL"))
                .thenReturn(true);

        assertThrows(
                DuplicateAssetSymbolException.class,
                () -> assetService.createAsset(request)
        );

        verify(assetRepository, never()).save(any());
    }

    @Test
    void getAssetById_shouldReturnAssetWhenItExists() {
        Asset asset = new Asset(
                "MSFT",
                "Microsoft",
                new BigDecimal("420.00")
        );

        when(assetRepository.findById(1L))
                .thenReturn(Optional.of(asset));

        AssetResponse response = assetService.getAssetById(1L);

        assertEquals("MSFT", response.getSymbol());
        assertEquals("Microsoft", response.getName());
    }

    @Test
    void getAssetById_shouldThrowWhenAssetDoesNotExist() {
        when(assetRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AssetNotFoundException.class,
                () -> assetService.getAssetById(999L)
        );
    }

    @Test
    void getAllAssets_shouldReturnAllAssets() {
        Asset apple = new Asset(
                "AAPL",
                "Apple Inc.",
                new BigDecimal("225.50")
        );

        Asset microsoft = new Asset(
                "MSFT",
                "Microsoft",
                new BigDecimal("420.00")
        );

        when(assetRepository.findAll())
                .thenReturn(List.of(apple, microsoft));

        List<AssetResponse> responses = assetService.getAllAssets();

        assertEquals(2, responses.size());
        assertEquals("AAPL", responses.get(0).getSymbol());
        assertEquals("MSFT", responses.get(1).getSymbol());
    }
}