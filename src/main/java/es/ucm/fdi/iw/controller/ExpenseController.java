package es.ucm.fdi.iw.controller;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import es.ucm.fdi.iw.DebtCalculator;
import es.ucm.fdi.iw.GroupAccessUtilities;
import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.NotificationSender;
import es.ucm.fdi.iw.model.Debt;
import es.ucm.fdi.iw.model.DebtID;
import es.ucm.fdi.iw.model.Expense;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Participates;
import es.ucm.fdi.iw.model.ParticipatesID;
import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.Type;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Notification.NotificationType;


import es.ucm.fdi.iw.exception.*;

/**
 * Group (and expenses) management.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("group/{groupId}")
public class ExpenseController {

    // private static final Logger log =
    // LogManager.getLogger(GroupController.class);
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    @Autowired
    private DebtCalculator debtCalculator;

    @Autowired
    private NotificationSender notifSender;

    @Autowired
    private GroupAccessUtilities groupAccessUtilities;

    private static final Logger log = LogManager.getLogger(ExpenseController.class);

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Failed to save image") // 500
    public static class ImageSavingFailed extends RuntimeException {
    }
    
    private void setExpenseAttributes(Group group, long expenseId, Model model, boolean newExpense) {
        List<Participates> participants = new ArrayList<>();
        if (!newExpense) {
            entityManager.createNamedQuery("Participates.getParticipants", Participates.class)
                    .setParameter("groupId", group.getId())
                    .setParameter("expenseId", expenseId)
                    .getResultList();
        }
        model.addAttribute("participants", participants);
        List<Member> members = group.getMembers();
        model.addAttribute("members", members);
        List<Type> types = entityManager.createNamedQuery("Type.getAllTypes", Type.class).getResultList();
        model.addAttribute("types", types);
        model.addAttribute("groupId", group.getId());
        model.addAttribute("newExpense", newExpense);
    }

    private Expense getExpenseOrThrow(long expenseId){
        Expense expense = entityManager.find(Expense.class, expenseId);
        if (expense == null || !expense.isEnabled())
            throw new ForbiddenException(ErrorType.E_EXPENSE_FORBIDDEN);

        return expense;
    }

    private void expenseBelongsToGroupOrThrow(Group group, Expense expense) {
        if (!group.hasExpense(expense))
            throw new ForbiddenException(ErrorType.E_EXPENSE_FORBIDDEN);
    }

    /*
     * 
     * GET MAPPINGS
     * 
     */

    /*
     * View: create group expense
     */
    @GetMapping("/new")
    public String newExpenseView(@PathVariable long groupId, Model model, HttpSession session) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        setExpenseAttributes(group, 0, model, true);
        model.addAttribute("group", group);

        return "expense";
    }

    /*
     * Get all expenses
     */
    @ResponseBody
    @Transactional
    @GetMapping("/getExpenses")
    public List<Expense.Transfer> getExpenses(@PathVariable long groupId, HttpSession session) {
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // get expenses
        List<Expense> expenses = entityManager.createNamedQuery("Participates.getUniqueExpensesByGroup", Expense.class).setParameter("groupId", groupId).getResultList();

        return expenses.stream().map(Transferable::toTransfer).collect(Collectors.toList());
    }

    /*
     * View: edit group expense
     */
    @GetMapping("{expenseId}")
    public String expense(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session) {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check if expense exists
        Expense expense = getExpenseOrThrow(expenseId);

        // check if expense belongs to the group
        expenseBelongsToGroupOrThrow(group, expense);

        setExpenseAttributes(group, expenseId, model, false);
        model.addAttribute("expense", expense);

        // Get array of participants
        List<Participates> participates = entityManager
                .createNamedQuery("Participates.getParticipants", Participates.class)
                .setParameter("groupId", group.getId()).setParameter("expenseId", expenseId).getResultList();

        // Change array to participantIds
        List<Long> participateIds = new ArrayList<>();
        for (Participates p : participates) {
            participateIds.add(p.getUser().getId());
        }

        model.addAttribute("participateIds", participateIds);
        model.addAttribute("group", group);

        return "expense";
    }

    /*
     * Returns the default expense pic
     * 
     * @return
     */
    private static InputStream defaultExpensePic() {
        return new BufferedInputStream(Objects.requireNonNull(
                ExpenseController.class.getClassLoader().getResourceAsStream("static/img/add-image.png")));
    }

    /**
     * Downloads a pic for an expense
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("{expenseId}/pic")
    public StreamingResponseBody expensePic(@PathVariable long groupId, @PathVariable long expenseId, Model model,
            HttpSession session) throws IOException {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check if expense exists
        Expense exp = entityManager.find(Expense.class, expenseId);

        // If expense == null, its a new expense
        if(exp == null) {
            InputStream in = new BufferedInputStream(ExpenseController.defaultExpensePic());
            return os -> FileCopyUtils.copy(in, os);
        }

        // Check if expense exists but disabled
        if (exp != null && !exp.isEnabled())
            throw new ForbiddenException(ErrorType.E_EXPENSE_FORBIDDEN);

        // check if expense belongs to the group
        expenseBelongsToGroupOrThrow(group, exp);

        File f = localData.getFile("expense", String.valueOf(expenseId));
        InputStream in = new BufferedInputStream(
                f.exists() ? new FileInputStream(f) : ExpenseController.defaultExpensePic());
        return os -> FileCopyUtils.copy(in, os);
    }

    /*
     * 
     * POST MAPPINGS
     * 
     */

    class PostParams {
        public Boolean valid = false;
        public User currUser;
        public Group group;
        public User paidBy;
        public Member paidByMember;
        public Type type;
        public LocalDate date;
        public List<User> participateUsers;
        public List<Member> participateMembers;
    }

    private PostParams validatedPostParams(HttpSession session, long groupId, String name, String desc, String dateString, float amount, long paidById, List<String> participateIds, long typeId) {

        PostParams validated = new PostParams();
        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());
        validated.currUser = user;

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);
        validated.group = group;

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check name
        if (name == null || name == "")
            throw new BadRequestException(ErrorType.E_EMPTY_NAME);
        else if (name.length() > 100)
            throw new BadRequestException(ErrorType.E_LONG_NAME);

        // check desc
        if (desc != null && desc.length() > 255)
            throw new BadRequestException(ErrorType.E_LONG_DESC);

        // check if user who paid exists
        User paidBy = entityManager.find(User.class, paidById);
        if (paidBy == null || !paidBy.isEnabled())
            throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
        validated.paidBy = paidBy;

        // check if user who paid belongs to the group
        MemberID paidByMemberId = new MemberID(groupId, paidById);
        Member paidByMember = entityManager.find(Member.class, paidByMemberId);
        if (paidByMember == null || !paidByMember.isEnabled())
            throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
        validated.paidByMember = paidByMember;

        // check if type exists
        Type type = entityManager.find(Type.class, typeId);
        if (type == null)
            throw new BadRequestException(ErrorType.E_INVALID_TYPE);
        validated.type = type;

        // check date format
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            throw new BadRequestException(ErrorType.E_INVALID_DATE);
        }
        if (date.isAfter(LocalDate.now()))
            throw new BadRequestException(ErrorType.E_AFTER_DATE);
        validated.date = date;

        // check it has participants
        if (participateIds.isEmpty())
            throw new BadRequestException(ErrorType.E_NO_PARTICIPANTS);

        // check participants exist
        List<User> participateUsers = new ArrayList<>();
        for (String idString : participateIds) {
            long pId = Long.parseLong(idString);
            User pUser = entityManager.find(User.class, pId);
            if (pUser == null || !pUser.isEnabled())
                throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
            participateUsers.add(pUser);
        }
        validated.participateUsers = participateUsers;

        // check participants are members
        List<Member> participateMembers = new ArrayList<>();
        for (User u : participateUsers) {
            MemberID participateMemberId = new MemberID(groupId, u.getId());
            Member participateMember = entityManager.find(Member.class, participateMemberId);
            if (participateMember == null || !participateMember.isEnabled())
                throw new ForbiddenException(ErrorType.E_USER_FORBIDDEN);
            participateMembers.add(participateMember);
        }
        validated.participateMembers = participateMembers;

        // check amount is not negative
        if (amount <= 0)
            throw new BadRequestException(ErrorType.E_INVALID_AMOUNT);

        validated.valid = true;
        return validated;
    }

    @Async
    private CompletableFuture<Void> createAndSendNotifs(NotificationType type, User sender, List<User> notifRecipients, Group group, Expense e) {
        for (User u : notifRecipients) {
            // Do not send notification to sender
            if (u.getId() == sender.getId())
                continue;

            Notification notif = new Notification(type, sender, u, group, e);
            entityManager.persist(notif);
            entityManager.flush();

            // Send notification
            notifSender.sendNotification(notif, "/user/" + u.getUsername() + "/queue/notifications");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    private CompletableFuture<Void> checkBudgetsAndSendNotifs(List<Member> participateMembers) {
        for(Member m : participateMembers) {
            if(m.getBudget() > 0){
                Float percentage = -1 * m.getBalance() / m.getBudget();
                if(percentage >= 0.5f){
                    log.info("User {} has used {} of their budget", m.getUser().getUsername(), percentage);
                    String percentageString = "50%";
                    if (percentage >= 0.75f) {
                        percentageString =  "75%";
                    } else if (percentage >= 1f) {
                        percentageString =  "100%";
                    }

                    Notification notif = new Notification(NotificationType.BUDGET_WARNING, m.getUser(), m.getGroup(), percentageString);
                    entityManager.persist(notif);
                    entityManager.flush();
                    
                    // Send notification
                    notifSender.sendNotification(notif, "/user/" + m.getUser().getUsername() + "/queue/notifications");
                }
                log.info("User {} has not used more than 50% of their budget - {}", m.getUser().getUsername(), percentage);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /*
     * Add expense to group
     */
    @PostMapping("/newExpense")
    @Transactional
    @ResponseBody
    public String newExpense(@PathVariable long groupId, Model model, HttpSession session, @RequestParam("name") String name, @RequestParam("desc") String desc, @RequestParam("dateString") String dateString, @RequestParam("amount") float amount, @RequestParam("paidById") long paidById, @RequestParam("participateIds") List<String> participateIds, @RequestParam("typeId") long typeId, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        PostParams params = validatedPostParams(session, groupId, name, desc, dateString, amount, paidById, participateIds, typeId);

        /*
         * 
         * CREATE EXPENSE
         * 
         */
        if (desc == null)
            desc = "";

        // Create expense
        Expense exp = new Expense(name, desc, amount, params.date, params.type, params.paidBy);
        entityManager.persist(exp);
        entityManager.flush();

        // add balance to paidBy
        params.paidByMember.setBalance(params.paidByMember.getBalance() + amount);

        // add all participants
        for (User u : params.participateUsers) {
            ParticipatesID pId = new ParticipatesID(exp.getId(), u.getId());
            Participates participates = new Participates(pId, params.group, u, exp);
            entityManager.persist(participates);

            // add debts of balance
            MemberID memberID = new MemberID(params.group.getId(), u.getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - exp.getAmount() / params.participateUsers.size());
        }

        // save the new expense image
        if(imageFile != null){
            File f = localData.getFile("expense", "" + exp.getId());
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                byte[] imageBytes = imageFile.getBytes();
                stream.write(imageBytes);
                log.info("Uploaded photo for {} into {}!", exp.getId(), f.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Error uploading image " + exp.getId() + " ", e);
                throw new ImageSavingFailed();
            }
        }

        // recalculate debts
        List<Debt> oldDebts = params.group.getDebts();
        for (Debt d : oldDebts) {
            entityManager.remove(d);
        }

        List<Debt> newDebts = debtCalculator.calculateDebts(params.group.getMembers(), params.group);
        for (Debt d : newDebts) {
            entityManager.persist(d);
        }

        // send notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_CREATED, params.currUser, params.participateUsers, params.group, exp);

        // check if budget exceeded
        checkBudgetsAndSendNotifs(params.participateMembers);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + params.group.getId(), "EXPENSE", NotificationType.EXPENSE_CREATED);

        return "{\"action\": \"redirect\",\"redirect\": \"/group/" + groupId + "\"}";
    }

    /*
     * Edit group expense
     */
    @PostMapping("{expenseId}/updateExpense")
    @Transactional
    @ResponseBody
    public String updateExpense(@PathVariable long groupId, @PathVariable long expenseId, Model model, HttpSession session, @RequestParam("name") String name, @RequestParam("desc") String desc, @RequestParam("dateString") String dateString, @RequestParam("amount") float amount, @RequestParam("paidById") long paidById, @RequestParam("participateIds") List<String> participateIds, @RequestParam("typeId") long typeId, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        PostParams params = validatedPostParams(session, groupId, name, desc, dateString, amount, paidById, participateIds, typeId);

        // check if expense exists
        Expense exp = getExpenseOrThrow(expenseId);

        // check if expense belongs to the group
        expenseBelongsToGroupOrThrow(params.group, exp);

        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for (Participates p : participants) {
            MemberID memberID = new MemberID(params.group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
        }

        // delete owed from balance
        MemberID originalPaidByMemberID = new MemberID(params.group.getId(), exp.getPaidBy().getId());
        Member m = entityManager.find(Member.class, originalPaidByMemberID);
        m.setBalance(m.getBalance() - exp.getAmount());

        // delete removed participants
        for (Participates p : exp.getBelong()) {
            if (!participateIds.contains(String.valueOf(p.getUser().getId()))) {
                entityManager.remove(p);
            }
        }

        // save the new expense image
        if(imageFile != null){
            File f = localData.getFile("expense", "" + exp.getId());
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                byte[] imageBytes = imageFile.getBytes();
                stream.write(imageBytes);
                log.info("Uploaded photo for {} into {}!", exp.getId(), f.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Error uploading image " + exp.getId() + " ", e);
                throw new ImageSavingFailed();
            }
        }
        
        // update expense
        exp.setName(name);
        if (desc == null)
            desc = "";
        exp.setDesc(desc);
        exp.setPaidBy(params.paidBy);
        exp.setType(params.type);
        exp.setDate(params.date);
        exp.setAmount(amount);

        // add balance to paidBy
        params.paidByMember.setBalance(params.paidByMember.getBalance() + amount);

        // add all participants
        for (User u : params.participateUsers) {
            ParticipatesID pId = new ParticipatesID(exp.getId(), u.getId());
            Participates participates = entityManager.find(Participates.class, pId);
            if (participates == null) {
                participates = new Participates(pId, params.group, u, exp);
                entityManager.persist(participates);
            }
            // add debts of balance
            MemberID memberID = new MemberID(params.group.getId(), u.getId());
            m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() - amount / params.participateUsers.size());
        }

        // recalculate debts
        List<Debt> oldDebts = params.group.getDebts();
        for (Debt d : oldDebts) {
            entityManager.remove(d);
        }

        List<Debt> newDebts = debtCalculator.calculateDebts(params.group.getMembers(), params.group);
        for (Debt d : newDebts) {
            entityManager.persist(d);
        }

        // create notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_MODIFIED, params.currUser, params.participateUsers, params.group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + params.group.getId(), "EXPENSE", NotificationType.EXPENSE_MODIFIED);

        return "{\"action\": \"none\"}";
    }

    /*
     * Delete group expense
     */
    @PostMapping("{expenseId}/delExpense")
    @Transactional
    @ResponseBody
    public String deleteExpense(@PathVariable long groupId, @PathVariable long expenseId, Model model,
            HttpSession session) throws IOException {

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // check if expense exists
        Expense exp = getExpenseOrThrow(expenseId);

        // check if expense belongs to the group
        expenseBelongsToGroupOrThrow(group, exp);

        // List of users to notify
        List<User> notifyUsers = new ArrayList<>();

        // delete debts of balances
        List<Participates> participants = exp.getBelong();
        for (Participates p : participants) {
            MemberID memberID = new MemberID(group.getId(), p.getUser().getId());
            Member m = entityManager.find(Member.class, memberID);
            m.setBalance(m.getBalance() + exp.getAmount() / participants.size());
            notifyUsers.add(p.getUser());
        }

        // delete owed from balance
        MemberID paidByMemberID = new MemberID(group.getId(), exp.getPaidBy().getId());
        Member m = entityManager.find(Member.class, paidByMemberID);
        m.setBalance(m.getBalance() - exp.getAmount());

        // delete participants
        for (Participates p : participants)
            entityManager.remove(p);

        // save the new expense image
        File f = localData.getFile("expense", "" + exp.getId());
        if (f.delete()) {
            log.info("Delete photo for {} into {}!", exp.getId(), f.getAbsolutePath());
        } else {
            log.warn("Error deleting image " + exp.getId() + " ");
        }

        // disable expense
        exp.setEnabled(false);

        // recalculate debts
        List<Debt> oldDebts = group.getDebts();
        for (Debt d : oldDebts) {
            entityManager.remove(d);
        }

        List<Debt> newDebts = debtCalculator.calculateDebts(group.getMembers(), group);
        for (Debt d : newDebts) {
            entityManager.persist(d);
        }

        // create notification ASYNC
        createAndSendNotifs(NotificationType.EXPENSE_DELETED, user, notifyUsers, group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + group.getId(), "EXPENSE", NotificationType.EXPENSE_DELETED);
        return "{\"action\": \"redirect\",\"redirect\": \"/group/" + groupId + "\"}";
    }

    /*
     * Settle debt (create negative expense)
     */
    @PostMapping("/settle")
    @Transactional
    @ResponseBody
    public String settleDebt(HttpSession session, @PathVariable long groupId, @RequestBody JsonNode jsonNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        long debtorId = objectMapper.convertValue(jsonNode.get("debtorId"), Long.class);
        long debtOwnerId = objectMapper.convertValue(jsonNode.get("debtOwnerId"), Long.class);
        float amount = objectMapper.convertValue(jsonNode.get("amount"), Float.class);

        User user = (User) session.getAttribute("u");
        user = entityManager.find(User.class, user.getId());

        // check if group exists
        Group group = groupAccessUtilities.getGroupOrThrow(groupId);

        // check if user belongs to the group
        groupAccessUtilities.getMemberOrThrow(groupId, user.getId());

        // Check if debt exists
        Debt debt = entityManager.find(Debt.class, new DebtID(groupId, debtorId, debtOwnerId));
        if (debt == null)
            throw new ForbiddenException(ErrorType.E_SETTLE_FORBIDDEN);
        User debtor = debt.getDebtor();
        User debtOwner = debt.getDebtOwner();

        Expense exp = new Expense("Reimbursement", debtor.getUsername() + " settled with " + debtOwner.getUsername(), amount, LocalDate.now(), entityManager.find(Type.class, Long.valueOf(8)), debtor);
        entityManager.persist(exp);
        entityManager.flush();

        // Delete debt from group
        entityManager.remove(debt);

        // Remove debt from debtor
        Member debtorM = entityManager.find(Member.class, new MemberID(groupId, debtor.getId()));
        debtorM.setBalance(debtorM.getBalance() + amount);

        // Add participates to debtOwner
        ParticipatesID pId = new ParticipatesID(exp.getId(), debtOwnerId);
        Participates participates = new Participates(pId, group, debtOwner, exp);
        entityManager.persist(participates);

        // Remove balance from debtOwner
        Member debtOwnerM = entityManager.find(Member.class, new MemberID(groupId, debtOwner.getId()));
        debtOwnerM.setBalance(debtOwnerM.getBalance() - amount);

        // send notification ASYNC
        createAndSendNotifs(NotificationType.DEBT_SETTLED, user, new ArrayList<>(Arrays.asList(debtOwner, debtor)), group, exp);

        // send expense to group ASYNC
        notifSender.sendTransfer(exp, "/topic/group/" + groupId, "EXPENSE", NotificationType.EXPENSE_CREATED);

        return "{\"success\": \"ok\"}";
    }
}