package es.ucm.fdi.iw.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import es.ucm.fdi.iw.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import es.ucm.fdi.iw.exception.*;

/**
 *  Non-authenticated requests only.
 */
@Controller
public class RootController {

	private static final Logger log = LogManager.getLogger(RootController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

	@GetMapping("/")
    public String root() {
        return login();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String home() {
        return "signup";
    }

  
    
    /*
     * SIGNUP
     */
    @PostMapping("/signup")
    @Transactional
    @ResponseBody
    public String signUp(@RequestBody JsonNode jsonNode) {

        String name = jsonNode.get("name").asText();
        String username = jsonNode.get("username").asText();
        String password = jsonNode.get("password").asText();
    
        // check params
        if (name.equals("") || username.equals("") || password.equals(""))
            throw new BadRequestException(ErrorType.E_EMPTY_FIELDS);
    
        // check repeat username
        List<User> u = entityManager.createNamedQuery("User.byUsername", User.class)
		        .setParameter("username", username)
		        .getResultList();
        if (u == null || u.size() > 0) {
            log.warn("Error: user repeat " + username);
            throw new BadRequestException(ErrorType.E_USERNAME_TAKEN);                      
        }
            

        // create user
        User user = new User(name, username, encodePassword(password));
        entityManager.persist(user);
        log.warn("TODO OK: user created " + username);

        return "{\"action\": \"redirect\",\"redirect\": \"/\"}";

    }

    /**
     * Encodes a password, so that it can be saved for future checking. Notice
     * that encoding the same password multiple times will yield different
     * encodings, since encodings contain a randomly-generated salt.
     * 
     * @param rawPassword to encode
     * @return the encoded password (typically a 60-character string)
     *         for example, a possible encoding of "test" is
     *         {bcrypt}$2y$12$XCKz0zjXAP6hsFyVc8MucOzx6ER6IsC1qo5zQbclxhddR1t6SfrHm
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

}
