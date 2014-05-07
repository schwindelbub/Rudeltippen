package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Confirmation;
import models.ConfirmationType;
import models.Constants;
import models.Settings;
import models.User;
import ninja.Cookie;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.session.Session;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.AuthService;
import services.DataService;
import services.I18nService;
import services.MailService;
import services.ValidationService;
import utils.AppUtils;
import utils.ValidationUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dtos.LoginDTO;
import dtos.PasswordDTO;
import dtos.UserDTO;
import filters.LanguageFilter;
import filters.SetupFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith({LanguageFilter.class, SetupFilter.class})
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Inject
    private DataService dataService;

    @Inject
    private ValidationService validationService;

    @Inject
    private MailService mailService;

    @Inject
    private AuthService authService;

    @Inject
    private I18nService i18nService;

    public Result password(@PathParam("token") String token, FlashScope flashScope) {
        final Confirmation confirmation = dataService.findConfirmationByToken(token);
        if (confirmation == null) {
            flashScope.put("warningmessage", i18nService.get("controller.users.invalidtoken"));
            return Results.redirect("/auth/login");
        }
        
        return Results.html().render(token);
    }

    public Result login() {
        Settings settings = dataService.findSettings();
        return Results.html().render(settings);
    }

    public Result reset(@PathParam("email") String email, FlashScope flashScope) {
        if (ValidationUtils.isValidEmail(email)) {
            flashScope.put("errormessage", i18nService.get("controller.auth.resenderror"));

            return Results.redirect("/auth/forgotton");
        } else {
            final User user = dataService.findUserByEmailAndActive(email);
            if (user != null) {
                final String token = UUID.randomUUID().toString();
                final ConfirmationType confirmType = ConfirmationType.NEWUSERPASS;
                final Confirmation confirmation = new Confirmation();
                confirmation.setUser(user);
                confirmation.setToken(token);
                confirmation.setConfirmType(confirmType);
                confirmation.setConfirmValue(authService.encryptAES(UUID.randomUUID().toString()));
                confirmation.setCreated(new Date());
                dataService.save(confirmation);

                mailService.confirm(user, token, confirmType);
                flashScope.put("infomessage", i18nService.get("confirm.message"));

                return Results.redirect("/auth/login");
            }
        }
        return Results.redirect("/");
    }

    public Result confirm(@PathParam("token") String token, FlashScope flashScope, Session session) {
        Confirmation confirmation = null;

        if (!ValidationUtils.isValidConfirmationToken(token)) {
            flashScope.put("warningmessage", i18nService.get("controller.users.invalidtoken"));
        } else {
            confirmation = dataService.findConfirmationByToken(token);
        }

        if (confirmation != null) {
            final User user = confirmation.getUser();
            if (user != null) {
                final ConfirmationType confirmationType = confirmation.getConfirmType();
                if (ConfirmationType.NEWUSERPASS.equals(confirmationType)) {
                    return Results.redirect("/auth/password/" + token);
                } else {
                    if ((ConfirmationType.ACTIVATION).equals(confirmationType)) {
                        authService.activateAndSetAvatar(user);
                        flashScope.success(i18nService.get("controller.users.accountactivated"));
                        dataService.delete(confirmation);

                        LOG.info("User activated: " + user.getEmail());
                    } else if ((ConfirmationType.CHANGEUSERNAME).equals(confirmationType)) {
                        final String oldusername = user.getEmail();
                        final String newusername = authService.decryptAES(confirmation.getConfirmValue());
                        user.setEmail(newusername);
                        dataService.save(user);
                        session.remove(Constants.USERNAME.value());
                        flashScope.success(i18nService.get("controller.users.changedusername"));
                        dataService.delete(confirmation);

                        LOG.info("User changed username... old username: " + oldusername + " - " + "new username: " + newusername);
                    } else if ((ConfirmationType.CHANGEUSERPASS).equals(confirmationType)) {
                        user.setUserpass(authService.decryptAES(confirmation.getConfirmValue()));
                        dataService.save(user);
                        session.remove("username");
                        flashScope.success(i18nService.get("controller.users.changeduserpass"));
                        dataService.delete(confirmation);

                        LOG.info(user.getEmail() + " changed his password");
                    }
                }
            } else {
                flashScope.put("warningmessage", i18nService.get("controller.users.invalidtoken"));
            }
        } else {
            flashScope.put("warningmessage", i18nService.get("controller.users.invalidtoken"));
        }

        return Results.redirect("/auth/login");
    }

    public Result register() {
        final Settings settings = dataService.findSettings();
        if (!settings.isEnableRegistration()) {
            return Results.redirect("/");
        }

        return Results.html();
    }

    public Result create(@JSR303Validation UserDTO userDTO, Validation validation) {
        final Settings settings = dataService.findSettings();
        if (!settings.isEnableRegistration()) {
            return Results.redirect("/");
        }

        validationService.validateUserDTO(userDTO, validation);

        if (validation.hasBeanViolations()) {
            return Results.html().render("user", userDTO).render("validation", validation).template("/views/AuthController/register.ftl.html");
        } else {
            final String salt = DigestUtils.sha512Hex(UUID.randomUUID().toString());
            final User user = new User();
            user.setRegistered(new Date());
            user.setUsername(userDTO.username);
            user.setEmail(userDTO.email);
            user.setActive(false);
            user.setReminder(true);
            user.setSendStandings(true);
            user.setSendGameTips(true);
            user.setNotification(true);
            user.setAdmin(false);
            user.setSalt(salt);
            user.setUserpass(AppUtils.hashPassword(userDTO.userpass, salt));
            user.setPoints(0);
            dataService.save(user);

            final String token = UUID.randomUUID().toString();
            final ConfirmationType confirmationType = ConfirmationType.ACTIVATION;
            final Confirmation confirmation = new Confirmation();
            confirmation.setConfirmType(confirmationType);
            confirmation.setConfirmValue(authService.encryptAES(UUID.randomUUID().toString()));
            confirmation.setCreated(new Date());
            confirmation.setToken(token);
            confirmation.setUser(user);
            dataService.save(confirmation);

            mailService.confirm(user, token, confirmationType);
            if (settings.isInformOnNewTipper()) {
                final List<User> admins = dataService.findAllAdmins();
                for (final User admin : admins) {
                    mailService.newuser(user, admin);
                }
            }
            LOG.info("User registered: " + user.getEmail());
        }

        return Results.html().render(settings);
    }

    public Result forgotten() {
        return Results.html();
    }

    public Result renew(@JSR303Validation PasswordDTO passwordDTO, Validation validation, FlashScope flashScope) {
        if (validation.hasBeanViolations()) {
            return Results.html().render("passwordDTO", passwordDTO).render("validation", validation).template("/views/AuthController/rendew.ftl.html");
        }

        final Confirmation confirmation = dataService.findConfirmationByToken(passwordDTO.token);
        if (confirmation == null) {
            flashScope.put("warningmessage", i18nService.get("controller.users.invalidtoken"));
            return Results.redirect("/auth/login");
        }

        final User user = confirmation.getUser();
        final String password = AppUtils.hashPassword(passwordDTO.userpass, user.getSalt());
        user.setUserpass(password);
        dataService.delete(user);

        dataService.delete(confirmation);
        flashScope.put("infomessage", i18nService.get("controller.auth.passwordreset"));

        return Results.redirect("/auth/login");
    }

    public Result authenticate(Session session, @JSR303Validation LoginDTO loginDTO, Validation validation, FlashScope flashScope) {
        if (validation.hasBeanViolations()) {
            return Results.html().render("login", loginDTO).render("validation", validation).template("/views/AuthController/login.ftl.html");
        } else {
            if (authService.authenticate(loginDTO.username, loginDTO.userpass)) {
                session.put(Constants.USERNAME.value(), loginDTO.username);
                if (loginDTO.remember) {
                    String signedUsername = authService.sign(loginDTO.username) + "-" + loginDTO.username;
                    Cookie.builder("rememberme", signedUsername).setSecure(true).setHttpOnly(true).build();
                }

                return Results.redirect("/");
            }
        }

        return Results.redirect("/auth/login");
    }

    public Result logout(Session session, FlashScope flashScope){
        session.clear();
        flashScope.put("infomessage", i18nService.get("controller.auth.logout"));

        return Results.redirect("/auth/login");
    }
}