package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A group of shared expenses
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Group.getAllGroups", query = "SELECT obj FROM Group obj"),
        @NamedQuery(name = "Group.getGroupIdsLike", query = "SELECT obj.id FROM Group obj WHERE obj.id = :groupId")
})
@Table(name = "IWGroup")
public class Group implements Transferable<Group.Transfer>, Comparator<Group> {

    public enum Currency {
        EUR,
        USD,
        GBP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private long id;

    @Column(nullable = false)
    private boolean enabled;

    private String desc;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private int numMembers;
    
    @Column(nullable = false)
    private float totBudget;
    
    @Column(nullable = false)
    private Currency currency;

    @OneToMany(mappedBy = "group")
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Participates> owns = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Debt> debts = new ArrayList<>();

    public Group(String name, String desc, Currency currency){
        this.enabled = true;
        this.name = name;
        this.desc = desc;
        this.currency = currency;
        this.totBudget = 0;
        this.numMembers = 0;
    }

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        private long id;
        private boolean enabled;
        private String name;
        private String desc;
        private int numMembers;
        private float totBudget;
        private Currency currency;
        private String currencyString;
        private List<Member.Transfer> members;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(id, enabled, name, desc, numMembers, totBudget, currency, getCurrencyText(), members.stream().map(Transferable::toTransfer).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

    public boolean hasExpense(Expense e) {
        for (Participates p : owns)
            if (p.getExpense().getId() == e.getId())
                return true;
        return false;
    }

    public String getCurrencyText() {
        switch(currency){
            case EUR: 
                return "€";
            case USD:
                return "$";
            case GBP:
                return "£";
            default:
                return "";
        }
    }

    @Override
    public int compare(Group g1, Group g2) {
        return g1.getName().compareTo(g2.getName());
    }

  
}