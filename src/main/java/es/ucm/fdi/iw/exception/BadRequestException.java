package es.ucm.fdi.iw.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(ErrorType error) {
        super(error.getErrorMessage());
    }

}
