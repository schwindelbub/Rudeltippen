package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.AbstractJob;
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
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.CalculationService;
import services.DataService;
import services.I18nService;
import services.MailService;
import services.ValidationService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import dtos.SettingsDTO;
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
    
    @Inject
    private ValidationService validationService;
    
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
        final Map<String, String> map = AppUtils.convertParamaters(context.getParameters());
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
        flashScope.put("warning", i18nService.get("controller.games.tippsstored", null));

        int playday = 1;
        if ((keys != null) && (keys.size() >= 1)) {
            if (StringUtils.isNotBlank(gamekey)) {
                gamekey = gamekey.replace("_et", "");
                final Game game = dataService.findGameById(gamekey);
                if ((game != null) && (game.getPlayday() != null)) {
                    playday = game.getPlayday().getNumber();
                }
            }
        }

        return Results.redirect("/admin/results/" + playday);
    }

    public Result updatesettings (FlashScope flashScope, @JSR303Validation SettingsDTO settingsDTO, Validation validation) {
        validationService.validateSettingsDTO(settingsDTO, validation);
        
        if (!validation.hasBeanViolations()) {
            final Settings settings = dataService.findSettings();
            settings.setGameName(settingsDTO.name);
            settings.setPointsTip(settingsDTO.pointsTip);
            settings.setPointsTipDiff(settingsDTO.pointsTipDiff);
            settings.setPointsTipTrend(settingsDTO.pointsTipTrend);
            settings.setMinutesBeforeTip(settingsDTO.minutesBeforeTip);
            settings.setInformOnNewTipper(settingsDTO.informOnNewTipper);
            settings.setCountFinalResult(settingsDTO.countFinalResult);
            settings.setEnableRegistration(settingsDTO.enableRegistration);
            dataService.save(settings);

            flashScope.success(i18nService.get("setup.saved"));
        }

        return Results.redirect("/settings");
    }

    public Result settings(FlashScope flashScope) {
        final Settings settings = dataService.findSettings();

        flashScope.put("name", settings.getGameName());
        flashScope.put("pointsTip", settings.getPointsTip());
        flashScope.put("pointsTipDiff", settings.getPointsTipDiff());
        flashScope.put("pointsTipTrend", settings.getPointsTipTrend());
        flashScope.put("minutesBeforeTip", settings.getMinutesBeforeTip());
        flashScope.put("informOnNewTipper", settings.isInformOnNewTipper());
        flashScope.put("countFinalResult", settings.isCountFinalResult());
        flashScope.put("enableRegistration", settings.isEnableRegistration());

        return Results.html().render(settings);
    }

    public Result changeactive(@PathParam("userid") String userId, Context context, FlashScope flashScope) {
        final User connectedUser = context.getAttribute("connectedUser", User.class);
        final User user = dataService.findUserById(userId);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String activate;
                if (user.isActive()) {
                    user.setActive(false);
                    activate = "deactivated";
                    message = i18nService.get("info.change.deactivate");
                } else {
                    final Confirmation confirmation = dataService.findConfirmationByTypeAndUser(ConfirmationType.ACTIVATION, user);
                    if (confirmation != null) {
                        dataService.delete(confirmation);
                    }
                    user.setActive(true);
                    activate = "activated";
                    message = i18nService.get("info.change.activate", new Object[]{user.getEmail()});
                }
                dataService.save(user);
                flashScope.success(message);
                LOG.info("User " + user.getEmail() + " has been " + activate + " - by " + connectedUser.getEmail());
            } else {
                flashScope.put("warning", i18nService.get("warning.change.active"));
            }
        } else {
            flashScope.error(i18nService.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    public Result changeadmin(@PathParam("userid") String userId, FlashScope flashScope, Context context) {
        final User connectedUser = context.getAttribute("connectedUser", User.class);
        final User user = dataService.findUserById(userId);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                String message;
                String admin;
                if (user.isAdmin()) {
                    message = i18nService.get("info.change.deadmin", new Object[]{user.getEmail()});
                    admin = "is now admin";
                    user.setAdmin(false);
                } else {
                    message = i18nService.get("info.change.admin", new Object[]{user.getEmail()});
                    admin = "is not admin anymore";
                    user.setAdmin(true);
                }
                dataService.save(user);
                flashScope.success(message);
                LOG.info("User " + user.getEmail() + " " + admin + " - by " + connectedUser.getEmail());
            } else {
                flashScope.put("warning", i18nService.get("warning.change.admin"));
            }
        } else {
            flashScope.error(i18nService.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    public Result deleteuser(@PathParam("userid") String userId, FlashScope flashScope, Context context) {
        final User connectedUser = context.getAttribute("conntectedUser", User.class);
        final User user = dataService.findUserById(userId);

        if (user != null) {
            if (!connectedUser.equals(user)) {
                final String username = user.getEmail();
                dataService.delete(user);
                flashScope.success(i18nService.get("info.delete.user", new Object[]{username}));
                LOG.info("User " + username + " has been deleted - by " + connectedUser.getEmail());

                calculationService.calculations();
            } else {
                flashScope.put("warning", i18nService.get("warning.delete.user"));
            }
        } else {
            flashScope.error(i18nService.get("error.loading.user"));
        }

        return Results.redirect("/admin/users");
    }

    public Result rudelmail() {
        return Results.html();
    }

    public Result tournament() {
        List<Bracket> brackets = dataService.findAllBrackets();
        List<Game> games = dataService.findAllGames();

        return Results.html().render("brackets", brackets).render("games", games);
    }

    public Result send(FlashScope flashScope, Context context) {
        String subject = context.getParameter("subject");
        String message = context.getParameter("message");
        
        if (StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(message)) {
            final List<String> recipients = new ArrayList<String>();
            final List<User> users = dataService.findAllActiveUsers();
            for (final User user : users) {
                recipients.add(user.getEmail());
            }

            String[] recipientsArray = new String[users.size()];
            recipientsArray = recipients.toArray(recipientsArray);
            
            User connectedUser = context.getAttribute("connectedUser", User.class);
            mailService.rudelmail(subject, message, recipientsArray, connectedUser.getEmail());
            flashScope.success(i18nService.get("info.rudelmail.send"));
        } else {
            flashScope.error(i18nService.get("error.rudelmail.send"));
        }

        return Results.redirect("/rudelmail");
    }

    public Result jobstatus(final String name) {
        if (StringUtils.isNotBlank(name)) {
            AbstractJob abstractJob = dataService.findAbstractJobByName(name);
            abstractJob.setActive(!abstractJob.isActive());
            dataService.save(abstractJob);
        }

        return Results.redirect("/admin/jobs");
    }
    
    public Result jobs() {
        List<AbstractJob> jobs = dataService.findAllAbstractJobs();
        
        return Results.html().render("jobs", jobs);
    }

    public Result calculations() {
        calculationService.calculations();

        return Results.redirect("/tournament");
    }
}