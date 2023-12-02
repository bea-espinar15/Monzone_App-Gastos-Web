package es.ucm.fdi.iw;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.NoArgsConstructor;

import javax.persistence.EntityManager;

import es.ucm.fdi.iw.exception.ErrorType;
import es.ucm.fdi.iw.exception.ForbiddenException;
import es.ucm.fdi.iw.model.Group;
import es.ucm.fdi.iw.model.Member;
import es.ucm.fdi.iw.model.MemberID;

@NoArgsConstructor
public class GroupAccessUtilities {

    @Autowired
    private EntityManager entityManager;

    public Group getGroupOrThrow(long groupId) {
        // check if group exists
        Group group = entityManager.find(Group.class, groupId);
        if (group == null || !group.isEnabled())
            throw new ForbiddenException(ErrorType.E_GROUP_FORBIDDEN);

        return group;
    }

    public Member getMemberOrThrow(long groupId, long userId) {
        // check if user belongs to the group
        MemberID mId = new MemberID(groupId, userId);
        Member member = entityManager.find(Member.class, mId);
        if (member == null || !member.isEnabled())
            throw new ForbiddenException(ErrorType.E_GROUP_FORBIDDEN);

        return member;
    }
}
