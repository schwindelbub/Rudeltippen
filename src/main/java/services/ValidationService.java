package services;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Confirmation;
import models.User;
import models.enums.Constants;
import ninja.validation.ConstraintViolation;
import ninja.validation.FieldViolation;
import ninja.validation.Validation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dtos.SettingsDTO;
import dtos.UserDTO;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ValidationService {
    private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

    @Inject
    private DataService dataService;

    @Inject
    private AuthService authService;

    @Inject
    private I18nService i18nService;

    /**
     * Checks in the database and pending confirmations if a given email already exists
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(final String email) {
        boolean exists = false;
        final List<Confirmation> confirmations = dataService.findAllConfirmation();
        for (final Confirmation confirmation : confirmations) {
            String value = confirmation.getConfirmValue();
            value = authService.decryptAES(value);

            if (value.equalsIgnoreCase(email)) {
                exists = true;
            }
        }

        if (!exists) {
            final User user = dataService.findUserByEmail(email);
            if (user != null) {
                exists = true;
            }
        }

        return exists;
    }

    /**
     * Checks in the database if a username already exists
     * 
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(final String username) {
        final User user = dataService.findUserByUsername(username);
        return user != null;
    }

    public void validateUserDTO(UserDTO userDTO, Validation validation) {
        if (!isValidUsername(userDTO.username)) {
            validation.addBeanViolation(createBeanValidation("username", i18nService.get("validation.username.size")));
        }

        if (usernameExists(userDTO.username)) {
            validation.addBeanViolation(createBeanValidation("username", i18nService.get("validation.username.exists")));
        }

        if (!isValidEmail(userDTO.email)) {
            validation.addBeanViolation(createBeanValidation("email", i18nService.get("validation.email.invalid")));
        }

        if (emailExists(userDTO.email)) {
            validation.addBeanViolation(createBeanValidation("email", i18nService.get("validation.email.exsits")));
        }

        if (!match(userDTO.email, userDTO.emailConfirmation)) {
            validation.addBeanViolation(createBeanValidation("email", i18nService.get("validation.email.notmatch")));
        }

        if (!isValidPassword(userDTO.userpass)) {
            validation.addBeanViolation(createBeanValidation("userpass", i18nService.get("validation.password.invalid")));
        }

        if (!match(userDTO.userpass, userDTO.userpassConfirmation)) {
            validation.addBeanViolation(createBeanValidation("userpass", i18nService.get("validation.password.notmatch")));
        }
    }

    public void validateSettingsDTO(SettingsDTO settingsDTO, Validation validation) {
        // TODO Auto-generated method stub
    }

    /**
     * Checks if the given filesize is lower or equal than configured in application.conf
     * 
     * @param filesize The filesize to check
     * @return true if filesiize is lower or equal given filesize, false otherwise
     */
    public boolean checkFileLength(final Long filesize) {
        boolean check = false;
        if ((filesize > 0) && (filesize <= 102400)) {
            check = true;
        }

        return check;
    }

    /**
     * Checks if given homeScore and awayScore is casteble to string and between 0 and 99
     * 
     * @param homeScore The homeScore to check
     * @param awayScore The awayScore to check
     * @return true if score is valid, false otherwise
     */
    public boolean isValidScore(String homeScore, String awayScore) {
        boolean valid = false;
        if (StringUtils.isNotBlank(homeScore) && StringUtils.isNotBlank(awayScore)) {
            homeScore = homeScore.trim();
            awayScore = awayScore.trim();
            int home, away;
            try {
                home = Integer.parseInt(homeScore);
                away = Integer.parseInt(awayScore);

                if ((home >= 0) && (home <= 99) && (away >= 0) && (away <= 99)) {
                    valid = true;
                }
            } catch (final Exception e) {
                LOG.error("Invalid score given",  e);
            }
        }

        return valid;
    }

    /**
     * Checks if a given email is matching defined EMAILPATTERN
     * 
     * @param email The email to check
     * @return true if email is valid, false otherwise
     */
    public boolean isValidEmail(final String email) {
        boolean valid = false;
        final Pattern p = Pattern.compile(Constants.EMAILPATTERN.value());
        final Matcher m = p.matcher(email);

        if (StringUtils.isNotBlank(email) && m.matches()) {
            valid = true;
        }

        return valid;
    }

    /**
     * Checks if a given username is matching defined USERNAMEPATTERN
     * 
     * @param username The username to check
     * @return true if username is valid, false otherwise
     */
    public boolean isValidUsername(final String username) {
        boolean valid = false;
        final Pattern p = Pattern.compile(Constants.USERNAMEPATTERN.value());
        final Matcher m = p.matcher(username);

        if (StringUtils.isNotBlank(username) && username.length() >= 3 && username.length() <= 32 && m.matches()) {
            valid = true;
        }

        return valid;
    }

    public boolean isValidConfirmationToken(String token) {
        final Pattern p = Pattern.compile(Constants.CONFIRMATIONPATTERN.value());
        final Matcher m = p.matcher(token);

        return m.matches();
    }

    public boolean match(String string1, String string2) {
        boolean valid = true;
        if (StringUtils.isNotBlank(string1) && !string1.equals(string2)) {
            valid = false;
        }

        return valid;
    }

    public FieldViolation createBeanValidation(String field, String message) {
        ConstraintViolation constraintViolation = new ConstraintViolation(message,"","");
        return new FieldViolation(field, constraintViolation);
    }

    public boolean isValidPassword(String userpass) {
        return StringUtils.isNotBlank(userpass) && userpass.length() >= 8 && userpass.length() <= 32;
    }
}