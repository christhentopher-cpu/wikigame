package com.mdsg.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGameRequest(@NotBlank @Size(max = 50) String playerName) {
}
