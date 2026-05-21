package com.wikigame.api;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wikigame.game.GameConflictException;
import com.wikigame.game.GameNotFoundException;
import com.wikigame.game.InvalidMoveException;
import com.wikigame.game.NotActivePlayerException;
import com.wikigame.wikidata.WikidataUnavailableException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		String detail = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining("; "));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
		problem.setTitle("Validation failed");
		return problem;
	}

	@ExceptionHandler(GameNotFoundException.class)
	ProblemDetail handleNotFound(GameNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Game not found");
		return problem;
	}

	@ExceptionHandler(GameConflictException.class)
	ProblemDetail handleConflict(GameConflictException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Game conflict");
		return problem;
	}

	@ExceptionHandler(InvalidMoveException.class)
	ProblemDetail handleInvalidMove(InvalidMoveException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Invalid move");
		return problem;
	}

	@ExceptionHandler(NotActivePlayerException.class)
	ProblemDetail handleNotActivePlayer(NotActivePlayerException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
		problem.setTitle("Not active player");
		return problem;
	}

	@ExceptionHandler(WikidataUnavailableException.class)
	ProblemDetail handleWikidataUnavailable(WikidataUnavailableException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
		problem.setTitle("Wikidata unavailable");
		return problem;
	}

}
