package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="IWDebt")
public class Debt implements Transferable<Debt.Transfer>{
    
    @EmbeddedId private DebtID dId;

    @Column(nullable = false)
    private float amount;

    @ManyToOne
    @MapsId("groupId")
    private Group group;

    @ManyToOne
    @MapsId("debtorId")
    private User debtor;

    @ManyToOne
    @MapsId("debtOwnerId")
    private User debtOwner;

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        public long idGroup;
        public long idDebtor;
        public long idDebtOwner;
        public String debtorName;
        public String debtOwnerName;
        public float amount;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(group.getId(), debtor.getId(), debtOwner.getId(), debtor.getUsername(), debtOwner.getUsername(), amount);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

}
