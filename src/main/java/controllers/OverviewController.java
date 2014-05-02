package controllers;

import java.util.List;
import java.util.Map;

import com.google.inject.Singleton;

import ninja.Result;
import ninja.Results;
import models.Extra;
import models.ExtraTip;
import models.GameTip;
import models.Pagination;
import models.Playday;
import models.User;
import utils.AppUtils;
import utils.ViewUtils;

@Singleton
public class OverviewController {
    public Result playday(final long number) {
        final Pagination pagination = ViewUtils.getPagination(number, "/overview/playday/");

        final Playday playday = Playday.find("byNumber", pagination.getNumberAsInt()).first();
        final List<User> users = User.find("SELECT u FROM User u WHERE active = true ORDER BY place ASC").from(0).fetch(15);
        final List<Map<User, List<GameTip>>> tips = AppUtils.getPlaydayTips(playday, users);
        final long usersCount = User.count();

        return Results.html().render(tips).render(playday).render(pagination).render(userCount);
    }

    public Result extras() {
        final List<User> users = User.find("SELECT u FROM User u WHERE active = true ORDER BY place ASC").fetch();
        final List<Extra> extras = Extra.findAll();
        final List<Map<User, List<ExtraTip>>> tips =  AppUtils.getExtraTips(users, extras);

        return Results.html().render(tips).render(extras);
    }

    public Result lazy(final int number, final int start) {
        final Playday playday = Playday.find("byNumber", number).first();
        final List<User> users = User.find("SELECT u FROM User u WHERE active = true ORDER BY place ASC").from(start).fetch(15);
        final List<Map<User, List<GameTip>>> tips = AppUtils.getPlaydayTips(playday, users);

        return Results.html().render(tips);
    }
}