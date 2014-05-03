package services;

import org.apache.commons.lang.StringUtils;

import models.Constants;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class I18nService {

    @Inject
    private Messages messages;

    @Inject
    private NinjaProperties ninjaProperties;

    public String get(String key) {
        return messages.getWithDefault(key, "Missing translation for: " + key, Optional.of("en"), null);
    }

    /**
     * Returns a Message with the difference to place 1
     * 
     * @param pointsDiff The difference in points
     * @return Message with difference or empty string
     */
    public String getDiffToTop(final int pointsDiff) {
        //TODO Refactoring
        //        String message = "";
        //        if (pointsDiff == 1) {
        //            message = get("points.to.top.one", pointsDiff);
        //        } else if (pointsDiff > 1) {
        //            message = get("points.to.top.many", pointsDiff);
        //        }
        //
        //        return message;
        return null;
    }

    /**
     * Returns the current timezone configured in application.conf or
     * the default timezone defined in IAppConstants
     * 
     * @return The String value of the timezone
     */
    public String getCurrentTimeZone() {
        String timezone = ninjaProperties.get("app.timezone");
        if (StringUtils.isBlank(timezone)) {
            timezone = Constants.DEFAULT_TIMEZONE.value();
        }
        return timezone;
    }
}
