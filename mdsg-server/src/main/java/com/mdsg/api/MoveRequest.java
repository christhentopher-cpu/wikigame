package com.mdsg.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MoveRequest(
		@NotBlank String playerId,
		@NotBlank @Pattern(regexp = "^Q\\d+$", message = "must be a Wikidata ID like Q16538") String nodeId) {
}
