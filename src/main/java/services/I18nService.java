package services;

import ninja.i18n.Messages;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class I18nService {
    @Inject
    private Messages messages;

    public String get(String key) {
        return messages.getWithDefault(key, "Missing translation for: " + key, Optional.of("en"), null);
    }
}
