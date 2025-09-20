package com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence;

import com.portfolio.management.domain.model.StockType;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.DatabaseStockTypeRepository;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.mapper.CategoryMapper;
import com.portfolio.management.infrastructure.adapters.outgoing.repository.persistence.entity.StockTypeEntity;
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
class StockTypePersistenceAdapterTest {

    @Mock
    private DatabaseStockTypeRepository mockRepository;

    @Mock
    private CategoryMapper mockMapper;

    private StockTypePersistenceAdapter adapter;
    private List<StockTypeEntity> mockEntities;
    private List<StockType> mockDomainObjects;

    @BeforeEach
    void setUp() {
        adapter = new StockTypePersistenceAdapter(mockRepository, mockMapper);
        
        mockEntities = List.of(
            createStockTypeEntity(1L, "COMMON_STOCK", "Common Stock", "Regular company shares"),
            createStockTypeEntity(2L, "ETF", "ETF", "Exchange-Traded Fund"),
            createStockTypeEntity(3L, "REIT", "REIT", "Real Estate Investment Trust")
        );
        
        mockDomainObjects = List.of(
            StockType.of(1L, "COMMON_STOCK", "Common Stock", "Regular company shares", true),
            StockType.of(2L, "ETF", "ETF", "Exchange-Traded Fund", true),
            StockType.of(3L, "REIT", "REIT", "Real Estate Investment Trust", true)
        );
    }

    @Test
    @DisplayName("Should find all active stock types")
    void testFindAll_Success() {
        // Given
        when(mockRepository.findAllActive()).thenReturn(Uni.createFrom().item(mockEntities));
        when(mockMapper.toStockType(any(StockTypeEntity.class)))
            .thenReturn(mockDomainObjects.get(0))
            .thenReturn(mockDomainObjects.get(1))
            .thenReturn(mockDomainObjects.get(2));

        // When
        List<StockType> result = adapter.findAll()
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("COMMON_STOCK", result.get(0).code());
        assertEquals("ETF", result.get(1).code());
        assertEquals("REIT", result.get(2).code());
        
        verify(mockRepository).findAllActive();
        verify(mockMapper, times(3)).toStockType(any(StockTypeEntity.class));
    }

    @Test
    @DisplayName("Should search stock types by query")
    void testSearch_Success() {
        // Given
        String query = "ETF";
        List<StockTypeEntity> filteredEntities = List.of(mockEntities.get(1));
        List<StockType> filteredDomainObjects = List.of(mockDomainObjects.get(1));
        
        when(mockRepository.searchActive(query)).thenReturn(Uni.createFrom().item(filteredEntities));
        when(mockMapper.toStockType(mockEntities.get(1))).thenReturn(mockDomainObjects.get(1));

        // When
        List<StockType> result = adapter.search(query)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ETF", result.get(0).code());
        
        verify(mockRepository).searchActive(query);
        verify(mockMapper).toStockType(mockEntities.get(1));
    }

    @Test
    @DisplayName("Should find stock type by code")
    void testFindByCode_Found() {
        // Given
        String code = "REIT";
        StockTypeEntity entity = mockEntities.get(2);
        StockType domainObject = mockDomainObjects.get(2);
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toStockType(entity)).thenReturn(domainObject);

        // When
        Optional<StockType> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("REIT", result.get().code());
        assertEquals("Real Estate Investment Trust", result.get().description());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper).toStockType(entity);
    }

    @Test
    @DisplayName("Should return empty optional when stock type not found by code")
    void testFindByCode_NotFound() {
        // Given
        String code = "UNKNOWN";
        
        when(mockRepository.findActiveByCode(code)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        Optional<StockType> result = adapter.findByCode(code)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertFalse(result.isPresent());
        
        verify(mockRepository).findActiveByCode(code);
        verify(mockMapper, never()).toStockType(any(StockTypeEntity.class));
    }

    @Test
    @DisplayName("Should find stock type by ID")
    void testFindById_Found() {
        // Given
        Long id = 2L;
        StockTypeEntity entity = mockEntities.get(1);
        StockType domainObject = mockDomainObjects.get(1);
        
        when(mockRepository.findActiveById(id)).thenReturn(Uni.createFrom().item(Optional.of(entity)));
        when(mockMapper.toStockType(entity)).thenReturn(domainObject);

        // When
        Optional<StockType> result = adapter.findById(id)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertTrue(result.isPresent());
        assertEquals("ETF", result.get().code());
        assertEquals(Long.valueOf(2L), result.get().id());
        
        verify(mockRepository).findActiveById(id);
        verify(mockMapper).toStockType(entity);
    }

    @Test
    @DisplayName("Should return empty optional when stock type not found by ID")
    void testFindById_NotFound() {
        // Given
        Long id = 999L;
        
        when(mockRepository.findActiveById(id)).thenReturn(Uni.createFrom().item(Optional.empty()));

        // When
        Optional<StockType> result = adapter.findById(id)
            .await().atMost(Duration.ofSeconds(5));

        // Then
        assertFalse(result.isPresent());
        
        verify(mockRepository).findActiveById(id);
        verify(mockMapper, never()).toStockType(any(StockTypeEntity.class));
    }

    @Test
    @DisplayName("Should count active stock types")
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
        verify(mockMapper, never()).toStockType(any(StockTypeEntity.class));
    }

    private StockTypeEntity createStockTypeEntity(Long id, String code, String name, String description) {
        StockTypeEntity entity = new StockTypeEntity(code, name, description);
        entity.id = id;
        return entity;
    }
}
