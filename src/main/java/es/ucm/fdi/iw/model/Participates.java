package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.*;

/**
 * A relationship Group<>Expense<>User
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@NamedQueries({
    @NamedQuery(name = "Participates.getParticipants", query = "SELECT obj FROM Participates obj WHERE obj.group.id = :groupId AND obj.expense.id = :expenseId"),
    @NamedQuery(name = "Participates.getUniqueExpensesByGroup", query = "SELECT DISTINCT obj.expense FROM Participates obj WHERE obj.group.id = :groupId ORDER BY obj.expense.date")
})
@Table(name="IWParticipates")
public class Participates {

    @EmbeddedId private ParticipatesID pId;

    @ManyToOne
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @ManyToOne
    @MapsId("expenseID")
    private Expense expense;

}

