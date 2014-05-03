package dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

/**
 * 
 * @author svenkubiak
 *
 */
public class UserDTO {
    @NotNull
    @Size(min = 3, max = 32)
    public String username;

    @NotNull
    @Size(min = 8, max = 32)
    public String userpass;

    @NotNull
    @Size(min = 8, max = 32)
    public String userpassConfirmation;

    @NotNull
    @Email
    public String email;

    @NotNull
    @Email
    public String emailConfirmation;
}