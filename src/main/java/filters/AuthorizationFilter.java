package filters;

import models.User;
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
public class AuthorizationFilter implements Filter {

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        User connectedUser = context.getAttribute("connectedUser", User.class);
        if (connectedUser == null || !connectedUser.isAdmin()) {
            return Results.forbidden();
        }

        return filterChain.next(context);
    }
}