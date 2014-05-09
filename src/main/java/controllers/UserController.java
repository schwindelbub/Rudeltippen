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
import models.Constants;
import models.ExtraTip;
import models.GameTip;
import models.Settings;
import models.User;
import models.statistic.UserStatistic;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;
import ninja.session.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.AuthService;
import services.DataService;
import services.I18nService;
import services.MailService;
import services.ValidationService;
import utils.AppUtils;

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
    
    @Inject
    private I18nService i18nService;
    
    @Inject
    private AuthService authService;
    
    @Inject
    private ValidationService validationService;

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

    public Result updateusername(Session session, Context context, FlashScope flashScope) {
        String username = context.getParameter("username");

        if (!validationService.isValidUsername(username)) {
            flashScope.error(i18nService.get("controller.users.usernamexists"));
        } else {
            final User user = context.getAttribute("connectedUser", User.class);
            user.setUsername(username);
            dataService.save(user);

            flashScope.success(i18nService.get("controller.profile.updateusername"));
            LOG.info("username updated: " + user.getEmail() + " / " + username);

            session.put("username", username);
        }

        return Results.redirect("/users/profile");
    }

    public Result updateemail(Context context, FlashScope flashScope) {
        String email = context.getParameter("email");
        String emailConfirmation = context.getParameter("emailConfirmation");

        if (!validationService.isValidEmail(email) || !email.equals(emailConfirmation)) {
            flashScope.error(i18nService.get("controller.users.invalidemail"));
        } else {
            final String token = UUID.randomUUID().toString();
            final User user = context.getAttribute("connectedUser", User.class);
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERNAME;
                final Confirmation confirmation = new Confirmation();
                confirmation.setConfirmType(confirmationType);
                confirmation.setConfirmValue(authService.encryptAES(email));
                confirmation.setCreated(new Date());
                confirmation.setToken(token);
                confirmation.setUser(user);
                dataService.save(confirmation);
                mailService.confirm(user, token, confirmationType);
                flashScope.success(i18nService.get("confirm.message"));
            }
        }

        return Results.redirect("/users/profile");
    }

    public Result updatepassword(Context context, FlashScope flashScope) {
        String userpass = context.getParameter("userpass");
        String userpassConfirmation = context.getParameter("userpassConfirmation");

        if (validationService.isValidPassword(userpass) || !userpass.equals(userpassConfirmation)) {
            flashScope.error(i18nService.get("controller.users.passwordisinvalid"));
        } else {
            final String token = UUID.randomUUID().toString();
            final User user = context.getAttribute("connectedUser", User.class);
            if (user != null) {
                final ConfirmationType confirmationType = ConfirmationType.CHANGEUSERPASS;
                final Confirmation confirm = new Confirmation();
                confirm.setConfirmType(confirmationType);
                confirm.setConfirmValue(authService.encryptAES(AppUtils.hashPassword(userpass, user.getSalt())));
                confirm.setCreated(new Date());
                confirm.setToken(token);
                confirm.setUser(user);
                dataService.save(confirm);
                mailService.confirm(user, token, confirmationType);
                flashScope.success(i18nService.get("confirm.message"));
                LOG.info("Password updated: " + user.getEmail());
            }
        }

        return Results.redirect("/users/profile");
    }

    public Result updatenotifications(Context context, FlashScope flashScope, final boolean reminder, final boolean notification, final boolean sendstandings, final boolean sendgametips) {
        final User user = context.getAttribute("conntectedUser", User.class);
        user.setReminder(reminder);
        user.setNotification(notification);
        user.setSendStandings(sendstandings);
        user.setSendGameTips(sendgametips);
        dataService.save(user);

        flashScope.success(i18nService.get("controller.profile.notifications"));
        LOG.info("Notifications updated: " + user.getEmail());

        return Results.redirect("/users/profile");
    }

    public Result updatepicture(final File picture, FlashScope flashScope, Context context) {
        final User user = context.getAttribute("connectedUser", User.class);
        
        String pictureLargeFilename = UUID.randomUUID().toString();
        String pictureSmallFilename = UUID.randomUUID().toString();
        
        File pictureSmall = new File(Constants.MEDIAFOLDER.value() + pictureSmallFilename);
        File pictureLarge = new File(Constants.MEDIAFOLDER.value() + pictureLargeFilename);

        AppUtils.resizeImage(pictureSmall, 64, 64);
        AppUtils.resizeImage(pictureLarge, 128, 128);
        
        if (picture.delete()) {
            LOG.warn("User-Picutre could not be deleted after upload.");
        } else {
            LOG.info("User-Picture deleted after upload.");
        }
        
        user.setPicture(pictureSmallFilename);
        user.setPictureLarge(pictureLargeFilename);
        dataService.save(user);

        flashScope.success(i18nService.get("controller.profile.updatepicture"));
        LOG.info("Picture updated: " + user.getEmail());

        return Results.redirect("/users/profile");
    }

    public Result deletepicture(Context context, FlashScope flashScope) {
        User user = context.getAttribute("connectedUser", User.class);
        user.setPicture(null);
        user.setPictureLarge(null);
        dataService.save(user);

        flashScope.success(i18nService.get("controller.profile.deletedpicture"));

        return Results.redirect("/users/profile");
    }
}