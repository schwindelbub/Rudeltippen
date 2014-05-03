package services;

import java.util.List;

import models.ConfirmationType;
import models.Extra;
import models.Game;
import models.Settings;
import models.User;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.utils.NinjaProperties;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ValidationUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Inject
    private Provider<Mail> mailProvider;

    @Inject
    private Postoffice postoffice;

    @Inject
    private NinjaProperties ninjaProperties;

    @Inject
    private I18nService i18nService;

    @Inject
    private DataService dataService;

    public void reminder(final User user, final List<Game> games, final List<Extra> extras) {
        final Settings settings = dataService.findSettings();
        final String recipient = user.getEmail();

        if (ValidationUtils.isValidEmail(recipient)) {
            Mail mail = getMailInstance(settings, recipient, StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + i18nService.get("mails.subject.reminder")));

            //TODO Refactoring
            //mail.setBodyText("bodyText");
            //send(user, games, settings, extras);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send reminder e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent reminder, but recipient was invalid.");
        }
    }

    public void confirm(final User user, final String token, final ConfirmationType confirmationType) {
        final Settings settings = dataService.findSettings();

        if ((user != null) && ValidationUtils.isValidEmail(user.getEmail()) && StringUtils.isNotBlank(token) && (confirmationType != null)) {
            String subject = "";
            String message = "";

            if (ConfirmationType.ACTIVATION.equals(confirmationType)) {
                subject = i18nService.get("mails.subject.activate");
                message = i18nService.get("mails.message.activate");
            } else if (ConfirmationType.CHANGEUSERNAME.equals(confirmationType)) {
                subject = i18nService.get("mails.subject.changeusername");
                message = i18nService.get("mails.message.changeusername");
            } else if (ConfirmationType.CHANGEUSERPASS.equals(confirmationType)) {
                subject = i18nService.get("mails.subject.changeuserpass");
                message = i18nService.get("mails.message.changeuserpass");
            } else if (ConfirmationType.NEWUSERPASS.equals(confirmationType)) {
                subject = i18nService.get("mails.subject.forgotuserpass");
                message = i18nService.get("mails.message.forgotuserpass");
            }
            message = StringEscapeUtils.unescapeHtml(message);

            Mail mail = getMailInstance(settings, user.getEmail(), StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + subject));

            //TODO Refactoring
            //mail.setBodyText("bodyText");
            //send(user, token, appUrl, message);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send confirm e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent confirmation e-mail, but user or confirmType was null or recipient e-mail was invalid.");
        }
    }

    public void newuser(final User user, final User admin) {
        final Settings settings = dataService.findSettings();
        if (ValidationUtils.isValidEmail(admin.getEmail()) && (user != null)) {
            Mail mail = getMailInstance(settings, user.getEmail(), StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + i18nService.get("mails.subject.newuser")));

            //TODO Refactoring
            //mail.setBodyText("bodyText");
            //send(user, settings);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send Reminder e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent new user e-mail to admin, but recipient was invalid or user was null.");
        }
    }

    public void error(final String response, final String recipient) {
        final Settings settings = dataService.findSettings();

        if (ValidationUtils.isValidEmail(recipient) && StringUtils.isNotBlank(response)) {
            Mail mail = getMailInstance(settings, recipient, StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + i18nService.get("mails.subject.updatefailed")));

            //TODO Refactoring
            //mail.setBodyText("bodyText");
            //send(response);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send error e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent info on webservice, but recipient was invalid or response was null.");
        }
    }

    public void notifications(final String subject, String notification, final User user) {
        final Settings settings = dataService.findSettings();
        notification = StringEscapeUtils.unescapeHtml(notification);

        if (ValidationUtils.isValidEmail(user.getEmail()) && StringUtils.isNotEmpty(notification)) {
            Mail mail = getMailInstance(settings, user.getEmail(), StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + subject));

            //TODO Refactoring
            //mail.setBodyText("bodyText");
            //send(notification);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send notifications e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent result notification, but recipient was invalid or notification was null.");
        }
    }

    public void gametips(final User user, final List<Game> games) {
        final Settings settings = dataService.findSettings();

        if (ValidationUtils.isValidEmail(user.getEmail()) && (games.size() > 0)) {
            Mail mail = getMailInstance(settings, user.getEmail(), StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + i18nService.get("overview")));

            //TODO Refactoring
            //mail.setBodyHTML("bodyHTML");
            //send(games, user);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send gametips e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent gametips mail, but recipient was invalid or games list was empty.");
        }
    }

    public void rudelmail(final String subject, final String message, final Object [] bbcRecipients, String recipient) {
        final Settings settings = dataService.findSettings();

        if (StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(message) && (bbcRecipients != null)) {
            Mail mail = getMailInstance(settings, recipient, StringEscapeUtils.unescapeHtml("[" + settings.getGameName() + "] " + subject));

            //TODO Refactoring
            //mail.addBcc(bbcRecipients);
            //mail.setBodyText("bodyText");
            //send(message);

            try {
                postoffice.send(mail);
            } catch (Exception e) {
                LOG.error("Failed to send rudelmail e-mail", e);
            }
        } else {
            LOG.error("Tryed to sent rudelmail, but subject, messages or recipients was empty");
        }
    }

    private Mail getMailInstance(final Settings settings, final String recipient, String subject) {
        Mail mail = mailProvider.get();
        mail.setCharset("UTF-8");
        mail.setFrom(ninjaProperties.get("mailservice.from"));
        mail.addReplyTo(ninjaProperties.get("mailservice.replyto"));
        mail.addTo(recipient);
        mail.setSubject(subject);

        return mail;
    }
}