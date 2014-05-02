package filters;

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
public class AuthenticationFilter implements Filter {
    public static final String USERNAME = "username";

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        if (context.getSession() == null || context.getSession().get(USERNAME) == null) {
            return Results.redirect("/auth/login");
        }

        return filterChain.next(context);
    }
}