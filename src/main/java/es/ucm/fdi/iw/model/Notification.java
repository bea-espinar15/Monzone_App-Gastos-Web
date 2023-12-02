package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Notifications of the system.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Notification.countUnread", query = "SELECT COUNT(*) FROM Notification n WHERE n.recipient.id = :userId AND n.dateRead = null"),
        @NamedQuery(name = "Invite.byUserAndGroup", query = "SELECT n FROM Notification n WHERE recipient.id = :userId AND group.id = :groupId AND n.type = :type")
})
@Table(name = "IWNotification")
public class Notification implements Transferable<Notification.Transfer> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    protected long id;

    public enum NotificationType {
        BUDGET_WARNING,
        EXPENSE_CREATED,
        EXPENSE_MODIFIED,
        EXPENSE_DELETED,
        DEBT_SETTLED, // With "negative" expense

        GROUP_INVITATION,
        GROUP_INVITATION_ACCEPTED, // When a user accepts an invite
        GROUP_MEMBER_REMOVED,
        GROUP_MODIFIED,
        GROUP_DELETED,
        GROUP_NEW_MODERATOR // New moderator in group
    }

    @ManyToOne
    protected User sender;

    @ManyToOne
    protected User recipient;

    @ManyToOne
    protected Group group;

    @Column(nullable = false)
    protected String message;

    @Column(nullable = false)
    protected NotificationType type;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    protected LocalDateTime dateSent;

    @Column(nullable = true)
    protected LocalDateTime dateRead = null;

    public Notification(NotificationType type, User sender, User recipient, Group group) {
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;

        messageBuilder();
    }

    // Budget notification
    public Notification(NotificationType type, User recipient, Group group, String percentage) {
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.recipient = recipient;
        this.group = group;

        messageBuilder(percentage);
    }

    // Expense notifications
    public Notification(NotificationType type, User sender, User recipient, Group group, Expense e) {
        this.type = type;
        this.dateSent = LocalDateTime.now();
        this.sender = sender;
        this.recipient = recipient;
        this.group = group;

        messageBuilder(e);
    }

    private void messageBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.sender.getName());

        switch (type) {
            case GROUP_INVITATION:
                sb.append(" has invited you to join \"");
                break;
            case GROUP_MODIFIED:
                sb.append(" has modified \"");
                break;
            case GROUP_DELETED:
                sb.append(" has deleted \"");
                break;
            case GROUP_INVITATION_ACCEPTED:
                sb.append(" has joined \"");
                break;
            case GROUP_MEMBER_REMOVED:
                sb.append(" no longer belongs to \"");
                break;
            case GROUP_NEW_MODERATOR:
                sb.append(" is now a moderator in \"");
                break;
            default: {
            }
        }

        sb.append(this.group.getName());
        sb.append("\"");

        this.message = sb.toString();
    }

    // Budget message builder
    private void messageBuilder(String percentage) {
        StringBuilder sb = new StringBuilder();

        sb.append("You have used up ");
        sb.append(percentage);
        sb.append(" of your budget in \"");
        sb.append(this.group.getName());
        sb.append("\"");

        this.message = sb.toString();
    }

    // Expense message builder
    private void messageBuilder(Expense e) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.sender.getName());

        switch (type) {
            case EXPENSE_CREATED:
                sb.append(" created the expense \"");
                break;
            case EXPENSE_MODIFIED:
                sb.append(" updated the expense \"");
                break;
            case EXPENSE_DELETED:
                sb.append(" deleted the expense \"");
                break;
            case DEBT_SETTLED:
                sb.append(" settled your debt in group ");
                sb.append(this.group.getName());
                this.message = sb.toString();
                return;
            default: {
            }
        }

        sb.append(e.getName());
        sb.append("\" in group ");
        sb.append(this.group.getName());

        this.message = sb.toString();
    }

    public String getDateSent() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateSent.format(format);
    }

    @AllArgsConstructor
    @Data
    public class Transfer {
        private long id;
        private String message;
        private NotificationType type;
        private String dateRead;
        private String dateSent;
        private long idGroup;
        private long idSender;
        private long idRecipient;

        public Transfer(long id, String message, NotificationType type, String dateRead, String dateSent, long idGroup,
                long idRecipient) {
            this.id = id;
            this.message = message;
            this.type = type;
            this.dateRead = dateRead;
            this.dateSent = dateSent;
            this.idGroup = idGroup;
            this.idRecipient = idRecipient;
        }
    }

    @Override
    public Transfer toTransfer() {
        String dateReadString = "";
        if (dateRead != null)
            dateReadString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateRead);

        if (sender == null) {
            // System generated notification
            return new Transfer(id, message, type, dateReadString,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent), group.getId(), recipient.getId());
        }

        return new Transfer(id, message, type, dateReadString, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateSent),
                group.getId(), sender.getId(), recipient.getId());
    }
}