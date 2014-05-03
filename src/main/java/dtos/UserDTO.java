package dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import validators.annotations.FieldMatch;
import validators.annotations.ValidEmail;
import validators.annotations.ValidUsername;

@FieldMatch.List({
    @FieldMatch(first = "userpass", second = "userpassConfirmation", message = "The password fields must match"),
    @FieldMatch(first = "email", second = "emailConfirmation", message = "The email fields must match")
})
public class UserDTO {
    @NotNull
    @Size(min = 3, max = 32)
    @ValidUsername
    public String username;

    @NotNull
    @Size(min = 8, max = 32)
    public String userpass;

    @NotNull
    @Size(min = 8, max = 32)
    public String userpassConfirmation;

    @NotNull
    @Email
    @ValidEmail
    public String email;

    @NotNull
    @Email
    public String emailConfirmation;
}