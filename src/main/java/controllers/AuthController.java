package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Confirmation;
import models.ConfirmationType;
import models.Constants;
import models.Settings;
import models.User;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.session.Session;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.AuthService;
import services.DataService;
import services.I18nService;
import services.MailService;
import utils.AppUtils;
import utils.ValidationUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Inject
    private DataService dataService;

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

    public Result confirm(@PathParam("token") String token, FlashScope flashScope) {
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
                    authService.confirmationUser(confirmation, user, confirmationType);
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

    public Result create(final String username, final String email, final String emailConfirmation, final String userpass, final String userpassConfirmation) {
        final Settings settings = dataService.findSettings();
        if (!settings.isEnableRegistration()) {
            return Results.redirect("/");
        }

        //        validation.required(userpass);
        //        validation.required(username);
        //        validation.email(email);
        //        validation.equals(email, emailConfirmation);
        //        validation.equals(userpass, userpassConfirmation);
        //        validation.minSize(userpass, 8);
        //        validation.maxSize(userpass, 32);
        //        validation.minSize(username, 3);
        //        validation.maxSize(username, 20);
        //        validation.isTrue(ValidationService.isValidUsername(username)).key("username").message(Messages.get("controller.users.invalidusername"));
        //        validation.isTrue(!ValidationService.usernameExists(username)).key("username").message(Messages.get("controller.users.usernamexists"));
        //        validation.isTrue(!ValidationService.emailExists(email)).key("email").message(Messages.get("controller.users.emailexists"));

        //TODO Refactoring - was validation.hasErrors()
        if (true) {
            //            params.flash();
            //            validation.keep();
            return Results.redirect("/auth/register");
        } else {
            final String salt = DigestUtils.sha512Hex(UUID.randomUUID().toString());
            final User user = new User();
            user.setRegistered(new Date());
            user.setUsername(username);
            user.setEmail(email);
            user.setActive(false);
            user.setReminder(true);
            user.setSendStandings(true);
            user.setSendGameTips(true);
            user.setNotification(true);
            user.setAdmin(false);
            user.setSalt(salt);
            user.setUserpass(AppUtils.hashPassword(userpass, salt));
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

    public Result login(Session session) {
        if (session.get(Constants.USERNAME.value()) != null) {
            return Results.redirect("/");
        }

        //        final Http.Cookie remember = request.cookies.get("rememberme");
        //        if ((remember != null) && (remember.value.indexOf("-") > 0)) {
        //            final String sign = remember.value.substring(0, remember.value.indexOf("-"));
        //            final String username = remember.value.substring(remember.value.indexOf("-") + 1);
        //            if ((sign != null) && (username != null) && Crypto.sign(username).equals(sign)) {
        //                session.put("username", username);
        //                redirectToOriginalURL();
        //            }
        //        }
        //        flash.keep("url");

        return Results.html();
    }

    public Result forgotten() {
        return Results.html();
    }

    public Result renew(final String token, final String userpass, final String userpassConfirmation) {
        //        validation.required(token);
        //        validation.match(token, CONFIRMATIONPATTERN);
        //        validation.required(userpass);
        //        validation.equals(userpass, userpassConfirmation);
        //        validation.minSize(userpass, 8);
        //        validation.maxSize(userpass, 32);

        final Confirmation confirmation = dataService.findConfirmationByToken(token);
        if (confirmation == null) {
            //flash.put("warningmessage", Messages.get("controller.users.invalidtoken"));
            //flash.keep();

            return Results.redirect("/auth/login");
        }

        //Refactoring - was validation.hasErrors()
        if (true) {
            //Validation.keep();
            return Results.redirect("/auth/password/" + token);
        } else {
            final User user = confirmation.getUser();
            final String password = AppUtils.hashPassword(userpass, user.getSalt());
            user.setUserpass(password);
            dataService.delete(user);

            dataService.delete(confirmation);
            //flash.put("infomessage", Messages.get("controller.auth.passwordreset"));
            //flash.keep();

            return Results.redirect("/auth/login");
        }
    }

    public Result authenticate(Session session, final String username, final String userpass, final boolean remember) {
        Boolean allowed = false;
        try {
            //            allowed = (Boolean) Security.invoke("authenticate", username, userpass);
            //            validation.isTrue(allowed);
            //            validation.required(username);
            //            validation.required(userpass);
        } catch (final UnsupportedOperationException e) {
            LOG.error("UnsupportedOperationException while authenticating", e);
        } catch (final Throwable e) {
            LOG.error("Authentication exception", e);
        }

        //TODO Refactoring - was validation.hasErrors()
        if (!allowed) {
            //            flash.keep("url");
            //            flash.put("errormessage", Messages.get("validation.invalidLogin"));
            //            params.flash();
            //            Validation.keep();

            return Results.redirect("/auth/login");
        } else {
            session.put(Constants.USERNAME.value(), username);
            if (remember) {
                //response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "7d");
            }
        }

        return Results.redirect("/");
    }

    public Result logout(Session session){
        session.clear();

        //flash.put("infomessage", Messages.get("controller.auth.logout"));
        //flash.keep();

        return Results.redirect("/auth/login");
    }
}