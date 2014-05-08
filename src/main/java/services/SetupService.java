package services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import models.Constants;
import models.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import utils.AppUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class SetupService {
    private static final Logger LOG = LoggerFactory.getLogger(ResultService.class);

    @Inject
    private DataService dataService;

    public List<String> getGamesFromWebService(final int playdays, final String leagueShortcut, final String leagueSaison) {
        final Map<String, String> teams = getBundesligaTeams();

        int game = 1;
        final List<String> games = new ArrayList<String>();
        for (int k=1; k <= playdays; k++) {
            final Document document = getDocumentFromWebService(String.valueOf(k), leagueShortcut, leagueSaison);
            final NodeList nodeList = document.getElementsByTagName("Matchdata");
            for (int i=0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                final NodeList childs = node.getChildNodes();

                String webserviceID = null;
                String kickoff = null;
                String homeTeam = null;
                String awayTeam = null;

                for (int j=0; j < childs.getLength(); j++) {
                    final Node childNode = childs.item(j);
                    final String name = childNode.getNodeName();
                    String value = childNode.getTextContent();

                    if ("matchID".equals(name)) {
                        webserviceID = value;
                    } else if (("matchDateTimeUTC").equals(name)) {
                        value = value.replace("T", " ");
                        value = value.replace("Z", "");
                        kickoff = value;
                    } else if (("idTeam1").equals(name)) {
                        homeTeam = teams.get(value);
                    } else if (("idTeam2").equals(name)) {
                        awayTeam = teams.get(value);
                    }
                }

                games.add("models.Game(g" + game + "):<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;number:        " + game + "<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;homeTeam:      " + homeTeam + "<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;awayTeam:      " + awayTeam + "<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;kickoff:       " + kickoff + "<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;playday:       p" + k + "<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;playoff:       false<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;ended:         false<br />");
                games.add("&nbsp;&nbsp;&nbsp;&nbsp;webserviceID:  " + webserviceID + "<br />");
                games.add("<br />");
                game++;
            }
        }

        return games;
    }

    public static Map<String, String> getBundesligaTeams() {
        final Map<String, String> teams = new HashMap<String, String>();
        teams.put("7", "bvb");
        teams.put("134", "swb");
        teams.put("87", "bmg");
        teams.put("123", "tsg");
        teams.put("16", "vfb");
        teams.put("131", "vfl");
        teams.put("55", "h96");
        teams.put("9", "s04");
        teams.put("112", "scf");
        teams.put("81", "m05");
        teams.put("95", "fca");
        teams.put("185", "fd");
        teams.put("100", "hsv");
        teams.put("79", "fcn");
        teams.put("115", "sgf");
        teams.put("40", "fcb");
        teams.put("91", "ef");
        teams.put("6", "b04");
        teams.put("74", "eb");
        teams.put("54", "bsc");

        return teams;
    }

    public Document getDocumentFromWebService(final String group, final String leagueShortcut, final String leagueSaison) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
        buffer.append("<soap12:Body>");
        buffer.append("<GetMatchdataByGroupLeagueSaison xmlns=\"http://msiggi.de/Sportsdata/Webservices\">");
        buffer.append("<groupOrderID>" + group + "</groupOrderID>");
        buffer.append("<leagueShortcut>" + leagueShortcut + "</leagueShortcut>");
        buffer.append("<leagueSaison>" + leagueSaison + "</leagueSaison>");
        buffer.append("</GetMatchdataByGroupLeagueSaison>");
        buffer.append("</soap12:Body>");
        buffer.append("</soap12:Envelope>");

        Document document = null;
        try {
            HttpResponse httpResponse = Request
                    .Post(Constants.WS_URL.value())
                    .setHeader("Content-Type", Constants.WS_COTENT_TYPE.value())
                    .setHeader("charset", Constants.DEFAULT_ENCODING.value())
                    .bodyString(buffer.toString(), ContentType.TEXT_XML)
                    .execute()
                    .returnResponse();
                
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = builder.parse(httpResponse.getEntity().getContent());
        } catch (final Exception e) {
            LOG.error("Failed to get league data from webservice", e);
        }

        return document;
    }

    public List<String> generatePlaydays(final int count) {
        final List<String> playdays = new ArrayList<String>();
        for (int i=1; i <= count; i++) {
            playdays.add("models.Playday(p" + i +"):<br />");
            playdays.add("&nbsp;&nbsp;&nbsp;&nbsp;name:          " + i + "spieltag<br />");
            playdays.add("&nbsp;&nbsp;&nbsp;&nbsp;current:       false<br />");
            playdays.add("&nbsp;&nbsp;&nbsp;&nbsp;playoff:       false<br />");
            playdays.add("&nbsp;&nbsp;&nbsp;&nbsp;number:        " + i + "<br />");
            playdays.add("<br />");
        }

        return playdays;
    }

    public Date getKickoffFromDocument(final Document document) {
        Date date = new Date();
        if (document != null) {
            final NodeList nodeList = document.getElementsByTagName("matchDateTimeUTC");
            if ((nodeList != null) && (nodeList.getLength() > 0)) {
                String kickoff = nodeList.item(0).getTextContent();
                kickoff = kickoff.replace("T", " ");
                kickoff = kickoff.replace("Z", "");
                final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    date = df.parse(kickoff);
                } catch (final ParseException e) {
                    LOG.error("Failed to parse Date for kickoff update", e);
                }
            }
        }

        return date;
    }

    public void loadTestUser() {
        final String salt = "foo";
        for (int i=1; i <= 100; i++) {
            final User user = new User();
            user.setAdmin(true);
            user.setEmail("user" + i + "@rudeltippen.de");
            user.setUsername("user" + i);
            user.setRegistered(new Date());
            user.setActive(true);
            user.setSalt(salt);
            user.setUserpass(AppUtils.hashPassword("user" + i, salt));
            dataService.save(user);
        }
    }
}