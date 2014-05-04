package services;

import java.util.List;

import ninja.validation.Validation;
import utils.ValidationUtils;
import models.Confirmation;
import models.User;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dtos.UserDTO;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ValidationService {

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
        if (!ValidationUtils.isValidUsername(userDTO.username)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("username", i18nService.get("validation.username.size")));
        }

        if (usernameExists(userDTO.username)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("username", i18nService.get("validation.username.exists")));
        }

        if (!ValidationUtils.isValidEmail(userDTO.email)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("email", i18nService.get("validation.email.invalid")));
        }

        if (emailExists(userDTO.email)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("email", i18nService.get("validation.email.exsits")));
        }

        if (!ValidationUtils.match(userDTO.email, userDTO.emailConfirmation)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("email", i18nService.get("validation.email.notmatch")));
        }

        if (!ValidationUtils.isValidPassword(userDTO.userpass)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("userpass", i18nService.get("validation.password.invalid")));
        }

        if (!ValidationUtils.match(userDTO.userpass, userDTO.userpassConfirmation)) {
            validation.addBeanViolation(ValidationUtils.createBeanValidation("userpass", i18nService.get("validation.password.notmatch")));
        }
    }
}