package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jobs.GameTipJob;
import models.AbstractJob;
import models.Bracket;
import models.Confirmation;
import models.ConfirmationType;
import models.Game;
import models.Pagination;
import models.Playday;
import models.Settings;
import models.User;
import ninja.Result;
import ninja.Results;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import services.DataService;
import services.MailService;
import services.ValidationService;
import utils.AppUtils;
import utils.ViewUtils;

@Singleton
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Inject
    private DataService dataService;

    public Result results(final long number) {
        final Pagination pagination = ViewUtils.getPagination(number, "/admin/results/");
        final Playday playday = Playday.find("byNumber", pagination.getNumberAsInt()).first();

        return Results.html().render(playday).render(pagination);
    }

    public Result users() {
        final List<User> users = User.find("SELECT u FROM User u ORDER BY username ASC").fetch();
        render(users);
    }

    public Result storeresults() {
        final Map<String, String> map = params.allSimple();
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
            AppUtils.setGameScore(key, homeScore, awayScore, extratime, homeScoreExtratime, awayScoreExtratime);
        }

        AppUtils.calculations();

        flash.put("infomessage", Messages.get("controller.games.tippsstored"));
        flash.keep();

        int playday = 1;
        if ((keys != null) && (keys.size() >= 1)) {
            if (StringUtils.isNotBlank(gamekey)) {
                gamekey = gamekey.replace("_et", "");
                final Game game = Game.findById(new Long(gamekey));
                if ((game != null) && (game.getPlayday() != null)) {
                    playday = game.getPlayday().getNumber();
                }
            }
        }

        redirect("/admin/results/" + playday);
    }

    public Result updatesettings (final String name, final int pointsTip, final int pointsTipDiff, final int pointsTipTrend, final int minutesBeforeTip, final boolean countFinalResult, final boolean informOnNewTipper, final boolean enableRegistration, final String trackingcode) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        validation.range(pointsTip, 0, 99);
        validation.range(pointsTipDiff, 0, 99);
        validation.range(pointsTipTrend, 0, 99);

        if (!validation.hasErrors()) {
            final Settings settings = Settings.find("byAppName", APPNAME).first();
            settings.setGameName(name);
            settings.setPointsTip(pointsTip);
            settings.setPointsTipDiff(pointsTipDiff);
            settings.setPointsTipTrend(pointsTipTrend);
            settings.setMinutesBeforeTip(minutesBeforeTip);
            settings.setInformOnNewTipper(informOnNewTipper);
            settings.setCountFinalResult(countFinalResult);
            settings.setEnableRegistration(enableRegistration);
            settings._save();

            flash.put("infomessage", Messages.get("setup.saved"));
            flash.keep();
        }
        params.flash();
        validation.keep();

        settings();
    }

    public Result settings() {
        final Settings settings = AppUtils.findSettings();

        flash.put("name", settings.getGameName());
        flash.put("pointsTip", settings.getPointsTip());
        flash.put("pointsTipDiff", settings.getPointsTipDiff());
        flash.put("pointsTipTrend", settings.getPointsTipTrend());
        flash.put("minutesBeforeTip", settings.getMinutesBeforeTip());
        flash.put("informOnNewTipper", settings.isInformOnNewTipper());
        flash.put("countFinalResult", settings.isCountFinalResult());
        flash.put("enableRegistration", settings.isEnableRegistration());

        render(settings);
    }

    public Result changeactive(final long userid) {
        final User connectedUser = AppUtils.getConnectedUser();
        final User user = User.findById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String activate;
                if (user.isActive()) {
                    user.setActive(false);
                    activate = "deactivated";
                    message = Messages.get("info.change.deactivate", user.getEmail());
                } else {
                    final Confirmation confirmation = Confirmation.find("byConfirmTypeAndUser", ConfirmationType.ACTIVATION, user).first();
                    if (confirmation != null) {
                        confirmation._delete();
                    }
                    user.setActive(true);
                    activate = "activated";
                    message = Messages.get("info.change.activate", user.getEmail());
                }
                user._save();
                flash.put("infomessage", message);
                LOG.info("User " + user.getEmail() + " has been " + activate + " - by " + connectedUser.getEmail());
            } else {
                flash.put("warningmessage", Messages.get("warning.change.active"));
            }
        } else {
            flash.put("errormessage", Messages.get("error.loading.user"));
        }

        flash.keep();
        redirect("/admin/users");
    }

    public Result changeadmin(final long userid) {
        final User connectedUser = AppUtils.getConnectedUser();
        final User user = User.findById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String admin;
                if (user.isAdmin()) {
                    message = Messages.get("info.change.deadmin", user.getEmail());
                    admin = "is now admin";
                    user.setAdmin(false);
                } else {
                    message = Messages.get("info.change.admin", user.getEmail());
                    admin = "is not admin anymore";
                    user.setAdmin(true);
                }
                user._save();
                flash.put("infomessage", message);
                LOG.info("User " + user.getEmail() + " " + admin + " - by " + connectedUser.getEmail());
            } else {
                flash.put("warningmessage", Messages.get("warning.change.admin"));
            }
        } else {
            flash.put("errormessage", Messages.get("error.loading.user"));
        }

        flash.keep();
        redirect("/admin/users");
    }

    public Result deleteuser(final long userid) {
        final User connectedUser = AppUtils.getConnectedUser();
        final User user = User.findById(userid);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                final String username = user.getEmail();
                user._delete();
                flash.put("infomessage", Messages.get("info.delete.user", username));
                LOG.info("User " + username + " has been deleted - by " + connectedUser.getEmail());

                AppUtils.calculations();
            } else {
                flash.put("warningmessage", Messages.get("warning.delete.user"));
            }
        } else {
            flash.put("errormessage", Messages.get("error.loading.user"));
        }

        flash.keep();
        redirect("/admin/users");
    }

    public Result jobs() {
        final List<Job> jobs = JobsPlugin.scheduledJobs;
        render(jobs);
    }

    public Result runjob(final String name) {
        if (StringUtils.isNotBlank(name)) {
            final List<Job> jobs = JobsPlugin.scheduledJobs;
            for (final Job job : jobs) {
                if (name.equalsIgnoreCase(job.getClass().getSimpleName())) {
                    job.now();
                }
            }
        }
        jobs();
    }

    public Result rudelmail() {
        return Results.html();
    }

    public Result tournament() {
        List<Bracket> brackets = Bracket.findAll();
        List<Game> games = Game.findAll();

        render(brackets, games);
    }

    public Result send(final String subject, final String message) {
        validation.required(subject);
        validation.required(message);

        if (!validation.hasErrors()) {
            final List<String> recipients = new ArrayList<String>();
            final List<User> users = AppUtils.getAllActiveUsers();
            for (final User user : users) {
                recipients.add(user.getEmail());
            }

            MailService.rudelmail(subject, message, recipients.toArray(), AppUtils.getConnectedUser().getEmail());
            flash.put("infomessage", Messages.get("info.rudelmail.send"));
        } else {
            flash.put("errormessage", Messages.get("error.rudelmail.send"));
            params.flash();
            validation.keep();
        }
        flash.keep();

        rudelmail();
    }

    public Result jobstatus(final String name) {
        if (StringUtils.isNotBlank(name)) {
            AbstractJob abstractJob = AbstractJob.find("byName", name).first();
            abstractJob.setActive(!abstractJob.isActive());
            abstractJob._save();
        }
        jobs();
    }

    public Result calculations() {
        AppUtils.calculations();
        tournament();
    }
}