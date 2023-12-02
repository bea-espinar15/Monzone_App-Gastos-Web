package es.ucm.fdi.iw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Transferable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 *  Site administration.
 *
 *  Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

    private static final Logger log = LogManager.getLogger(AdminController.class);

    @Autowired
    private EntityManager entityManager;

    /*
     * GET MAPPINGS
     */

    /*
     * Main view
     */
    @GetMapping("/")
    public String index(HttpSession session) {
        User u = (User) session.getAttribute("u");
        log.warn("Usuario {} ha accedido a admin", u.getUsername());

        return "admin";
    }

    /*
     * Group view
     */
    @GetMapping("/{groupId}")
    public String index(Model model, HttpSession session, @PathVariable long groupId) {
        // get group
        Group group = entityManager.find(Group.class, groupId);
        model.addAttribute("group", group);

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }
        model.addAttribute("currencies", currencies);

        // get expenses
        List<Participates> ps = group.getOwns();
        // each expense only once, even if more than 1 user participates in it
        Set<Expense> uniqueExpenses = new HashSet<>();
        for (Participates p : ps) {
            uniqueExpenses.add(p.getExpense());
        }
        List<Expense> expenses = new ArrayList<>(uniqueExpenses);
        model.addAttribute("expenses", expenses);

        return "admin_group";
    }

    /*
     * Get all groups
     */
    @ResponseBody
    @Transactional
    @GetMapping("/getAllGroups")
    public List<Group.Transfer> getAllGroups(HttpSession session) {
        List<Group> groups = entityManager.createNamedQuery("Group.getAllGroups", Group.class).getResultList();
        return groups.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Get all users
     */
    @ResponseBody
    @Transactional
    @GetMapping("/getAllUsers")
    public List<User.Transfer> getAllUsers(HttpSession session) {
        List<User> users = entityManager.createNamedQuery("User.getAllUsers", User.class).getResultList();
        return users.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Search groups
     */
    @ResponseBody
    @GetMapping("searchGroup/{id}")
    public List<Long> searchGroupsIds(@PathVariable String id) {
        List<Long> ids = new ArrayList<>();
        try {
            Long groupId = Long.parseLong(id);
            ids = entityManager.createNamedQuery("Group.getGroupIdsLike", Long.class).setParameter("groupId", groupId)
                    .getResultList();
        } catch (NumberFormatException e) {
        }
        return ids;
    }

    /*
     * Search users
     */
    @ResponseBody
    @GetMapping("searchUser/{request}")
    public List<Long> searchUsersIds(@PathVariable String request) {
        List<Long> ids = entityManager.createNamedQuery("User.getUserIdsLike", Long.class)
                .setParameter("request", request).getResultList();
        return ids;
    }

}