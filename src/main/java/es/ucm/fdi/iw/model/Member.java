package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Comparator;

import javax.persistence.*;

/**
 * A relationship Group<>User with the user Role
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@NamedQueries({
    @NamedQuery(name = "Member.getGroupAdmins", query = "SELECT obj FROM Member obj WHERE group.id = :groupId AND role = 1"),
})
@Table(name="IWMember")
public class Member implements Transferable<Member.Transfer>, Comparator<Member> {

    public enum GroupRole {
        GROUP_USER,			    // normal users 
        GROUP_MODERATOR,        // admin users
    }

    @EmbeddedId private MemberID mId;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private GroupRole role;

    @Column(nullable = false)
    private float budget;

    @Column(nullable = false)
    private float balance;

	@ManyToOne
    @MapsId("groupID")
    private Group group;

    @ManyToOne
    @MapsId("userID")
    private User user;

    @Getter
    @Data
    @AllArgsConstructor
    public static class Transfer {
        private long idGroup;
        private long idUser;
        private String username;
        private boolean enabled;
        private GroupRole role;
        private float budget;
        private float balance;
    }

    @Override
    public Transfer toTransfer() {
        return new Transfer(group.getId(), user.getId(), user.getUsername(), enabled, role, budget, balance);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

    @Override
    public int compare(Member m1, Member m2) {
        return m1.getUser().getName().compareTo(m2.getUser().getName());
    }
}