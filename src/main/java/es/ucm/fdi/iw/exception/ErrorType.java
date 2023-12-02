package es.ucm.fdi.iw.exception;

public enum ErrorType {

    // ERROR MSGS
    // BadRequestException
    E_EMPTY_FIELDS("Invalid data: Name, username and password must not be empty."),
    E_USERNAME_TAKEN("Invalid username: Username is already taken."),
    E_DIFF_PASS("Passwords don't match."),
    E_USER_NE("Invalid user: User doesn't exist."),
    E_USER_NOTENABLED("Invalid user: User is not enabled."),
    E_WRONG_PASS("Wrong password."),
    E_EMPTY_NAME("Invalid data: Name must not be empty."),
    E_INVALID_BUDGET("Invalid data: Budget must be a float greater or equal to 0."),
    E_LONG_NAME("Invalid data: Name must be 100 characters or less."),
    E_LONG_DESC("Invalid data: Description must be 255 characters or less."),
    E_BALANCES_NZ("Error: A group with balances other than 0 can't be deleted."),
    E_MEMBER_BALANCE_NZ("Error: A member with a balance other than 0 can't leave the group."),
    E_ONLY_MODERATOR("Error: You can't leave the group being the only moderator."),
    E_EMPTY_USERNAME("Invalid data: Username must not be empty."),
    E_ALREADY_BELONGS("Invalid user: User already belongs to the group."),
    E_GROUP_NE("Error: The group no longer exists."),
    E_AFTER_DATE("Invalid data: Date must be on or before today."),
    E_INVALID_AMOUNT("Invalid data: Amount must be a float greater than 0."),
    E_NO_PARTICIPANTS("Invalid data: At least one member must participate in the expense."),
    E_PASS_UNCHANGED("Invalid password: New password is the same as the old one."),
    E_INVALID_CURRENCY("Invalid data: Currency must be one of the available."),
    E_INVALID_DATE("Invalid format date. Please choose from the calendar."),
    E_INVALID_TYPE("Invalid data. Type must be one of the available."),
    E_INVITED_USER("Invalid user. User already invited."),
    // ForbiddenException
    E_GROUP_FORBIDDEN("FORBIDDEN: The group doesn't exist or you are not allowed to access it."),
    E_DELETE_FORBIDDEN("FORBIDDEN: You can't delete a group unless you are a moderator."),
    E_DELMEMBER_FORBIDDEN("FORBIDDEN: You can't remove other members unless you are a moderator."),
    E_MAKEMODERATOR_FORBIDDEN("FORBIDDEN: You can't make other members moderators unless you are a moderator."),
    E_INVITE_FORBIDDEN("FORBIDDEN: You can't invite a member unless you are the moderator."),
    E_EXPENSE_FORBIDDEN("FORBIDDEN: The expense doesn't exist or you are not allowed to access it."),
    E_USER_FORBIDDEN("FORBIDDEN: User doesn't exist or doesn't belong to the group."),
    E_INVITATION_FORBIDDEN("FORBIDDEN: Group doesn't exist or you are not invited to it."),
    E_NOTIF_FORBIDDEN("FORBIDDEN: Notification doesn't exist or wasn't sent to you."),
    E_SETTLE_FORBIDDEN("FORBIDDEN: Unabled to settle debt."),
    E_PROFILE_FORBIDDEN("FORBIDDEN: This profile doesn't exist or you are not allowd to access it."),
    E_ALREADYMODERATOR_FORBIDDEN("FORBIDDEN: Member was already a moderator."),
    // Other
    E_INTERNAL_SERVER("Error: Bad data in DB."),
    E_UNKNOWN("Unknown error.");

    // CONSTRUCTOR
    private final String errorMessage;

    ErrorType(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // GETTER
    public String getErrorMessage() {
        return errorMessage;
    }

}
