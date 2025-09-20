package com.portfolio.management.infrastructure.adapters.incoming.web.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * Response DTO for ticker suggestions
 */
@RegisterForReflection
@Schema(
        name = "SuggestionsResponse",
        description = "Response containing ticker/symbol suggestions based on search query"
)
public record SuggestionsResponse(
        @Schema(
                description = "List of ticker suggestions matching the search query",
                example = "[{\"id\":1,\"symbol\":\"AAPL\",\"name\":\"Apple Inc.\",\"exchange\":\"NASDAQ\",\"type\":\"Common Stock\",\"country\":\"US\",\"currency\":\"USD\"}]"
        )
        List<TickerSuggestionDto> suggestions,

        @Schema(
                description = "The original search query that was processed",
                example = "apple"
        )
        String query,

        @Schema(
                description = "Total number of suggestions returned (same as suggestions.length)",
                example = "1",
                minimum = "0"
        )
        int count
) {

    @RegisterForReflection
    @Schema(
            name = "TickerSuggestionDto",
            description = "Individual ticker/symbol suggestion with company details"
    )
    public record TickerSuggestionDto(
            @Schema(
                    description = "Stock unique identifier",
                    example = "1",
                    required = true
            )
            Long id,

            @Schema(
                    description = "Stock ticker symbol (e.g., AAPL, MSFT, GOOGL)",
                    example = "AAPL",
                    required = true
            )
            String symbol,

            @Schema(
                    description = "Full company name",
                    example = "Apple Inc.",
                    required = true
            )
            String name,

            @Schema(
                    description = "Stock exchange where the ticker is traded",
                    example = "NASDAQ"
            )
            String exchange,

            @Schema(
                    description = "Type of security (e.g., Common Stock, ETF, Preferred Stock)",
                    example = "Common Stock"
            )
            String type,

            @Schema(
                    description = "Geographic country of the company",
                    example = "US"
            )
            String country,

            @Schema(
                    description = "Trading currency",
                    example = "USD"
            )
            String currency
    ) {
    }
}
