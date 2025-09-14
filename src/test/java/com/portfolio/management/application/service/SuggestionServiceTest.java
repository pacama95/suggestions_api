package com.portfolio.management.application.service;

import com.portfolio.management.domain.model.TickerSuggestion;
import com.portfolio.management.domain.port.incoming.GetSuggestionsUseCase;
import com.portfolio.management.domain.port.outgoing.SuggestionRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SuggestionServiceTest {

    @Inject
    SuggestionService suggestionService;

    @InjectMock
    SuggestionRepository suggestionRepository;

    @Test
    public void testExecute_ValidQuery_Success() {
        // Given
        var query = new GetSuggestionsUseCase.Query("apple", 5);
        var suggestions = List.of(
            new TickerSuggestion("AAPL", "Apple Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD")
        );
        
        Mockito.when(suggestionRepository.findByQuery("apple", 5))
            .thenReturn(Uni.createFrom().item(suggestions));

        // When & Then
        var result = suggestionService.execute(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .getItem();
        
        assertTrue(result instanceof GetSuggestionsUseCase.Result.Success);
        var success = (GetSuggestionsUseCase.Result.Success) result;
        assertEquals("apple", success.query());
        assertEquals(1, success.count());
        assertEquals(1, success.suggestions().size());
        assertEquals("AAPL", success.suggestions().get(0).symbol());
    }

    @Test
    public void testExecute_EmptyQuery_ValidationError() {
        // Given
        var query = new GetSuggestionsUseCase.Query(" ", 5);

        // When & Then
        var result = suggestionService.execute(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .getItem();
            
        assertTrue(result instanceof GetSuggestionsUseCase.Result.ValidationError);
        var validationError = (GetSuggestionsUseCase.Result.ValidationError) result;
        assertFalse(validationError.errors().isEmpty());
        assertEquals("input", validationError.errors().errors().get(0).field());
    }

    @Test
    public void testExecute_RepositoryError_SystemError() {
        // Given
        var query = new GetSuggestionsUseCase.Query("apple", 5);
        
        Mockito.when(suggestionRepository.findByQuery("apple", 5))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        // When & Then
        var result = suggestionService.execute(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .getItem();
            
        assertTrue(result instanceof GetSuggestionsUseCase.Result.SystemError);
        var systemError = (GetSuggestionsUseCase.Result.SystemError) result;
        assertFalse(systemError.errors().isEmpty());
        assertEquals("repository", systemError.errors().errors().get(0).field());
    }

    @Test
    public void testExecute_NoResults_EmptySuccess() {
        // Given
        var query = new GetSuggestionsUseCase.Query("nonexistent", 5);
        
        Mockito.when(suggestionRepository.findByQuery("nonexistent", 5))
            .thenReturn(Uni.createFrom().item(List.of()));

        // When & Then
        var result = suggestionService.execute(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
            .getItem();
            
        assertTrue(result instanceof GetSuggestionsUseCase.Result.Success);
        var success = (GetSuggestionsUseCase.Result.Success) result;
        assertEquals("nonexistent", success.query());
        assertEquals(0, success.count());
        assertTrue(success.suggestions().isEmpty());
    }
}
