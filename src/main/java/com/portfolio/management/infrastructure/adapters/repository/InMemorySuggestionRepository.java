package com.portfolio.management.infrastructure.adapters.repository;

import com.portfolio.management.domain.model.TickerSuggestion;
import com.portfolio.management.domain.port.outgoing.SuggestionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * In-memory implementation of SuggestionRepository for development and testing
 */
@ApplicationScoped
public class InMemorySuggestionRepository implements SuggestionRepository {
    
    private static final Logger LOG = Logger.getLogger(InMemorySuggestionRepository.class);
    
    private List<TickerSuggestion> tickerDatabase;
    
    @PostConstruct
    public void init() {
        LOG.info("Initializing in-memory ticker suggestion database");
        initializeSampleData();
        LOG.infof("Loaded %d ticker suggestions", tickerDatabase.size());
    }
    
    @Override
    public Uni<List<TickerSuggestion>> findByQuery(String query, int limit) {
        return Uni.createFrom().completionStage(
            CompletableFuture.supplyAsync(() -> {
                LOG.debugf("Searching for tickers with query: %s, limit: %d", query, limit);
                
                var results = tickerDatabase.stream()
                    .filter(ticker -> ticker.matches(query))
                    .limit(Math.max(1, Math.min(limit, 50)))
                    .toList();
                
                LOG.debugf("Found %d matching tickers", results.size());
                return results;
            })
        );
    }
    
    @Override
    public Uni<Optional<TickerSuggestion>> findBySymbol(String symbol) {
        return Uni.createFrom().completionStage(
            CompletableFuture.supplyAsync(() -> {
                LOG.debugf("Searching for ticker with symbol: %s", symbol);
                
                var result = tickerDatabase.stream()
                    .filter(ticker -> ticker.symbol().equalsIgnoreCase(symbol.trim()))
                    .findFirst();
                
                LOG.debugf("Ticker %s: %s", symbol, result.isPresent() ? "found" : "not found");
                return result;
            })
        );
    }
    
    @Override
    public Uni<List<TickerSuggestion>> findAll(int offset, int limit) {
        return Uni.createFrom().completionStage(
            CompletableFuture.supplyAsync(() -> {
                LOG.debugf("Getting all tickers with offset: %d, limit: %d", offset, limit);
                
                var results = tickerDatabase.stream()
                    .skip(Math.max(0, offset))
                    .limit(Math.max(1, Math.min(limit, 100)))
                    .toList();
                
                LOG.debugf("Returning %d tickers", results.size());
                return results;
            })
        );
    }
    
    /**
     * Initialize sample ticker data for development and testing
     */
    private void initializeSampleData() {
        tickerDatabase = new ArrayList<>();
        
        // Tech stocks
        tickerDatabase.add(new TickerSuggestion("AAPL", "Apple Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("MSFT", "Microsoft Corporation", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("GOOGL", "Alphabet Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("AMZN", "Amazon.com Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("TSLA", "Tesla Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("META", "Meta Platforms Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("NFLX", "Netflix Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("NVDA", "NVIDIA Corporation", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        
        // Financial stocks
        tickerDatabase.add(new TickerSuggestion("JPM", "JPMorgan Chase & Co.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("BAC", "Bank of America Corporation", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("WFC", "Wells Fargo & Company", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("GS", "Goldman Sachs Group Inc.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        
        // Healthcare stocks
        tickerDatabase.add(new TickerSuggestion("JNJ", "Johnson & Johnson", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("PFE", "Pfizer Inc.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("UNH", "UnitedHealth Group Incorporated", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("MRNA", "Moderna Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        
        // Consumer goods
        tickerDatabase.add(new TickerSuggestion("KO", "The Coca-Cola Company", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("PEP", "PepsiCo Inc.", "NASDAQ", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("WMT", "Walmart Inc.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("HD", "The Home Depot Inc.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        
        // Energy
        tickerDatabase.add(new TickerSuggestion("XOM", "Exxon Mobil Corporation", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("CVX", "Chevron Corporation", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        
        // Some smaller/mid-cap examples
        tickerDatabase.add(new TickerSuggestion("SHOP", "Shopify Inc.", "NYSE", "Common Stock", "CA", "Mid Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("PLTR", "Palantir Technologies Inc.", "NYSE", "Common Stock", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("ROKU", "Roku Inc.", "NASDAQ", "Common Stock", "US", "Mid Cap", "USD"));
        
        // ETFs for diversification
        tickerDatabase.add(new TickerSuggestion("SPY", "SPDR S&P 500 ETF Trust", "NYSE", "ETF", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("QQQ", "Invesco QQQ Trust", "NASDAQ", "ETF", "US", "Large Cap", "USD"));
        tickerDatabase.add(new TickerSuggestion("VTI", "Vanguard Total Stock Market ETF", "NYSE", "ETF", "US", "Large Cap", "USD"));
    }
}
