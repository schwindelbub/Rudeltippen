package dtos;

import javax.validation.constraints.NotNull;

public class LoginDTO {
    @NotNull
    public String username;

    @NotNull
    public String userpass;

    public boolean remember;
}
