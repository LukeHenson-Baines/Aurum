package com.aurum.service;

import com.aurum.dto.AssetRequest;
import com.aurum.dto.AssetResponse;
import com.aurum.exception.AssetNotFoundException;
import com.aurum.exception.DuplicateAssetSymbolException;
import com.aurum.model.Asset;
import com.aurum.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public AssetResponse createAsset(AssetRequest request) {
        String normalisedSymbol = request.getSymbol().trim().toUpperCase();

        if (assetRepository.existsBySymbolIgnoreCase(normalisedSymbol)) {
            throw new DuplicateAssetSymbolException(normalisedSymbol);
        }

        Asset asset = new Asset(
                normalisedSymbol,
                request.getName().trim(),
                request.getCurrentPrice()
        );

        Asset savedAsset = assetRepository.save(asset);

        return toResponse(savedAsset);
    }

    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        return toResponse(asset);
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getSymbol(),
                asset.getName(),
                asset.getCurrentPrice()
        );
    }
}