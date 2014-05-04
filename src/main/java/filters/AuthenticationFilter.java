package filters;

import models.Constants;
import ninja.Context;
import ninja.Cookie;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;

import org.apache.commons.lang3.StringUtils;

import services.AuthService;
import services.DataService;

import com.google.inject.Inject;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticationFilter implements Filter {

    @Inject
    private AuthService authService;

    @Inject
    private DataService dataService;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        Result result = filterChain.next(context);
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
        } else {
            result.render("connectedUser", dataService.findUserByUsernameOrEmail(context.getSession().get(Constants.USERNAME.value())));
            result.render("currentPlayday", dataService.findCurrentPlayday());
        }

        return result;
    }
}