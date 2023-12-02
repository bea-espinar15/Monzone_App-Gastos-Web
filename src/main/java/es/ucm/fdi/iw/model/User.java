package es.ucm.fdi.iw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An authorized user of the system.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name="User.byUsername",
                query="SELECT u FROM User u "
                        + "WHERE u.username = :username"),
        @NamedQuery(name="User.hasUsername",
                query="SELECT COUNT(u) "
                        + "FROM User u "
                        + "WHERE u.username = :username"),
        @NamedQuery(name = "User.getAllUsers", query = "SELECT obj FROM User obj"),
        @NamedQuery(name = "User.getUserIdsLike", query = "SELECT obj.id FROM User obj WHERE CAST(obj.id AS string) = :request OR LOWER(obj.username) LIKE CONCAT('%', LOWER(:request), '%')")
})
@Table(name="IWUser")
public class User implements Transferable<User.Transfer> {

    public enum Role {
        USER,			// normal users 
        ADMIN,          // admin users
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roles; // split by ',' to separate roles

    @OneToMany(fetch= FetchType.EAGER, mappedBy = "user")
    private List<Member> memberOf = new ArrayList<>();

    @OneToMany(mappedBy = "recipient")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Participates> expenses = new ArrayList<>();

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.roles = "USER";
    }

    /**
     * Checks whether this user has a given role.
     * @param role to check
     * @return true if this user has that role.
     */
    public boolean hasRole(Role role) {
        String roleName = role.name();
        return Arrays.asList(roles.split(",")).contains(roleName);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Transfer {
		private long id;
        private boolean enabled;
        private String username;
        private String name;
    }

	@Override
    public Transfer toTransfer() {
		return new Transfer(id, enabled, username, name);
	}
	
	@Override
	public String toString() {
		return toTransfer().toString();
	}

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof User))
            return false;

        return id == ((User) o).getId();
    }

    public void addMemberOf(Member m){
        this.memberOf.add(m);
    }
}

