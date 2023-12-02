package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.exception.BadRequestException;
import es.ucm.fdi.iw.exception.ForbiddenException;
import es.ucm.fdi.iw.exception.InternalServerException;

@ControllerAdvice
public class ExceptionController {
    
    private static final Logger log = LogManager.getLogger(ExceptionController.class);

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleBadRequestException(BadRequestException ex) {
        return "{\"message\": \"" + ex.getMessage() + "\"}";
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbiddenException(ForbiddenException ex) {        
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(InternalServerException.class)
    public void handleInternalServerException(InternalServerException ex) {
        log.warn("ATTENTION: ", ex.getMessage());
    }

}
