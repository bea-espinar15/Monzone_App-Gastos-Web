package es.ucm.fdi.iw.exception;

public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(ErrorType error) {
        super(error.getErrorMessage());
    }

}
