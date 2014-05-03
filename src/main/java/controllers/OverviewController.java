package controllers;

import java.util.List;
import java.util.Map;

import models.Extra;
import models.ExtraTip;
import models.GameTip;
import models.Pagination;
import models.Playday;
import models.User;
import ninja.Result;
import ninja.Results;
import services.DataService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class OverviewController {

    @Inject
    private DataService dataService;

    public Result playday(final long number) {
        final Pagination pagination = AppUtils.getPagination(number, "/overview/playday/", dataService.findAllPlaydaysOrderByNumber().size());

        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());
        final List<User> users = dataService.findActiveUsers(15);
        final List<Map<User, List<GameTip>>> tips = dataService.getPlaydayTips(playday, users);
        final long usersCount = dataService.getUsersCount();

        return Results.html().render(tips).render(playday).render(pagination).render(usersCount);
    }

    public Result extras() {
        final List<User> users = dataService.findAllActiveUsersOrderedByPlace();
        final List<Extra> extras = dataService.findAllExtras();
        final List<Map<User, List<ExtraTip>>> tips = dataService.getExtraTips(users, extras);

        return Results.html().render(tips).render(extras);
    }

    public Result lazy(final int number, final int start) {
        final Playday playday = dataService.findPlaydaybByNumber(number);
        final List<User> users = dataService.findActiveUsers(15);
        final List<Map<User, List<GameTip>>> tips = dataService.getPlaydayTips(playday, users);

        return Results.html().render(tips);
    }
}