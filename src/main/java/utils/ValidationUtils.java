package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Constants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author svenkubiak
 *
 */
public class ValidationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtils.class);

    /**
     * Checks if the given filesize is lower or equal than configured in application.conf
     * 
     * @param filesize The filesize to check
     * @return true if filesiize is lower or equal given filesize, false otherwise
     */
    public static boolean checkFileLength(final Long filesize) {
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
    public static boolean isValidScore(String homeScore, String awayScore) {
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
    public static boolean isValidEmail(final String email) {
        final Pattern p = Pattern.compile(Constants.EMAILPATTERN.value());
        final Matcher m = p.matcher(email);

        return m.matches();
    }

    /**
     * Checks if a given username is matching defined USERNAMEPATTERN
     * 
     * @param username The username to check
     * @return true if username is valid, false otherwise
     */
    public static boolean isValidUsername(final String username) {
        final Pattern p = Pattern.compile(Constants.USERNAMEPATTERN.value());
        final Matcher m = p.matcher(username);

        return m.matches();
    }

    public static boolean isValidConfirmationToken(String token) {
        final Pattern p = Pattern.compile(Constants.CONFIRMATIONPATTERN.value());
        final Matcher m = p.matcher(token);

        return m.matches();
    }
}
