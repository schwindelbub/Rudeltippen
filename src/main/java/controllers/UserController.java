package controllers;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Confirmation;
import models.ConfirmationType;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Settings;
import models.User;
import models.statistic.UserStatistic;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.AuthService;
import services.DataService;
import services.MailService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class UserController extends RootController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Inject
    private DataService dataService;

    @Inject
    private MailService mailService;

    public Result show(@PathParam("username") String username) {
        final User user = dataService.findUserByUsername(username);

        if (user != null) {
            final Map<String, Integer> statistics = new HashMap<String, Integer>();
            final List<ExtraTip> extraTips = dataService.findExtraTipsByUser(user);
            final List<GameTip> tips = dataService.findGameTipsByUser(user);
            final long extra = dataService.countAllExtras();
            final int sumAllTipps = tips.size();
            final int correctTipps = user.getCorrectResults();
            final int correctTrend = user.getCorrectTrends();
            final int correctDifference = user.getCorrectDifferences();
            final DecimalFormat df = new DecimalFormat( "0.00" );

            statistics.put("sumGames", (int) dataService.countAllGames());
            statistics.put("sumTipps", sumAllTipps);
            statistics.put("correctTipps", correctTipps);
            statistics.put("correctTrend", correctTrend);
            statistics.put("correctDifference", correctDifference);
            statistics.put("extraTips", (int) extra);
            statistics.put("correctExtraTips", extraTips.size());

            int tippedGames = 0;
            for (final GameTip tip : tips) {
                if (tip.getGame().isEnded()) {
                    tippedGames++;
                }
            }

            String tippQuote = "0 %";
            if (tippedGames > 0) {
                final double quote = (100.00 / tippedGames) * correctTipps;
                tippQuote = df.format( quote );
            }

            final float pointsTipp = (float) user.getPoints() / (float) tippedGames;
            String pointsPerTipp = "0";
            if (pointsTipp > 0) {
                pointsPerTipp = df.format( pointsTipp );
            }

            final List<UserStatistic> userStatistics = dataService.findUserStatisticByUser(user);
            final int users = dataService.findAllActiveUsers().size();
            final int usersScale = users + 1;

            return Results.html()
                    .render("statistics", statistics)
                    .render("pointsPerTipp", pointsPerTipp)
                    .render("tippQuote", tippQuote)
                    .render("tippedGames", tippedGames)
                    .render("userStatistics", userStatistics)
                    .render("user", user)
                    .render("usersScale", usersScale);
        } else {
            return Results.redirect("/");
        }
    }

    public Result profile(Context context) {
        final User user = context.getAttribute("connectedUser", User.class);
        final Settings settings = dataService.findSettings();

        return Results.html().render("users", user).render(settings);
    }

    public Result updateusername(final String username, Session session) {
        //TODO Refactoring
        //        validation.required(username);
        //        validation.minSize(username, 3);
        //        validation.maxSize(username, 20);
        //        validation.isTrue(!ValidationService.usernameExists(username)).key("username").message(Messages.get("controller.users.usernamexists"));

        //TODO Refactoring - was validation.hasErrors();
        if (true) {
            //TODO Refactroing
            //            params.flash();
            //            validation.keep();
        } else {
            //TODO Refactoring
            final User user = null;//AppUtils.getConnectedUser();
            user.setUsername(username);
            dataService.save(user);

            //TODO Refactoring
            //flash.put("infomessage", Messages.get("controller.profile.updateusername"));
            LOG.info("username updated: " + user.getEmail() + " / " + username);

            session.put("username", username);
        }
        //TODO Refactoring
        //flash.keep();

        return Results.redirect("/users/profile");
    }

    //TODO Refactoring
    public Result updateemail(final String email, final String emailConfirmation) {
        //        validation.required(email);
        //        validation.email(email);
        //        validation.equals(email, emailConfirmation);
        //        validation.equals(ValidationService.emailExists(email), false).key("email").message(Messages.get("controller.users.emailexists"));

        //TODO Refactoring - was validations.hasErrors()
        if (true) {
            //            params.flash();
            //            validation.keep();
        } else {
            final String token = UUID.randomUUID().toString();
            final User user = null;//AppUtils.getConnectedUser();
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERNAME;
                final Confirmation confirmation = new Confirmation();
                confirmation.setConfirmType(confirmationType);
                confirmation.setConfirmValue(null); // TODO Refactoring - was Crypto.encryptAES(email)
                confirmation.setCreated(new Date());
                confirmation.setToken(token);
                confirmation.setUser(user);
                dataService.save(confirmation);
                mailService.confirm(user, token, confirmationType);
                //flash.put("infomessage", Messages.get("confirm.message"));
            }
        }
        //TODO Refactoring
        //flash.keep();

        return Results.redirect("/users/profile");
    }

    public Result updatepassword(final String userpass, final String userpassConfirmation) {
        //        validation.required(userpass);
        //        validation.equals(userpass, userpassConfirmation);
        //        validation.minSize(userpass, 6);
        //        validation.maxSize(userpass, 30);

        //TODO Refactoring - was validations.hasErrors()
        if (true) {
            //            params.flash();
            //            validation.keep();
        } else {
            final String token = UUID.randomUUID().toString();
            final User user = null;//AppUtils.getConnectedUser();
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERPASS;
                final Confirmation confirm = new Confirmation();
                confirm.setConfirmType(confirmationType);
                confirm.setConfirmValue(null); //TODO Refactoring - was Crypto.encryptAES(AppUtils.hashPassword(userpass, user.getSalt()))
                confirm.setCreated(new Date());
                confirm.setToken(token);
                confirm.setUser(user);
                dataService.save(confirm);
                mailService.confirm(user, token, confirmationType);
                //flash.put("infomessage", Messages.get("confirm.message"));
                LOG.info("Password updated: " + user.getEmail());
            }
        }
        //flash.keep();

        return Results.redirect("/users/profile");
    }

    public Result updatenotifications(final boolean reminder, final boolean notification, final boolean sendstandings, final boolean sendgametips) {
        final User user = null;//AppUtils.getConnectedUser(); TODO Refactoring
        user.setReminder(reminder);
        user.setNotification(notification);
        user.setSendStandings(sendstandings);
        user.setSendGameTips(sendgametips);
        dataService.save(user);

        //TODO Refactoring
        //flash.put("infomessage", Messages.get("controller.profile.notifications"));
        //flash.keep();
        LOG.info("Notifications updated: " + user.getEmail());

        return Results.redirect("/users/profile");
    }

    public Result updatepicture(final File picture) {
        //validation.required(picture);

        if (picture != null) {
            //final String message = Messages.get("profile.maxpicturesize", 100);
            //validation.isTrue(ValidationService.checkFileLength(picture.length())).key("picture").message(message);
        } else {
            //validation.isTrue(false);
        }

        //TODO Refactoring - was validation.hasErrors();
        if (true) {
            //            params.flash();
            //            validation.keep();
        } else {
            final User user = null;//AppUtils.getConnectedUser();
            //Images.resize(picture, picture, PICTURELARGE, PICTURELARGE);
            //user.setPictureLarge(Images.toBase64(picture));
            //Images.resize(picture, picture, PICTURESMALL, PICTURESMALL);
            //user.setPicture(Images.toBase64(picture));
            if (picture.delete()) {
                LOG.warn("User-Picutre could not be deleted after upload.");
            } else {
                LOG.info("User-Picture deleted after upload.");
            }

            dataService.save(user);
            //flash.put("infomessage", Messages.get("controller.profile.updatepicture"));
            LOG.info("Picture updated: " + user.getEmail());
        }

        //flash.keep();
        return Results.redirect("/users/profile#picture");
    }

    //TODO Refactoring
    public Result deletepicture() {
        final User user = null;//AppUtils.getConnectedUser();
        user.setPicture(null);
        user.setPictureLarge(null);
        dataService.save(user);

        //flash.put("infomessage", Messages.get("controller.profile.deletedpicture"));
        //flash.keep();

        return Results.redirect("/users/profile#picture");
    }
}