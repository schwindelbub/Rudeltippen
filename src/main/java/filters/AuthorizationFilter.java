package filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthorizationFilter implements Filter {

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        return filterChain.next(context);
    }
}