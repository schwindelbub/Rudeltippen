package controllers;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ninja.Result;
import models.Confirmation;
import models.ConfirmationType;
import models.Extra;
import models.ExtraTip;
import models.Game;
import models.GameTip;
import models.Settings;
import models.User;
import models.statistic.UserStatistic;
import services.MailService;
import services.ValidationService;
import utils.AppUtils;

public class UserController {

    public Result show(final String username) {
        final User user = User.find("byUsername", username).first();

        if (user != null) {
            final Map<String, Integer> statistics = new HashMap<String, Integer>();
            final List<ExtraTip> extraTips = ExtraTip.find("SELECT e FROM ExtraTip e WHERE user = ? AND points > 0", user).fetch();
            final List<GameTip> tips = GameTip.find("byUser", user).fetch();
            final long extra = Extra.count();
            final int sumAllTipps = tips.size();
            final int correctTipps = user.getCorrectResults();
            final int correctTrend = user.getCorrectTrends();
            final int correctDifference = user.getCorrectDifferences();
            final DecimalFormat df = new DecimalFormat( "0.00" );

            statistics.put("sumGames", (int) Game.count());
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

            final List<UserStatistic> userStatistics = UserStatistic.find("SELECT u FROM UserStatistic u WHERE user = ? ORDER BY playday ASC", user).fetch();
            final int users = AppUtils.getAllActiveUsers().size();
            final int usersScale = users + 1;

            render(user, statistics, pointsPerTipp, tippQuote, tippedGames, userStatistics, users, usersScale);
        } else {
            redirect("/");
        }
    }

    @Transactional(readOnly=true)
    public Result profile() {
        final User user = AppUtils.getConnectedUser();
        final Settings settings = AppUtils.getSettings();
        render(user, settings);
    }

    public Result updateusername(final String username) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        validation.required(username);
        validation.minSize(username, 3);
        validation.maxSize(username, 20);
        validation.isTrue(!ValidationService.usernameExists(username)).key("username").message(Messages.get("controller.users.usernamexists"));

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
            final User user = AppUtils.getConnectedUser();
            user.setUsername(username);
            user._save();

            flash.put("infomessage", Messages.get("controller.profile.updateusername"));
            Logger.info("username updated: " + user.getEmail() + " / " + username);

            session.put("username", username);
        }
        flash.keep();

        redirect("/users/profile");
    }

    public Result updateemail(final String email, final String emailConfirmation) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        validation.required(email);
        validation.email(email);
        validation.equals(email, emailConfirmation);
        validation.equals(ValidationService.emailExists(email), false).key("email").message(Messages.get("controller.users.emailexists"));

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
            final String token = Codec.UUID();
            final User user = AppUtils.getConnectedUser();
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERNAME;
                final Confirmation confirmation = new Confirmation();
                confirmation.setConfirmType(confirmationType);
                confirmation.setConfirmValue(Crypto.encryptAES(email));
                confirmation.setCreated(new Date());
                confirmation.setToken(token);
                confirmation.setUser(user);
                confirmation._save();
                MailService.confirm(user, token, confirmationType);
                flash.put("infomessage", Messages.get("confirm.message"));
            }
        }
        flash.keep();

        redirect("/users/profile");
    }

    public Result updatepassword(final String userpass, final String userpassConfirmation) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        validation.required(userpass);
        validation.equals(userpass, userpassConfirmation);
        validation.minSize(userpass, 6);
        validation.maxSize(userpass, 30);

        if (Validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
            final String token = Codec.UUID();
            final User user = AppUtils.getConnectedUser();
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERPASS;
                final Confirmation confirm = new Confirmation();
                confirm.setConfirmType(confirmationType);
                confirm.setConfirmValue(Crypto.encryptAES(AppUtils.hashPassword(userpass, user.getSalt())));
                confirm.setCreated(new Date());
                confirm.setToken(token);
                confirm.setUser(user);
                confirm._save();
                MailService.confirm(user, token, confirmationType);
                flash.put("infomessage", Messages.get("confirm.message"));
                Logger.info("Password updated: " + user.getEmail());
            }
        }
        flash.keep();

        redirect("/users/profile");
    }

    public Result updatenotifications(final boolean reminder, final boolean notification, final boolean sendstandings, final boolean sendgametips) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        final User user = AppUtils.getConnectedUser();
        user.setReminder(reminder);
        user.setNotification(notification);
        user.setSendStandings(sendstandings);
        user.setSendGameTips(sendgametips);
        user._save();

        flash.put("infomessage", Messages.get("controller.profile.notifications"));
        flash.keep();
        Logger.info("Notifications updated: " + user.getEmail());

        redirect("/users/profile");
    }

    public Result updatepicture(final File picture) {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        validation.required(picture);

        if (picture != null) {
            final String message = Messages.get("profile.maxpicturesize", 100);
            validation.isTrue(ValidationService.checkFileLength(picture.length())).key("picture").message(message);
        } else {
            validation.isTrue(false);
        }

        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
        } else {
            final User user = AppUtils.getConnectedUser();
            try {
                Images.resize(picture, picture, PICTURELARGE, PICTURELARGE);
                user.setPictureLarge(Images.toBase64(picture));
                Images.resize(picture, picture, PICTURESMALL, PICTURESMALL);
                user.setPicture(Images.toBase64(picture));
                if (picture.delete()) {
                    Logger.warn("User-Picutre could not be deleted after upload.");
                } else {
                    Logger.info("User-Picture deleted after upload.");
                }

                user._save();
                flash.put("infomessage", Messages.get("controller.profile.updatepicture"));
                Logger.info("Picture updated: " + user.getEmail());
            } catch (final IOException e) {
                flash.put("warningmessage", Messages.get("controller.profile.updatepicturefail"));
                Logger.error("Failed to save user picture", e);
            }
        }

        flash.keep();
        redirect("/users/profile#picture");
    }

    public Result deletepicture() {
        final User user = AppUtils.getConnectedUser();
        user.setPicture(null);
        user.setPictureLarge(null);
        user._save();

        flash.put("infomessage", Messages.get("controller.profile.deletedpicture"));
        flash.keep();

        redirect("/users/profile#picture");
    }
}