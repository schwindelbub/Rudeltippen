package filters;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import services.AuthService;
import models.Constants;
import ninja.Context;
import ninja.Cookie;
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

    @Inject
    private AuthService authService;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Cookie cookie = context.getCookie(Constants.REMEMBERME.value());
        if (cookie != null && cookie.getValue().indexOf("-") > 0) {
            final String sign = cookie.getValue().substring(0, cookie.getValue().indexOf("-"));
            final String username = cookie.getValue().substring(cookie.getValue().indexOf("-") + 1);

            if (StringUtils.isNotBlank(sign) && StringUtils.isNotBlank(username) && authService.sign(username).equals(sign)) {
                if (context.getSession() != null) {
                    context.getSession().put(Constants.USERNAME.value(), username);
                }
            }
        }

        if (context.getSession() == null || context.getSession().get(Constants.USERNAME.value()) == null) {
            return Results.redirect("/auth/login");
        }

        return filterChain.next(context);
    }
}