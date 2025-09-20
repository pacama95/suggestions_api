package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.Currency;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseCurrencyRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.CurrencyEntity;
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
class CurrencyPersistenceAdapterTest {

    @Mock
    private DatabaseCurrencyRepository mockRepository;

    @Mock
    private CategoryMapper mockMapper;

    private CurrencyPersistenceAdapter adapter;
    private List<CurrencyEntity> mockEntities;
    private List<Currency> mockDomainObjects;

    @BeforeEach
    void setUp() {
        adapter = new CurrencyPersistenceAdapter(mockRepository, mockMapper);
        
        mockEntities = List.of(
            createCurrencyEntity(1L, "USD", "US Dollar", "$", "US"),
            createCurrencyEntity(2L, "EUR", "Euro", "€", "EU"),
            createCurrencyEntity(3L, "JPY", "Japanese Yen", "¥", "JP")
        );
        
        mockDomainObjects = List.of(
            Currency.of(1L, "USD", "US Dollar", "$", "US", true),
            Currency.of(2L, "EUR", "Euro", "€", "EU", true),
            Currency.of(3L, "JPY", "Japanese Yen", "¥", "JP", true)
        );
    }

    @Test
    @DisplayName("Should find all active currencies")
    void testFindAll_Success() {
        // Given
        when(mockRepository.findAllActive()).thenReturn(Uni.createFrom().item(mockEntities));
        when(mockMapper.toCurrency(any(CurrencyEntity.class)))
            .thenReturn(mockDomainObjects.get(0))
            .thenReturn(mockDomainObjects.get(1))
            .thenReturn(mockDomainObjects.get(2));

        // When
        List<Currency> result = adapter.findAll()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("USD", result.get(0).code());
        assertEquals("EUR", result.get(1).code());
        assertEquals("JPY", result.get(2).code());
        
        verify(mockRepository).findAllActive();
        verify(mockMapper, times(3)).toCurrency(any(CurrencyEntity.class));
    }

    @Test
    @DisplayName("Should search currencies by query")
    void testSearch_Success() {
        // Given
        String query = "Euro";
        List<CurrencyEntity> filteredEntities = List.of(mockEntities.get(1));
        
        when(mockRepository.searchActive(query)).thenReturn(Uni.createFrom().item(filteredEntities));
        when(mockMapper.toCurrency(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Currency> result = adapter.search(query)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EUR", result.get(0).code());
        assertEquals("€", result.get(0).symbol());
        
        verify(mockRepository).searchActive(query);
        verify(mockMapper).toCurrency(mockEntities.get(1));
    }

    @Test
    @DisplayName("Should find currency by code")
    void testFindByCode_Found() {
        // Given
        String code = "USD";
        CurrencyEntity entity = mockEntities.get(0);
        Currency domainObject = mockDomainObjects.get(0);
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toCurrency(entity)).thenReturn(domainObject);

        // When
        Optional<Currency> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("USD", result.get().code());
        assertEquals("$", result.get().symbol());
        assertTrue(result.get().isUSD());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper).toCurrency(entity);
    }

    @Test
    @DisplayName("Should find currency by ID")
    void testFindById_Found() {
        // Given
        Long id = 2L;
        CurrencyEntity entity = mockEntities.get(1);
        Currency domainObject = mockDomainObjects.get(1);
        
        when(mockRepository.findActiveById(id)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toCurrency(entity)).thenReturn(domainObject);

        // When
        Optional<Currency> result = adapter.findById(id)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("EUR", result.get().code());
        assertTrue(result.get().isEUR());
        
        verify(mockRepository).findActiveById(id);
        verify(mockMapper).toCurrency(entity);
    }

    @Test
    @DisplayName("Should find currencies by country code")
    void testFindByCountryCode_Success() {
        // Given
        String countryCode = "US";
        List<CurrencyEntity> filteredEntities = List.of(mockEntities.get(0));
        
        when(mockRepository.findActiveByCountryCode(countryCode)).thenReturn(Uni.createFrom().item(filteredEntities));
        when(mockMapper.toCurrency(mockEntities.get(0))).thenReturn(mockDomainObjects.get(0));

        // When
        List<Currency> result = adapter.findByCountryCode(countryCode)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).code());
        assertEquals("US", result.get(0).countryCode());
        
        verify(mockRepository).findActiveByCountryCode(countryCode);
        verify(mockMapper).toCurrency(mockEntities.get(0));
    }

    @Test
    @DisplayName("Should find major currencies")
    void testFindMajorCurrencies_Success() {
        // Given
        List<CurrencyEntity> majorEntities = mockEntities.subList(0, 2); // USD, EUR
        
        when(mockRepository.findMajorCurrencies()).thenReturn(Uni.createFrom().item(majorEntities));
        when(mockMapper.toCurrency(mockEntities.get(0))).thenReturn(mockDomainObjects.get(0));
        when(mockMapper.toCurrency(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<Currency> result = adapter.findMajorCurrencies()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("USD", result.get(0).code());
        assertEquals("EUR", result.get(1).code());
        assertTrue(result.get(0).isMajorCurrency());
        assertTrue(result.get(1).isMajorCurrency());
        
        verify(mockRepository).findMajorCurrencies();
        verify(mockMapper, times(2)).toCurrency(any(CurrencyEntity.class));
    }

    @Test
    @DisplayName("Should return empty optional when currency not found")
    void testFindByCode_NotFound() {
        // Given
        String code = "UNKNOWN";
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        Optional<Currency> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertFalse(result.isPresent());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper, never()).toCurrency(any(CurrencyEntity.class));
    }

    @Test
    @DisplayName("Should count active currencies")
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
    void testSearch_RepositoryFailure() {
        // Given
        String query = "USD";
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(mockRepository.searchActive(query)).thenReturn(Uni.createFrom().failure(repositoryException));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            adapter.search(query).await().atMost(Duration.ofSeconds(5))
        );
        
        verify(mockRepository).searchActive(query);
        verify(mockMapper, never()).toCurrency(any(CurrencyEntity.class));
    }

    private CurrencyEntity createCurrencyEntity(Long id, String code, String name, String symbol, String countryCode) {
        CurrencyEntity entity = new CurrencyEntity(code, name, symbol, countryCode);
        entity.id = id;
        return entity;
    }
}
