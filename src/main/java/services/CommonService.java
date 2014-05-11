package services;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import models.Bracket;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Playday;
import models.Team;
import models.User;
import models.enums.Constants;
import models.pagination.Pagination;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Singleton
public class CommonService extends ViewService {
    private static final Logger LOG = LoggerFactory.getLogger(CommonService.class);

    @Inject
    private I18nService i18nService;

    @Inject
    private DataService dataService;

    /**
     * Checks if all games in given list have ended
     *
     * @param games The games to check
     * @return true if all games have ended, false otherweise
     */
    public boolean allReferencedGamesEnded(final List<Game> games) {
        boolean ended = true;
        if (games != null && !games.isEmpty()) {
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
    public String getGravatarImage(final String email, final String type, int size) {
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
            final File file = new File(filename);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = response.getEntity().getContent();
                outputStream = new FileOutputStream(file);

                final byte byteBuffer[] = new byte[1024];
                int length;
                while ((length = inputStream.read(byteBuffer)) > 0) {
                    outputStream.write(byteBuffer, 0, length);
                }

                FileUtils.copyFile(file, new File(Constants.MEDIAFOLDER.value() + filename));
                if (!file.delete()) {
                    LOG.error("Failed to delete image when handling response from gravatar");
                }
            } catch (final Exception e) {
                LOG.error("Failed to get and convert gravatar image", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LOG.error("Failed to close inputstream while getting gravatar image", e);
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        LOG.error("Failed to close outputstream while getting gravatar image", e);
                    }
                }
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
    public Pagination getPagination(long number, final String url, long totalPlaydays) {
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

    public void resizeImage(File sourceImage, File targetImage, int width, int height, boolean keepRatio) {
        BufferedImage source = null;
        try {
            source = ImageIO.read(sourceImage);
        } catch (IOException e) {
            LOG.error("IOException while trying to create source image for resizing", e);
        }

        if (source != null) {
            int originalWidth = source.getWidth();
            int originalHeight = source.getHeight();
            double ratio = (double) originalWidth / originalHeight;

            int maxWidth = width;
            int maxHeight = height;

            if (width < 0 && height < 0) {
                width = originalWidth;
                height = originalHeight;
            }
            if (width < 0 && height > 0) {
                width = (int) (height * ratio);
            }
            if (width > 0 && height < 0) {
                height = (int) (width / ratio);
            }

            if(keepRatio) {
                height = (int) (width / ratio);
                if(height > maxHeight) {
                    height = maxHeight;
                    width = (int) (height * ratio);
                }
                if(width > maxWidth) {
                    width = maxWidth;
                    height = (int) (width / ratio);
                }
            }

            String mimeType = "image/jpeg";
            if (targetImage.getName().endsWith(".png")) {
                mimeType = "image/png";
            }
            if (targetImage.getName().endsWith(".gif")) {
                mimeType = "image/gif";
            }

            BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image srcSized = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);

            Graphics graphics = dest.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcSized, 0, 0, null);

            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            ImageWriteParam params = writer.getDefaultWriteParam();

            FileImageOutputStream fileImageOutputStream = null;
            try {
                fileImageOutputStream = new FileImageOutputStream(targetImage);
            } catch (IOException e) {
                LOG.error("Failed to create outputstream for resizing image", e);
            }

            if (fileImageOutputStream != null) {
                writer.setOutput(fileImageOutputStream);
                IIOImage image = new IIOImage(dest, null, null);

                try {
                    writer.write(null, image, params);
                } catch (IOException e) {
                    LOG.error("Failed to write outputstream for resizing image", e);
                }

                try {
                    fileImageOutputStream.flush();
                } catch (IOException e) {
                    LOG.error("Failed to flush outputstream for resizing image", e);
                }

                try {
                    fileImageOutputStream.close();
                } catch (IOException e) {
                    LOG.error("Failed to close outputstream for resizing image", e);
                }
                writer.dispose();
            }
        }
    }

    public Map<String, String> convertParamaters(Map<String, String[]> parameters) {
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

    public String getProcessedTemplate(String name, Map<String, Object> content) {
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

    public boolean extraIsTipable(Extra extra) {
        if (extra.getEnding() != null && (new Date().getTime() >= extra.getEnding().getTime())) {
            return false;
        }

        return true;
    }

    public String homeReferenceName (final Game game) {
        return getReference(game.getHomeReference());
    }

    public String awayReferenceName (final Game game) {
        return getReference(game.getAwayReference());
    }

    public String getPoints(final Game game, User user) {
        String points = "-";
        final List<GameTip> gameTips = game.getGameTips();

        for (final GameTip gameTip : gameTips) {
            if (gameTip.getGame().isEnded() && gameTip.getUser().equals(user)) {
                points = String.valueOf(gameTip.getPoints());
                break;
            }
        }

        return points;
    }

    private String getReference(final String reference) {
        final String [] references = reference.split("-");
        String message = "";
        if (("G").equals(references[0])) {
            if ("W".equals(references[2])) {
                message = i18nService.get("model.game.winnergame") + " " + references[1];
            } else if (("L").equals(references[2])) {
                message = i18nService.get("model.game.losergame") + " " + references[1];
            }
        } else if (("B").equals(references[0])) {
            final Bracket bracket = dataService.findBracketByNumber(references[1]);
            final String groupName = bracket.getName();
            final String placeName = getPlaceName(Integer.parseInt(references[2]));

            message = placeName + " " + i18nService.get(groupName);
        }

        return message;
    }

    public String getPlaceName(final int place) {
        String message = "";

        if (place == 1) {
            message = i18nService.get("helper.first");
        } else if (place == 2){
            message = i18nService.get("helper.second");
        } else if (place == 3){
            message = i18nService.get("helper.third");
        } else if (place == 4){
            message = i18nService.get("helper.fourth");
        } else if (place == 5){
            message = i18nService.get("helper.fifth");
        } else if (place == 6){
            message = i18nService.get("helper.six");
        } else if (place == 7){
            message = i18nService.get("helper.seventh");
        } else if (place == 8){
            message = i18nService.get("helper.eight");
        } else if (place == 9){
            message = i18nService.get("helper.ninth");
        } else if (place == 10){
            message = i18nService.get("helper.tenth");
        }

        return message;
    }

    public String getExtraTip(final Extra extra, User user) {
        final ExtraTip extraTip = dataService.findExtraTipByExtraAndUser(extra, user);
        String id = null;

        if ((extraTip != null) && (extraTip.getAnswer() != null)) {
            id = extraTip.getAnswer().getId().toString();
        }

        return id;
    }

    public String getAnswer(final Extra extra, User user) {
        final ExtraTip extraTip = dataService.findExtraTipByExtraAndUser(extra, user);
        String answer = "";

        if ((extraTip != null) && (extraTip.getAnswer() != null)) {
            answer = i18nService.get(extraTip.getAnswer().getName());
        }

        return answer;
    }

    public String getExtraTipAnswer(final ExtraTip extraTip) {
        String answer = "-";
        if (extraTip.getAnswer() != null) {
            if (extraTip.getExtra().getEnding().getTime() < new Date().getTime()) {
                answer = i18nService.get(extraTip.getAnswer().getName());
            } else {
                answer = i18nService.get("model.user.tipped");
            }
        }

        return answer;
    }

    public String getExtraTipPoints(final ExtraTip extraTip) {
        String points = "";
        if ((extraTip != null) && (extraTip.getExtra() != null) && (extraTip.getExtra().getAnswer() != null)) {
            points = " ("+ extraTip.getPoints() + ")";
        }

        return points;
    }

    public String htmlUnescape(final String html) {
        return StringEscapeUtils.unescapeHtml(html);
    }

    public boolean allGamesEnded(Playday playday) {
        for (final Game game : playday.getGames()) {
            if (!game.isEnded()) {
                return false;
            }
        }
        return true;
    }

    public boolean allGamesEnded(Bracket bracket) {
        for (final Game game : bracket.getGames()) {
            if (!game.isEnded()) {
                return false;
            }
        }

        return true;
    }

    public Team getTeamByPlace(final int place, Bracket bracket) {
        int i = 1;
        for (final Team team : bracket.getTeams()) {
            if  (i == place) {
                return team;
            }
            i++;
        }

        return null;
    }

    public Team getWinner(Game game) {
        String home, away;
        if (game.isOvertime()) {
            home = game.getHomeScoreOT();
            away = game.getAwayScoreOT();
        } else {
            home = game.getHomeScore();
            away = game.getAwayScore();
        }

        if (StringUtils.isNotBlank(home) && StringUtils.isNotBlank(away)) {
            final int homeScore = Integer.parseInt(home);
            final int awayScore = Integer.parseInt(away);
            if (homeScore > awayScore) {
                return game.getHomeTeam();
            } else {
                return game.getAwayTeam();
            }
        }

        return null;
    }

    public Team getLoser(Game game) {
        String home, away;
        if (game.isOvertime()) {
            home = game.getHomeScoreOT();
            away = game.getAwayScoreOT();
        } else {
            home = game.getHomeScore();
            away = game.getAwayScore();
        }

        if (StringUtils.isNotBlank(home) && StringUtils.isNotBlank(away)) {
            final int homeScore = Integer.parseInt(home);
            final int awayScore = Integer.parseInt(away);
            if (homeScore > awayScore) {
                return game.getHomeTeam();
            } else {
                return game.getAwayTeam();
            }
        }

        return null;
    }

    /**
     * Checks if at least one extra tip in given list is tipable
     *
     * @param extras List of extra tips
     * @return true if at least one extra tip is tipable, false otherwise
     */
    public boolean extrasAreTipable(final List<Extra> extras) {
        boolean tippable = false;
        for (final Extra extra : extras) {
            if (extraIsTipable(extra)) {
                tippable = true;
                break;
            }
        }

        return tippable;
    }
}