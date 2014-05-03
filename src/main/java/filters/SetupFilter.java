package filters;

import services.DataService;

import com.google.inject.Inject;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;

/**
 * 
 * @author svenkubiak
 *
 */
public class SetupFilter implements Filter {

    @Inject
    private DataService dataService;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        if (!dataService.appIsInizialized()) {
            return Results.redirect("/setup");
        }

        return filterChain.next(context);
    }
}