package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import es.ucm.fdi.iw.model.Debt;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Notification.NotificationType;
import es.ucm.fdi.iw.model.User.Role;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Group.Currency;
import es.ucm.fdi.iw.model.Member.GroupRole;
import es.ucm.fdi.iw.GroupAccessUtilities;
import es.ucm.fdi.iw.NotificationSender;
import es.ucm.fdi.iw.model.Transferable;

import es.ucm.fdi.iw.exception.*;

/**
 * Group (and expenses) management.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("group")
public class GroupController {

    // private static final Logger log =
    // LogManager.getLogger(GroupController.class);
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private NotificationSender notifSender;

    @Autowired
    private GroupAccessUtilities groupAccessUtilities;

    private static final Logger log = LogManager.getLogger(GroupController.class);

    /*
     * 
     * GET MAPPINGS
     * 
     */

    /*
     * View: new group
     */
    @GetMapping("/new")
    public String newGroupView(HttpSession session, Model model) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }

        model.addAttribute("currencies", currencies);
        model.addAttribute("group", null);
        model.addAttribute("userId", user.getId());
        model.addAttribute("budget", 0);
        model.addAttribute("isGroupAdmin", true);

        return "group_config";

    }

    /*
     * View: group home page
     */
    @GetMapping("{groupId}")
    public String index(@PathVariable long groupId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        model.addAttribute("groupId", groupId);
        model.addAttribute("group", group);
        model.addAttribute("userId", user.getId());

        return "group";
    }

    /*
     * View: group configuration
     */
    @GetMapping("{groupId}/config")
    public String config(@PathVariable long groupId, Model model, HttpSession session) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // Get budget
        model.addAttribute("budget", member.getBudget());

        // get members
        List<Member> members = group.getMembers();

        // get currencies
        List<String> currencies = new ArrayList<>();
        for (Group.Currency g : Group.Currency.values()) {
            currencies.add(g.name());
        }

        model.addAttribute("isGroupAdmin", member.getRole() == GroupRole.GROUP_MODERATOR);
        model.addAttribute("currencies", currencies);
        model.addAttribute("group", group);
        model.addAttribute("userId", user.getId());
        model.addAttribute("members", members);

        return "group_config";
    }

    /*
     * Get groupconfig
     */
    @ResponseBody
    @GetMapping("{groupId}/getGroupConfig")
    public Group.Transfer getGroupConfig(@PathVariable long groupId, HttpSession session) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        return group.toTransfer();
    }

    /*
     * Get members
     */
    @ResponseBody
    @GetMapping("{groupId}/getMembers")
    public List<Member.Transfer> getMembers(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // get members
        List<Member> members = group.getMembers();

        return members.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Get and calculate debts
     */
    @ResponseBody
    @GetMapping("{groupId}/getDebts")
    public List<Debt.Transfer> getDebts(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // get debts
        List<Debt> debts = group.getDebts();

        return debts.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * Get number of expenses
     */
    @ResponseBody
    @GetMapping("{groupId}/getTotExpenses")
    public Integer getTotExpenses(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group or admin
        try {
            groupAccessUtilities.getMemberOrThrow(groupId, user.getId());
        } catch (Exception e) {
            if (!user.hasRole(Role.ADMIN))
                throw new ForbiddenException(ErrorType.E_GROUP_FORBIDDEN);
        }

        // get expenses
        List<Participates> ps = group.getOwns();
        // each expense only once, even if more than 1 user participates in it
        Set<Expense> uniqueExpenses = new HashSet<>();
        for (Participates p : ps) {
            uniqueExpenses.add(p.getExpense());
        }

        return uniqueExpenses.size();
    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

    @Async
    private CompletableFuture<Void> createAndSendNotifs(NotificationType type, User sender, Group group) {

        for (Member m : group.getMembers()) {
            // Do not send notification to sender
            if (m.getUser().getId() == sender.getId() || !m.isEnabled())
                continue;

            Notification notif = new Notification(type, sender, m.getUser(), group);
            entityManager.persist(notif);
            entityManager.flush();

            // Send notification
            notifSender.sendNotification(notif, "/user/" + m.getUser().getUsername() + "/queue/notifications");
        }

        return CompletableFuture.completedFuture(null);

    }

    /*
     * Creates group
     */
    @ResponseBody
    @Transactional
    @PostMapping("/newGroup")
    public String newGroup(HttpSession session, @RequestBody JsonNode jsonNode) {

        User u = (User) session.getAttribute("u");
        u = entityManager.find(User.class, u.getId());

        String name = jsonNode.get("name").asText();
        String desc = jsonNode.get("desc").asText();
        Float budget = Float.parseFloat(jsonNode.get("budget").asText());
        Integer currId = jsonNode.get("currId").asInt();

        // parse name
        if (name == null || name == "")
            throw new BadRequestException(ErrorType.E_EMPTY_NAME);
        else if (name.length() > 100)
            throw new BadRequestException(ErrorType.E_LONG_NAME);

        // parse budget
        if (budget < 0)
            throw new BadRequestException(ErrorType.E_INVALID_BUDGET);

        // parse curr
        if (currId < 0 || currId >= Currency.values().length)
            throw new BadRequestException(ErrorType.E_INVALID_CURRENCY);
        Currency curr = Currency.values()[currId];

        // create group
        if (desc == null)
            desc = "";
        else if (desc.length() > 255)
            throw new BadRequestException(ErrorType.E_LONG_DESC);
        Group g = new Group(name, desc, curr);
        entityManager.persist(g);
        entityManager.flush(); // forces DB to add group & assign valid id

        log.warn("ID de grupo creado es {}", g.getId());

        // create member
        Member m = new Member(new MemberID(g.getId(), u.getId()), true, GroupRole.GROUP_MODERATOR, budget, 0, g,
                u);
        entityManager.persist(m);
        entityManager.flush(); // forces DB to add group & assign valid id
        // add member to the group
        g.getMembers().add(m);
        u.getMemberOf().add(m);
        // update group
        g.setNumMembers(1);
        g.setTotBudget(budget);

        return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";

    }

    /*
     * Updates group
     */
    @ResponseBody
    @Transactional
    @PostMapping("{groupId}/updateGroup")
    public String updateGroup(HttpSession session, @PathVariable long groupId, @RequestBody JsonNode jsonNode) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        String name = jsonNode.get("name").asText();
        String desc = jsonNode.get("desc").asText();
        Float budget = Float.parseFloat(jsonNode.get("budget").asText());
        Integer currId = jsonNode.get("currId").asInt();

        // only moderators can edit group settings
        if (member.getRole() == GroupRole.GROUP_MODERATOR) {
            // parse curr
            if (currId < 0 || currId >= Currency.values().length)
                throw new BadRequestException(ErrorType.E_INVALID_CURRENCY);
            Currency curr = Currency.values()[currId];

            // update group
            if (desc == null)
                desc = "";
            group.setDesc(desc);
            group.setName(name);
            group.setCurrency(curr);
        }

        // check member budget
        if (budget < 0)
            throw new BadRequestException(ErrorType.E_INVALID_BUDGET);

        group.setTotBudget(group.getTotBudget() - member.getBudget());
        member.setBudget(budget);
        group.setTotBudget(group.getTotBudget() + budget);

        // Send notif
        createAndSendNotifs(NotificationType.GROUP_MODIFIED, user, group);

        // send group to other members
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_MODIFIED);

        return "{\"action\": \"none\"}";
    }

    /*
     * Delete group
     */
    @ResponseBody
    @Transactional
    @PostMapping("{groupId}/delGroup")
    public String delGroup(HttpSession session, @PathVariable long groupId) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // only moderators can delete group
        if (member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(ErrorType.E_DELETE_FORBIDDEN);
        }

        // check all balances = 0 and remove member from the group
        List<Member> members = group.getMembers();
        for (Member m : members) {
            if (m.getBalance() != 0)
                throw new BadRequestException(ErrorType.E_BALANCES_NZ);
            m.setEnabled(false);
            ;
        }

        // disable expenses
        List<Participates> owns = group.getOwns();
        for (Participates o : owns) {
            Expense e = o.getExpense();
            e.setEnabled(false);
        }

        // disable group
        group.setEnabled(false);

        // send notif
        createAndSendNotifs(NotificationType.GROUP_DELETED, user, group);

        // send group to other members
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_DELETED);

        return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";

    }

    /*
     * Remove member
     */
    @Transactional
    @ResponseBody
    @PostMapping("{groupId}/delMember")
    public String removeMember(@PathVariable long groupId, Model model, HttpSession session,
            @RequestBody JsonNode node) {

        long removeId = node.get("removeId").asLong();

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if requesting user belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check if member to remove belongs to group (only if not leaving group)
        Member removeMember = entityManager.find(Member.class, new MemberID(groupId, removeId));
        if (removeId != user.getId()) {
            if (removeMember == null || !removeMember.isEnabled()) {
                throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
            }
        }

        // only moderators can remove other members
        if (user.getId() != removeId && member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(ErrorType.E_DELMEMBER_FORBIDDEN);
        }

        // if only member, delete group
        if (group.getMembers().size() == 1) {
            log.warn("Deleting EMPTY group {}", group);
            return delGroup(session, groupId);
        }

        /*
         * moderator can't leave the group if there are no other admins
         */
        if (user.getId() == removeId && member.getRole() == GroupRole.GROUP_MODERATOR) {
            List<Member> moderators = entityManager.createNamedQuery("Member.getGroupAdmins", Member.class)
                    .setParameter("groupId", groupId).getResultList();
            if (moderators.size() == 1)
                throw new BadRequestException(ErrorType.E_ONLY_MODERATOR);
        }

        // balance must be 0
        if (removeMember.getBalance() != 0) {
            throw new BadRequestException(ErrorType.E_MEMBER_BALANCE_NZ);
        }

        // remove member
        removeMember.setEnabled(false);
        // update group budget
        group.setNumMembers(group.getNumMembers() - 1);
        group.setTotBudget(group.getTotBudget() - removeMember.getBudget());

        log.warn("Removed user from group {}", group);

        // Send notification to members
        createAndSendNotifs(NotificationType.GROUP_MEMBER_REMOVED, removeMember.getUser(), group);

        // Send group to members and particularly to the member removed
        notifSender.sendTransfer(group, "/user/" + removeMember.getUser().getUsername() + "/queue/notifications",
                "GROUP", NotificationType.GROUP_MEMBER_REMOVED);
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_MEMBER_REMOVED);

        if (user.getId() == removeId) {
            return "{\"action\": \"redirect\",\"redirect\": \"/user/\"}";
        }

        return "{\"action\": \"none\"}";
    }

    /**
     * Invites a user to a group
     */
    @PostMapping("/{groupId}/inviteMember")
    @Transactional
    @ResponseBody
    public String inviteMember(@PathVariable long groupId, @RequestBody JsonNode o, HttpSession session)
            throws JsonProcessingException {

        String username = o.get("username").asText();
        User sender = (User) session.getAttribute("u");
        sender = entityManager.find(User.class, sender.getId());

        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(ErrorType.E_GROUP_FORBIDDEN);

        // check if sender belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, sender.getId());

        // only moderators can invite new members
        if (member.getRole() != GroupRole.GROUP_MODERATOR)
            throw new ForbiddenException(ErrorType.E_INVITE_FORBIDDEN);

        // Check invited user
        List<User> userList = entityManager.createNamedQuery("User.byUsername", User.class)
                .setParameter("username", username).getResultList();

        if (userList.isEmpty() || !userList.get(0).isEnabled())
            throw new BadRequestException(ErrorType.E_USER_NE);

        if (userList.size() > 1)
            throw new InternalServerException(ErrorType.E_INTERNAL_SERVER);

        User user = userList.get(0);
        MemberID mId = new MemberID(group.getId(), user.getId());
        member = entityManager.find(Member.class, mId);
        // check user not already member
        if (member == null || !member.isEnabled()) {
            // check user not already invited
            List<Notification> invites = entityManager.createNamedQuery("Invite.byUserAndGroup", Notification.class)
                    .setParameter("userId", user.getId())
                    .setParameter("groupId", group.getId())
                    .setParameter("type", NotificationType.GROUP_INVITATION)
                    .getResultList();

            if (invites.isEmpty()) {
                Notification invite = new Notification(NotificationType.GROUP_INVITATION, sender, user, group);
                entityManager.persist(invite);
                entityManager.flush();

                // Send notification
                notifSender.sendNotification(invite, "/user/" + user.getUsername() + "/queue/notifications");

                return "{\"status\":\"invited\"}";
            } else {
                throw new BadRequestException(ErrorType.E_INVITED_USER);
            }
        } else {
            // user is already in a group
            log.info("User {} cannot join group {}", member.getUser().getUsername(), group.getName());
            throw new BadRequestException(ErrorType.E_ALREADY_BELONGS);
        }
    }

    /**
     * Accept invite to group
     */
    @PostMapping("/{groupId}/acceptInvite")
    @Transactional
    @ResponseBody
    public String acceptInvite(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if an invite for user exists
        List<Notification> invites = entityManager.createNamedQuery("Invite.byUserAndGroup", Notification.class)
                .setParameter("userId", user.getId())
                .setParameter("groupId", groupId)
                .setParameter("type", NotificationType.GROUP_INVITATION)
                .getResultList();

        if (invites.size() < 1)
            throw new ForbiddenException(ErrorType.E_INVITATION_FORBIDDEN);

        // check if group still exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new BadRequestException(ErrorType.E_GROUP_NE);

        for (Notification invite : invites) {
            // Check if sender is an admin in group
            User sender = invite.getSender();
            Member m = entityManager.find(Member.class, new MemberID(group.getId(), sender.getId()));
            if (m == null || m.getRole() != GroupRole.GROUP_MODERATOR) {
                // notification should be deleted as it is no longer valid
                user.getNotifications().remove(invite);
                entityManager.remove(invite);

                return "{\"status\": \"expired\"}";
            }

            // notification valid, check if user is not already member
            Member newMember = entityManager.find(Member.class, new MemberID(group.getId(), user.getId()));
            if (newMember != null && newMember.isEnabled()) {
                // notification should be deleted as it is no longer valid
                user.getNotifications().remove(invite);
                entityManager.remove(invite);

                return "{\"status\": \"already_in_group\"}";
            } else if (newMember == null) {
                newMember = new Member(new MemberID(group.getId(), user.getId()), true, GroupRole.GROUP_USER, 0, 0,
                        group, user);
                entityManager.persist(newMember);
            } else {
                newMember.setEnabled(true);
            }
            // Update user and group
            user.getMemberOf().add(newMember);
            group.getMembers().add(newMember);
            group.setNumMembers(group.getNumMembers() + 1);

            // Delete notification
            entityManager.remove(invite);
            entityManager.flush();
        }

        // Send notification
        createAndSendNotifs(NotificationType.GROUP_INVITATION_ACCEPTED, user, group);

        // Send group transfer to user (to render if on /user/)
        notifSender.sendTransfer(group, "/user/" + user.getUsername() + "/queue/notifications", "GROUP",
                NotificationType.GROUP_INVITATION_ACCEPTED);
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_INVITATION_ACCEPTED);

        return "{\"status\": \"ok\",\"id\": \"" + groupId + "\"}";
    }

    /*
     * Make moderator
     */
    @Transactional
    @ResponseBody
    @PostMapping("{groupId}/makeModerator")
    public String makeModerator(@PathVariable long groupId, Model model, HttpSession session,
            @RequestBody JsonNode node) {

        long moderatorId = node.get("moderatorId").asLong();

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if requesting user belongs to the group
        Member member = groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check if member to become moderator belongs to group
        Member newModerator = entityManager.find(Member.class, new MemberID(groupId, moderatorId));
        if (newModerator == null || !newModerator.isEnabled()) {
            throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
        }

        // only moderators can remove other members
        if (member.getRole() != GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(ErrorType.E_MAKEMODERATOR_FORBIDDEN);
        }

        // check if new moderator wasn't already a moderator
        if (newModerator.getRole() == GroupRole.GROUP_MODERATOR) {
            throw new ForbiddenException(ErrorType.E_ALREADYMODERATOR_FORBIDDEN);
        }

        // make moderator
        newModerator.setRole(GroupRole.GROUP_MODERATOR);

        log.warn("New moderator in group {}", group);

        // Send notification to members
        createAndSendNotifs(NotificationType.GROUP_NEW_MODERATOR, newModerator.getUser(), group);

        // Send group to members and particularly to the new moderator
        notifSender.sendTransfer(group, "/user/" + newModerator.getUser().getUsername() + "/queue/notifications",
                "GROUP", NotificationType.GROUP_NEW_MODERATOR);
        notifSender.sendTransfer(group, "/topic/group/" + groupId, "GROUP", NotificationType.GROUP_NEW_MODERATOR);

        return "{\"action\": \"none\"}";
    }

}