package utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import models.Constants;
import models.Game;
import models.Pagination;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

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
     * @return SHA512 hashed string
     */
    public static String hashPassword(final String userpass, final String usersalt) {
        String hash = "";
        for (int i = 1; i <= 100000; i++) {
            hash = DigestUtils.sha512Hex(hash + userpass + usersalt);
        }

        return hash;
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
     * Returns the filename from a Gravatar image
     * 
     * @param email The email adress to check
     * @param type Return a default image if no email is available
     * @return The image filename
     */
    public static String getGravatarImage(final String email, final String type, int size) {
        if ((size <= 0) || (size > 128)) {
            size = 64;
        }

        String url = "https://secure.gravatar.com/avatar/" + DigestUtils.md5Hex(email) + ".jpg?s=" + size + "&r=pg&d=" + type;
        HttpResponse response = null;
        try {
            response = Request.Get(url).execute().returnResponse();
        } catch (IOException e) {
            LOG.error("Failed to get response from gravator", e);
        }

        final String filename = UUID.randomUUID().toString();
        if (response != null && response.getStatusLine().getStatusCode() == 200) {
            try {
                final File file = new File(filename);
                final InputStream inputStream = response.getEntity().getContent();
                final OutputStream out = new FileOutputStream(file);
                final byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                inputStream.close();
                
                FileUtils.copyFile(file, new File(Constants.MEDIAFOLDER.value() + filename));
                file.delete();
            } catch (final Exception e) {
                LOG.error("Failed to get and convert gravatar image. " + e);
            }
        }
        
        return filename;
    }

    /**
     * Calculates the pagination 
     * 
     * @param number The number of pages
     * @param url The urls to set to the links
     * @param totalPlaydays The total number of playdays
     * @return Pagination object conatining the ready computed pagination
     */
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
    
    public static BufferedImage resizeImage(File file, int width, int height){
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(file);
        } catch (IOException e) {
            LOG.error("Failed to read image for resizing", e);
        }
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, width, height, null);
        graphics2D.dispose();
     
        return resizedImage;
    }
    
    public static Map<String, String> convertParamaters(Map<String, String[]> parameters) {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<Map.Entry<String, String[]>> entries = parameters.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String[]> entry = entries.next();
            
            if (entry.getValue() != null && entry.getValue().length > 0) {
                map.put(entry.getKey(), entry.getValue()[0]);  
            }
        }
        
        return map;
    }
    
    public static String getProcessedTemplate(String name, Map<String, Object> content) {
        Writer writer = new StringWriter(); 
        Configuration configuration = new Configuration();
        try {
            Template template = configuration.getTemplate(name);
            template.process(content, writer); 
        } catch (Exception e) {
            LOG.error("Failed to create template for: " + name, e);
        }
        
        return writer.toString();
    }
}