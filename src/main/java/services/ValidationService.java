package services;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Confirmation;
import models.Constants;
import models.User;

import com.google.inject.Inject;

public class ValidationService {

    @Inject
    private DataService dataService;

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
            //TODO Refactoring
            value = "";//Crypto.decryptAES(value);

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