package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import models.Extra;
import models.Game;
import models.Pagination;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author svenkubiak
 *
 */
public class AppUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AppUtils.class);

    /**
     * Hashes a given clear-text password with a given salt using 100000 rounds
     *
     * @param userpass The password
     * @param usersalt The salt
     * @return SHA1 hashed string
     */
    public static String hashPassword(final String userpass, final String usersalt) {
        String hash = "";
        for (int i = 1; i <= 100000; i++) {
            hash = DigestUtils.sha512Hex(hash + userpass + usersalt);
        }

        return hash;
    }

    /**
     * Checks if at least one extra tip in given list is tipable
     *
     * @param extras List of extra tips
     * @return true if at least one extra tip is tipable, false otherwise
     */
    public static boolean extrasTipable(final List<Extra> extras) {
        boolean tippable = false;
        for (final Extra extra : extras) {
            if (extra.isTipable()) {
                tippable = true;
                break;
            }
        }

        return tippable;
    }

    /**
     * Checks if all games in given list have ended
     *
     * @param games The games to check
     * @return true if all games have ended, false otherweise
     */
    public static boolean allReferencedGamesEnded(final List<Game> games) {
        boolean ended = true;
        if ((games != null) && (games.size() > 0)) {
            for (final Game game : games) {
                if (!game.isEnded()) {
                    ended = false;
                    break;
                }
            }
        }

        return ended;
    }

    /**
     * Returns a Base64 encoded Image from Gravatar if available
     * 
     * @param email The email adress to check
     * @param d Return a default image if no email is available
     * @return Base64 encoded Image, null if no image on gravatar exists
     */
    public static String getGravatarImage(final String email, final String d, int size) {
        String image = null;

        if (ValidationUtils.isValidEmail(email)) {
            if ((size <= 0) || (size > 128)) {
                size = 64;
            }

            String url = null;
            if (StringUtils.isNotBlank(d)) {
                url = "https://secure.gravatar.com/avatar/" + DigestUtils.md5Hex(email) + ".jpg?s=" + size + "&r=pg&d=" + d;
            } else {
                url = "https://secure.gravatar.com/avatar/" + DigestUtils.md5Hex(email) + ".jpg?s=" + size + "&r=pg";
            }

            HttpResponse response = null;
            try {
                response = Request.Get(url).execute().returnResponse();
            } catch (IOException e) {
                LOG.error("Failed to get response from gravator", e);
            }

            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                try {
                    final File file = new File(UUID.randomUUID().toString());
                    final InputStream inputStream = response.getEntity().getContent();
                    final OutputStream out = new FileOutputStream(file);
                    final byte buf[] = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    inputStream.close();

                    //TODO Refactoring
                    //image = Images.toBase64(file);
                    file.delete();
                } catch (final Exception e) {
                    LOG.error("Failed to get and convert gravatar image. " + e);
                }
            }
        }

        return image;
    }

    public static Pagination getPagination(long number, final String url, long totalPlaydays) {
        final Pagination pagination = new Pagination();

        final long offsetEnd = totalPlaydays;
        if (number <= 0) {
            number = 1;
        } else if (number > offsetEnd) {
            number = offsetEnd;
        }

        long offsetStart = number - 3;
        long offset = number + 3;

        if (offsetStart <= 0) {
            offsetStart = 1;
        }

        if (offset > offsetEnd) {
            offset = offsetEnd;
        }

        pagination.setNumber(number);
        pagination.setOffsetStart(offsetStart);
        pagination.setOffset(offset);
        pagination.setOffsetEnd(offsetEnd);
        pagination.setUrl(url);

        return pagination;
    }
}