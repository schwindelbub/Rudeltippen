package dtos;

import javax.validation.constraints.Pattern;

/**
 * 
 * @author svenkubiak
 *
 */
public class PasswordDTO {
    @Pattern(regexp = "\\w{8,8}-\\w{4,4}-\\w{4,4}-\\w{4,4}-\\w{12,12}")
    public String token;
    public String userpass;
    public String userpassConfirmation;
}
