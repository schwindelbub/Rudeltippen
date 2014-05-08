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
import ninja.params.PathParam;
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
public class OverviewController extends RootController {

    @Inject
    private DataService dataService;

    public Result playday(@PathParam("number") long number) {
        final Pagination pagination = AppUtils.getPagination(number, "/overview/playday/", dataService.findAllPlaydaysOrderByNumber().size());

        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());
        final List<User> users = dataService.findActiveUsers(15);
        final List<Map<User, List<GameTip>>> tips = dataService.findPlaydayTips(playday, users);
        final long usersCount = dataService.countAllUsers();

        return Results.html()
                .render("tips", tips)
                .render("playday", playday)
                .render("pagination", pagination)
                .render("usersCount", usersCount);
    }

    public Result extras() {
        final List<User> users = dataService.findAllActiveUsersOrderedByPlace();
        final List<Extra> extras = dataService.findAllExtras();
        final List<Map<User, List<ExtraTip>>> tips = dataService.findExtraTips(users, extras);

        return Results.html()
                .render("tips", tips)
                .render("extras", extras);
    }
}