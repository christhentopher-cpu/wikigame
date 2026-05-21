package com.mdsg.api;

import jakarta.validation.constraints.NotBlank;

public record GiveUpRequest(@NotBlank String playerId) {
}
