package com.aurum.service;

import com.aurum.dto.PortfolioRequest;
import com.aurum.dto.PortfolioResponse;
import com.aurum.exception.PortfolioNotFoundException;
import com.aurum.model.Portfolio;
import com.aurum.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    private PortfolioService portfolioService;

    @BeforeEach
    void setUp() {
        portfolioService = new PortfolioService(portfolioRepository);
    }

    @Test
    void createPortfolio_shouldSaveAndReturnPortfolio() {
        PortfolioRequest request = new PortfolioRequest();
        request.setName("Long-Term Investments");

        when(portfolioRepository.save(any(Portfolio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PortfolioResponse response = portfolioService.createPortfolio(request);

        assertEquals("Long-Term Investments", response.getName());

        ArgumentCaptor<Portfolio> portfolioCaptor =
                ArgumentCaptor.forClass(Portfolio.class);

        verify(portfolioRepository).save(portfolioCaptor.capture());

        assertEquals(
                "Long-Term Investments",
                portfolioCaptor.getValue().getName()
        );
    }

    @Test
    void getPortfolioById_shouldReturnPortfolioWhenItExists() {
        Portfolio portfolio = new Portfolio("Growth Portfolio");

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        PortfolioResponse response = portfolioService.getPortfolioById(1L);

        assertEquals("Growth Portfolio", response.getName());

        verify(portfolioRepository).findById(1L);
    }

    @Test
    void getAllPortfolios_shouldReturnAllPortfolios() {
        Portfolio first = new Portfolio("Growth");
        Portfolio second = new Portfolio("Income");

        when(portfolioRepository.findAll())
                .thenReturn(List.of(first, second));

        List<PortfolioResponse> responses =
                portfolioService.getAllPortfolios();

        assertEquals(2, responses.size());
        assertEquals("Growth", responses.get(0).getName());
        assertEquals("Income", responses.get(1).getName());

        verify(portfolioRepository).findAll();
    }

    @Test
    void updatePortfolio_shouldUpdateNameAndSavePortfolio() {
        Portfolio portfolio = new Portfolio("Old Name");

        PortfolioRequest request = new PortfolioRequest();
        request.setName("New Name");

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        when(portfolioRepository.save(portfolio))
                .thenReturn(portfolio);

        PortfolioResponse response =
                portfolioService.updatePortfolio(1L, request);

        assertEquals("New Name", response.getName());
        assertEquals("New Name", portfolio.getName());

        verify(portfolioRepository).save(portfolio);
    }

    @Test
    void deletePortfolio_shouldDeletePortfolioWhenItExists() {
        Portfolio portfolio = new Portfolio("Delete Me");

        when(portfolioRepository.findById(1L))
                .thenReturn(Optional.of(portfolio));

        portfolioService.deletePortfolio(1L);

        verify(portfolioRepository).delete(portfolio);
    }

    @Test
    void getPortfolioById_shouldThrowExceptionWhenPortfolioDoesNotExist() {
        when(portfolioRepository.findById(999L))
                .thenReturn(Optional.empty());

        PortfolioNotFoundException exception =
                assertThrows(
                        PortfolioNotFoundException.class,
                        () -> portfolioService.getPortfolioById(999L)
                );

        assertEquals(
                "Portfolio not found with id: 999",
                exception.getMessage()
        );

        verify(portfolioRepository).findById(999L);
    }
}