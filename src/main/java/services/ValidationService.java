package services;

import java.util.List;

import models.Confirmation;
import models.User;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
}