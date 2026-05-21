package com.mdsg.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateGameRequest(
		@NotBlank @Pattern(regexp = "^Q\\d+$", message = "must be a Wikidata ID from movie search") String startMovieId,
		@NotBlank @Pattern(regexp = "^Q\\d+$", message = "must be a Wikidata ID from movie search") String targetMovieId,
		@NotBlank @Size(max = 50) String hostPlayerName) {
}
