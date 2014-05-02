package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Game;
import models.Settings;
import models.User;
import ninja.Result;
import ninja.Results;
import ninja.session.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SystemController {
    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);

    @Inject
    private DataService dataService;


    //TODO Refactoring
    //    @Before()
    //    protected static void before() {
    //        AppUtils.setAppLanguage();
    //    }

    public Result setup() {
        if (dataService.appIsInizialized()) {
            return Results.redirect("/");
        }

        return Results.html();
    }

    public Result init(Session session) {
        if (!dataService.appIsInizialized()) {
            session.clear();
            //TODO Refactoring
            //response.removeCookie("rememberme");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOG.error("Failed while trying to sleep in system/init", e);
            }
            //TODO Refactoring
            //            Fixtures.deleteAllModels();
            //            Fixtures.deleteDatabase();
            //            Fixtures.loadModels(YAMLFILE);

            final List<Game> prePlayoffGames = dataService.findAllNonPlayoffGames();
            final List<Game> playoffGames = dataService.findAllPlayoffGames();
            boolean hasPlayoffs = false;
            if ((playoffGames != null) && (playoffGames.size() > 0)) {
                hasPlayoffs = true;
            }

            Settings settings = dataService.findSettings();
            settings = dataService.findSettings();
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

            return Results.ok();
        }

        return Results.redirect("/");
    }

    public Result yamler() {
        //TODO Refactoring
        //        if (("true").equals(Play.configuration.getProperty("yamler"))) {
        //            final List<String> playdays = SetupService.generatePlaydays(34);
        //            final List<String> games = SetupService.getGamesFromWebService(34, "WM-2014", "2014");
        //            render(playdays, games);
        //        }
        //        notFound();

        return Results.notFound();
    }
}