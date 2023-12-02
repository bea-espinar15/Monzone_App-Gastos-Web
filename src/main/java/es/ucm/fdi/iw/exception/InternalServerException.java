package es.ucm.fdi.iw.exception;

public class InternalServerException extends RuntimeException {
    
    public InternalServerException(ErrorType error) {
        super(error.getErrorMessage());
    }

}
