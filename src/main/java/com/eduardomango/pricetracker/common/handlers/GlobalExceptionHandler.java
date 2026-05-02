package com.eduardomango.pricetracker.common.handlers;

import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.common.exceptions.ParseException;
import com.eduardomango.pricetracker.common.exceptions.UnsuportedWebsite;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> entityNotFound(EntityNotFoundException e, HttpServletRequest request) {

        //Uses the default ProblemDetail implementation
        //It is an RFC 7807 compliant error representation
        ProblemDetail error = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);


        error.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase()); // "Not Found"
        error.setDetail(e.getMessage());                       // message

        //Adds custom properties to the error
        error.setProperty("timestamp", Instant.now());
        error.setProperty("path", request.getRequestURI());
        error.setProperty("method", request.getMethod());


        //Adds links to the error
        //It would be ideal to create documentation for the error in this URL
        error.setType(URI.create("https://api.tuapp.com/errors/entity-not-found"));
        error.setInstance(URI.create(request.getRequestURI()));

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        String message = "Database constraint violation";

        Throwable root = ex.getRootCause();

        if (root != null && root.getMessage().contains("email_address")) {
            message = "Email already exists";
        }
        else if (root != null && root.getMessage().contains("url")) {
            message = "Product already exists";
        }

        problem.setTitle("Conflict");
        problem.setDetail(message);
        problem.setType(URI.create("https://api.tuapp.com/errors/conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("method", request.getMethod());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(UnsuportedWebsite.class)
    public ResponseEntity<ProblemDetail> handleUnsuportedWebsite(UnsuportedWebsite ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Website not supported");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.tuapp.com/errors/website-not-supported"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("method", request.getMethod());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(problem);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<ProblemDetail> handleParseException(ParseException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Parse error");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://api.tuapp.com/errors/parse-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("method", request.getMethod());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }


}
