package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Bracket;
import models.Confirmation;
import models.ConfirmationType;
import models.Game;
import models.Pagination;
import models.Playday;
import models.Settings;
import models.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.CalculationService;
import services.DataService;
import services.I18nService;
import services.MailService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import filters.AuthorizationFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith(AuthorizationFilter.class)
public class AdminController extends RootController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Inject
    private DataService dataService;

    @Inject
    private CalculationService calculationService;

    @Inject
    private MailService mailService;

    @Inject
    private I18nService i18nService;
    
    public Result results(@PathParam("number") long number) {
        final Pagination pagination = AppUtils.getPagination(number, "/admin/results/", dataService.findAllPlaydaysOrderByNumber().size());
        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());

        return Results.html().render(playday).render(pagination);
    }

    public Result users() {
        final List<User> users = dataService.findUsersOrderByUsername();
        return Results.html().render("users", users);
    }

    public Result storeresults(Context context, FlashScope flashScope) {
        //TODO Refactoring
        final Map<String, String> map = null;//params.allSimple();
        final Set<String> keys = new HashSet<String>();
        for (final Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isNotBlank(key) && key.contains("game_") && (key.contains("_homeScore") || key.contains("_awayScore"))) {
                key = key.replace("game_", "")
                        .replace("_homeScore", "")
                        .replace("_awayScore", "")
                        .replace("_homeScore_et", "")
                        .replace("_awayScore_et", "")
                        .trim();
                keys.add(key);
            }
        }

        String gamekey = null;
        for (final String key : keys) {
            gamekey = key;
            final String homeScore = map.get("game_" + key + "_homeScore");
            final String awayScore = map.get("game_" + key + "_awayScore");
            final String extratime = map.get("extratime_" + key);
            final String homeScoreExtratime = map.get("game_" + key + "_homeScore_et");
            final String awayScoreExtratime = map.get("game_" + key + "_awayScore_et");
            calculationService.setGameScore(key, homeScore, awayScore, extratime, homeScoreExtratime, awayScoreExtratime);
        }

        calculationService.calculations();
        flashScope.put("warning", i18nService.get("controller.games.tippsstored"));

        int playday = 1;
        if ((keys != null) && (keys.size() >= 1)) {
            if (StringUtils.isNotBlank(gamekey)) {
                gamekey = gamekey.replace("_et", "");
                final Game game = dataService.findGameById(new Long(gamekey));
                if ((game != null) && (game.getPlayday() != null)) {
                    playday = game.getPlayday().getNumber();
                }
            }
        }

        return Results.redirect("/admin/results/" + playday);
    }

    public Result updatesettings (FlashScope flashScope, final String name, final int pointsTip, final int pointsTipDiff, final int pointsTipTrend, final int minutesBeforeTip, final boolean countFinalResult, final boolean informOnNewTipper, final boolean enableRegistration, final String trackingcode) {
        //        validation.range(pointsTip, 0, 99);
        //        validation.range(pointsTipDiff, 0, 99);
        //        validation.range(pointsTipTrend, 0, 99);

        //TODO Refactoring - was !validation.hasErrors()
        if (true) {
            final Settings settings = dataService.findSettings();
            settings.setGameName(name);
            settings.setPointsTip(pointsTip);
            settings.setPointsTipDiff(pointsTipDiff);
            settings.setPointsTipTrend(pointsTipTrend);
            settings.setMinutesBeforeTip(minutesBeforeTip);
            settings.setInformOnNewTipper(informOnNewTipper);
            settings.setCountFinalResult(countFinalResult);
            settings.setEnableRegistration(enableRegistration);
            dataService.save(settings);

            flashScope.success(i18nService.get("setup.saved"));
        }

        return Results.redirect("/settings");
    }

    public Result settings() {
        final Settings settings = dataService.findSettings();

        //TODO Refactoring
        //        flash.put("name", settings.getGameName());
        //        flash.put("pointsTip", settings.getPointsTip());
        //        flash.put("pointsTipDiff", settings.getPointsTipDiff());
        //        flash.put("pointsTipTrend", settings.getPointsTipTrend());
        //        flash.put("minutesBeforeTip", settings.getMinutesBeforeTip());
        //        flash.put("informOnNewTipper", settings.isInformOnNewTipper());
        //        flash.put("countFinalResult", settings.isCountFinalResult());
        //        flash.put("enableRegistration", settings.isEnableRegistration());

        return Results.html().render(settings);
    }

    //TODO Refactoring
    public Result changeactive(@PathParam("userid") long userid) {
        final User connectedUser = null;//AppUtils.getConnectedUser();
        final User user = dataService.findUserById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String activate;
                if (user.isActive()) {
                    user.setActive(false);
                    activate = "deactivated";
                    message = null;//Messages.get("info.change.deactivate", user.getEmail());
                } else {
                    final Confirmation confirmation = dataService.findConfirmationByTypeAndUser(ConfirmationType.ACTIVATION, user);
                    if (confirmation != null) {
                        dataService.delete(confirmation);
                    }
                    user.setActive(true);
                    activate = "activated";
                    message = null;//Messages.get("info.change.activate", user.getEmail());
                }
                dataService.save(user);
                //flash.put("infomessage", message);
                LOG.info("User " + user.getEmail() + " has been " + activate + " - by " + connectedUser.getEmail());
            } else {
                //flash.put("warningmessage", Messages.get("warning.change.active"));
            }
        } else {
            //flash.put("errormessage", Messages.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    //TODO Refactoring
    public Result changeadmin(@PathParam("userid") long userid, FlashScope flashScope) {
        final User connectedUser = null;//AppUtils.getConnectedUser();
        final User user = dataService.findUserById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String admin;
                if (user.isAdmin()) {
                    message = null;//Messages.get("info.change.deadmin", user.getEmail());
                    admin = "is now admin";
                    user.setAdmin(false);
                } else {
                    message = null;//Messages.get("info.change.admin", user.getEmail());
                    admin = "is not admin anymore";
                    user.setAdmin(true);
                }
                dataService.save(user);
                //flash.put("infomessage", message);
                LOG.info("User " + user.getEmail() + " " + admin + " - by " + connectedUser.getEmail());
            } else {
                //flash.put("warningmessage", Messages.get("warning.change.admin"));
            }
        } else {
            flashScope.error(i18nService.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    public Result deleteuser(@PathParam("userid") long userid, FlashScope flashScope) {
        final User connectedUser = null;//AppUtils.getConnectedUser();
        final User user = dataService.findUserById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                final String username = user.getEmail();
                dataService.delete(user);
                //flash.put("infomessage", Messages.get("info.delete.user", username));
                LOG.info("User " + username + " has been deleted - by " + connectedUser.getEmail());

                calculationService.calculations();
            } else {
                //flash.put("warningmessage", Messages.get("warning.delete.user"));
            }
        } else {
            flashScope.error(i18nService.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    public Result jobs() {
        //TODO Refactoring
        //        final List<Job> jobs = JobsPlugin.scheduledJobs;
        //        render(jobs);
        return Results.html();
    }

    //TODO Refactoring
    public Result runjob(final String name) {
        //        if (StringUtils.isNotBlank(name)) {
        //            final List<Job> jobs = JobsPlugin.scheduledJobs;
        //            for (final Job job : jobs) {
        //                if (name.equalsIgnoreCase(job.getClass().getSimpleName())) {
        //                    job.now();
        //                }
        //            }
        //        }
        //        jobs();
        return Results.html();
    }

    public Result rudelmail() {
        return Results.html();
    }

    public Result tournament() {
        List<Bracket> brackets = dataService.findAllBrackets();
        List<Game> games = dataService.findAllGames();

        return Results.html().render("brackets", brackets).render("games", games);
    }

    public Result send(final String subject, final String message, FlashScope flashScope) {
        //        validation.required(subject);
        //        validation.required(message);

        //TODO Refactoring - was !validation.hasErrors()
        if (true) {
            final List<String> recipients = new ArrayList<String>();
            final List<User> users = dataService.findAllActiveUsers();
            for (final User user : users) {
                recipients.add(user.getEmail());
            }

            mailService.rudelmail(subject, message, recipients.toArray(), null/*AppUtils.getConnectedUser().getEmail()*/);
            //flash.put("infomessage", Messages.get("info.rudelmail.send"));
        } else {
            //flash.put("errormessage", Messages.get("error.rudelmail.send"));
            flashScope.error(i18nService.get("error.rudelmail.send"));
        }

        return Results.redirect("/rudelmail");
    }

    public Result jobstatus(final String name) {
        //TODO Refactoring
        //        if (StringUtils.isNotBlank(name)) {
        //            AbstractJob abstractJob = AbstractJob.find("byName", name).first();
        //            abstractJob.setActive(!abstractJob.isActive());
        //            abstractJob._save();
        //        }
        //        jobs();

        return Results.html();
    }

    public Result calculations() {
        calculationService.calculations();

        return Results.redirect("/tournament");
    }
}