package filters;

import javax.inject.Inject;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.i18n.Lang;
import services.I18nService;

/**
 * 
 * @author svenkubiak
 *
 */
public class LanguageFilter implements Filter {
    
    @Inject
    private Lang lang;

    @Inject
    private I18nService i18nService;
    
    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Result result = filterChain.next(context);
        lang.setLanguage(i18nService.getDefaultLanguage(), result);
        
        return result;
    }
}