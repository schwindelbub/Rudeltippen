package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Constants;
import models.Game;
import models.Settings;
import models.User;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.session.Session;
import ninja.utils.NinjaProperties;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;
import services.ImportService;
import services.SetupService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class SystemController {
    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);

    @Inject
    private DataService dataService;
    
    @Inject
    private ImportService importService;
    
    @Inject
    private SetupService setupService;
    
    @Inject
    private NinjaProperties ninjaProperties;

    public Result setup() {
        if (dataService.appIsInizialized()) {
            return Results.redirect("/");
        }

        return Results.html();
    }

    public Result init(Session session, Context context) {
        if (!dataService.appIsInizialized()) {
            session.clear();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error("Failed while trying to sleep in system/init", e);
            }

            dataService.dropDatabase();
            importService.loadInitialData();

            final List<Game> prePlayoffGames = dataService.findAllNonPlayoffGames();
            final List<Game> playoffGames = dataService.findAllPlayoffGames();
            boolean hasPlayoffs = false;
            if ((playoffGames != null) && (playoffGames.size() > 0)) {
                hasPlayoffs = true;
            }

            Settings settings = new Settings();
            settings.setAppName(Constants.APPNAME.value());
            settings.setPointsGameWin(3);
            settings.setPointsGameDraw(1);
            settings.setAppSalt(DigestUtils.sha512Hex(UUID.randomUUID().toString()));
            settings.setGameName("Rudeltippen");
            settings.setPointsTip(4);
            settings.setPointsTipDiff(2);
            settings.setPointsTipTrend(1);
            settings.setMinutesBeforeTip(5);
            settings.setPlayoffs(hasPlayoffs);
            settings.setNumPrePlayoffGames(prePlayoffGames.size());
            settings.setInformOnNewTipper(true);
            settings.setEnableRegistration(true);
            dataService.save(settings);

            User user = new User();
            final String salt = DigestUtils.sha512Hex(UUID.randomUUID().toString());
            user.setSalt(salt);
            user.setEmail("admin@foo.bar");
            user.setUsername("admin");
            user.setUserpass(AppUtils.hashPassword("admin", salt));
            user.setRegistered(new Date());
            user.setExtraPoints(0);
            user.setTipPoints(0);
            user.setPoints(0);
            user.setActive(true);
            user.setAdmin(true);
            user.setReminder(true);
            user.setCorrectResults(0);
            user.setCorrectDifferences(0);
            user.setCorrectTrends(0);
            user.setCorrectExtraTips(0);
            dataService.save(user);

            return Results.ok().render(Result.NO_HTTP_BODY);
        }

        return Results.redirect("/");
    }

    public Result yamler() {
        if (("true").equals(ninjaProperties.get("rudeltippen.yamler"))) {
            final List<String> playdays = setupService.generatePlaydays(34);
            final List<String> games = setupService.getGamesFromWebService(34, "WM-2014", "2014");
            return Results.html().render("playdays", playdays).render("games", games);
        }

        return Results.redirect("/");
    }
}