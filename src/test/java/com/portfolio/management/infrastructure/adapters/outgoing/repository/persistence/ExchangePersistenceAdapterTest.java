package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Exchange;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseExchangeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.ExchangeEntity;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangePersistenceAdapterTest {

    @Mock
    private DatabaseExchangeRepository mockRepository;

    @Mock
    private CategoryMapper mockMapper;

    private ExchangePersistenceAdapter adapter;
    private List<ExchangeEntity> mockEntities;
    private List<Exchange> mockDomainObjects;

    @BeforeEach
    void setUp() {
        adapter = new ExchangePersistenceAdapter(mockRepository, mockMapper);
        
        mockEntities = List.of(
            createExchangeEntity(1L, "NYSE", "New York Stock Exchange", "United States", "EST", "USD"),
            createExchangeEntity(2L, "NASDAQ", "NASDAQ", "United States", "EST", "USD"),
            createExchangeEntity(3L, "LSE", "London Stock Exchange", "United Kingdom", "GMT", "GBP")
        );
        
        mockDomainObjects = List.of(
            Exchange.of(1L, "NYSE", "New York Stock Exchange", "United States", "EST", "USD", true),
            Exchange.of(2L, "NASDAQ", "NASDAQ", "United States", "EST", "USD", true),
            Exchange.of(3L, "LSE", "London Stock Exchange", "United Kingdom", "GMT", "GBP", true)
        );
    }

    @Test
    @DisplayName("Should find all active exchanges")
    void testFindAll_Success() {
        // Given
        when(mockRepository.findAllActive()).thenReturn(Uni.createFrom().item(mockEntities));
        when(mockMapper.toExchange(any(ExchangeEntity.class)))
            .thenReturn(mockDomainObjects.get(0))
            .thenReturn(mockDomainObjects.get(1))
            .thenReturn(mockDomainObjects.get(2));

        // When
        List<Exchange> result = adapter.findAll()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("NYSE", result.get(0).code());
        assertEquals("NASDAQ", result.get(1).code());
        assertEquals("LSE", result.get(2).code());
        
        verify(mockRepository).findAllActive();
        verify(mockMapper, times(3)).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should search exchanges by query")
    void testSearch_Success() {
        // Given
        String query = "NASDAQ";
        List<ExchangeEntity> filteredEntities = List.of(mockEntities.get(1));
        
        when(mockRepository.searchActive(query)).thenReturn(Uni.createFrom().item(filteredEntities));
        when(mockMapper.toExchange(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Exchange> result = adapter.search(query)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NASDAQ", result.get(0).code());
        assertTrue(result.get(0).isUSExchange());
        
        verify(mockRepository).searchActive(query);
        verify(mockMapper).toExchange(mockEntities.get(1));
    }

    @Test
    @DisplayName("Should find exchange by code")
    void testFindByCode_Found() {
        // Given
        String code = "LSE";
        ExchangeEntity entity = mockEntities.get(2);
        Exchange domainObject = mockDomainObjects.get(2);
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toExchange(entity)).thenReturn(domainObject);

        // When
        Optional<Exchange> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("LSE", result.get().code());
        assertEquals("London Stock Exchange", result.get().name());
        assertEquals("Europe", result.get().getRegion());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper).toExchange(entity);
    }

    @Test
    @DisplayName("Should find exchange by ID")
    void testFindById_Found() {
        // Given
        Long id = 1L;
        ExchangeEntity entity = mockEntities.get(0);
        Exchange domainObject = mockDomainObjects.get(0);
        
        when(mockRepository.findActiveById(id)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toExchange(entity)).thenReturn(domainObject);

        // When
        Optional<Exchange> result = adapter.findById(id)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("NYSE", result.get().code());
        assertEquals(Long.valueOf(1L), result.get().id());
        
        verify(mockRepository).findActiveById(id);
        verify(mockMapper).toExchange(entity);
    }

    @Test
    @DisplayName("Should find exchanges by country")
    void testFindByCountry_Success() {
        // Given
        String country = "United States";
        List<ExchangeEntity> usEntities = mockEntities.subList(0, 2); // NYSE, NASDAQ
        
        when(mockRepository.findActiveByCountry(country)).thenReturn(Uni.createFrom().item(usEntities));
        when(mockMapper.toExchange(mockEntities.get(0))).thenReturn(mockDomainObjects.get(0));
        when(mockMapper.toExchange(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Exchange> result = adapter.findByCountry(country)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("NYSE", result.get(0).code());
        assertEquals("NASDAQ", result.get(1).code());
        assertTrue(result.get(0).isUSExchange());
        assertTrue(result.get(1).isUSExchange());
        
        verify(mockRepository).findActiveByCountry(country);
        verify(mockMapper, times(2)).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should find exchanges by currency code")
    void testFindByCurrencyCode_Success() {
        // Given
        String currencyCode = "USD";
        List<ExchangeEntity> usdEntities = mockEntities.subList(0, 2); // NYSE, NASDAQ
        
        when(mockRepository.findActiveByCurrencyCode(currencyCode)).thenReturn(Uni.createFrom().item(usdEntities));
        when(mockMapper.toExchange(mockEntities.get(0))).thenReturn(mockDomainObjects.get(0));
        when(mockMapper.toExchange(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Exchange> result = adapter.findByCurrencyCode(currencyCode)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("NYSE", result.get(0).code());
        assertEquals("NASDAQ", result.get(1).code());
        assertEquals("USD", result.get(0).currencyCode());
        assertEquals("USD", result.get(1).currencyCode());
        
        verify(mockRepository).findActiveByCurrencyCode(currencyCode);
        verify(mockMapper, times(2)).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should find US exchanges")
    void testFindUSExchanges_Success() {
        // Given
        List<ExchangeEntity> usEntities = mockEntities.subList(0, 2); // NYSE, NASDAQ
        
        when(mockRepository.findUSExchanges()).thenReturn(Uni.createFrom().item(usEntities));
        when(mockMapper.toExchange(mockEntities.get(0))).thenReturn(mockDomainObjects.get(0));
        when(mockMapper.toExchange(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Exchange> result = adapter.findUSExchanges()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isUSExchange());
        assertTrue(result.get(1).isUSExchange());
        assertEquals("North America", result.get(0).getRegion());
        assertEquals("North America", result.get(1).getRegion());
        
        verify(mockRepository).findUSExchanges();
        verify(mockMapper, times(2)).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should find European exchanges")
    void testFindEuropeanExchanges_Success() {
        // Given
        List<ExchangeEntity> europeanEntities = List.of(mockEntities.get(2)); // LSE
        
        when(mockRepository.findEuropeanExchanges()).thenReturn(Uni.createFrom().item(europeanEntities));
        when(mockMapper.toExchange(mockEntities.get(2))).thenReturn(mockDomainObjects.get(2));

        // When
        List<Exchange> result = adapter.findEuropeanExchanges()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LSE", result.get(0).code());
        assertTrue(result.get(0).isEuropeanExchange());
        
        verify(mockRepository).findEuropeanExchanges();
        verify(mockMapper).toExchange(mockEntities.get(2));
    }

    @Test
    @DisplayName("Should find Asian exchanges")
    void testFindAsianExchanges_Success() {
        // Given
        List<ExchangeEntity> asianEntities = List.of(); // Empty for this test
        
        when(mockRepository.findAsianExchanges()).thenReturn(Uni.createFrom().item(asianEntities));

        // When
        List<Exchange> result = adapter.findAsianExchanges()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(mockRepository).findAsianExchanges();
        verify(mockMapper, never()).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should return empty optional when exchange not found")
    void testFindByCode_NotFound() {
        // Given
        String code = "UNKNOWN";
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        Optional<Exchange> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertFalse(result.isPresent());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper, never()).toExchange(any(ExchangeEntity.class));
    }

    @Test
    @DisplayName("Should count active exchanges")
    void testCount_Success() {
        // Given
        long expectedCount = 3L;
        
        when(mockRepository.countActive()).thenReturn(Uni.createFrom().item(expectedCount));

        // When
        Long result = adapter.count()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertEquals(expectedCount, result);
        
        verify(mockRepository).countActive();
    }

    @Test
    @DisplayName("Should handle repository failure")
    void testFindAll_RepositoryFailure() {
        // Given
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(mockRepository.findAllActive()).thenReturn(Uni.createFrom().failure(repositoryException));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            adapter.findAll().await().atMost(Duration.ofSeconds(5))
        );
        
        verify(mockRepository).findAllActive();
        verify(mockMapper, never()).toExchange(any(ExchangeEntity.class));
    }

    private ExchangeEntity createExchangeEntity(Long id, String code, String name, String country, String timezone, String currencyCode) {
        ExchangeEntity entity = new ExchangeEntity(code, name, country, timezone, currencyCode);
        entity.id = id;
        return entity;
    }
}
