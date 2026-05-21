package com.wikigame.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoundSetupRequest(
		@NotBlank String playerId,
		@NotBlank @Pattern(regexp = "^Q\\d+$", message = "must be a Wikidata ID from movie search") String startMovieId,
		@NotBlank @Pattern(regexp = "^Q\\d+$", message = "must be a Wikidata ID from movie search") String targetMovieId) {
}
