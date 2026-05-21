package com.mdsg.ws;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.mdsg.game.GameNotFoundException;
import com.mdsg.game.InvalidMoveException;
import com.mdsg.game.NotActivePlayerException;

@ControllerAdvice
public class GameWsExceptionHandler {

	@MessageExceptionHandler(InvalidMoveException.class)
	public ProblemDetail handleInvalidMove(InvalidMoveException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@MessageExceptionHandler(NotActivePlayerException.class)
	public ProblemDetail handleNotActive(NotActivePlayerException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@MessageExceptionHandler(GameNotFoundException.class)
	public ProblemDetail handleNotFound(GameNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

}
