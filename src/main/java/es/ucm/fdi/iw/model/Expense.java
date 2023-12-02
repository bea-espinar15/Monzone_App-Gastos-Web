package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;  

/**
 * An authorized user of the system.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name="Expense.byId",
                query="SELECT u FROM Expense u "
                        + "WHERE u.id = :id")
})
@Table(name="IWExpense")
public class Expense implements Transferable<Expense.Transfer>, Comparator<Expense> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 100)
    private String name;

    private String desc;

    @Column(nullable = false)
    private float amount;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    private Type type;

    @ManyToOne
    private User paidBy;

    @OneToMany(mappedBy = "expense")
    private List<Participates> belong;

    public Expense(String name, String desc, float amount, LocalDate date, Type type, User paidBy) {
        this.enabled = true;
        this.name = name;
        this.desc = desc;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.paidBy = paidBy;
        this.belong = new ArrayList<Participates>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transfer {
		private long expenseId;
        private boolean enabled;
        private String name;
        private String desc;
        private float amount;
        private String date;
        private long typeID;
        private long paidByID;
        private String paidByName;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id,	enabled, name, desc, amount, getDate(), type.getId(), paidBy.getId(), paidBy.getUsername());
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    public String getDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
        return date.format(format);  
    }

    @Override
    public int compare(Expense e1, Expense e2) {
        return e2.getDate().compareTo(e1.getDate());
    }

}