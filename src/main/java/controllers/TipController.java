package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.Extra;
import models.Game;
import models.Pagination;
import models.Playday;
import models.Team;
import models.User;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;

import org.apache.commons.lang.StringUtils;

import services.DataService;
import utils.AppUtils;
import utils.ValidationUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class TipController extends RootController {

    @Inject
    private DataService dataService;

    public Result playday(@PathParam("number") long number) {
        final Pagination pagination = AppUtils.getPagination(number, "/tips/playday/", dataService.findAllPlaydaysOrderByNumber().size());
        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());

        final List<Extra> extras = dataService.findAllExtras();
        final boolean tippable = AppUtils.extrasTipable(extras);

        return Results.html().render(playday).render(number).render(pagination).render(extras).render(tippable);
    }

    //TODO Refactoring
    public Result storetips() {
        int tipped = 0;
        int playday = 1;
        final List<String> keys = new ArrayList<String>();
        final Map<String, String> map = null;//params.allSimple();
        for (final Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isNotBlank(key) && key.contains("game_") && (key.contains("_homeScore") || key.contains("_awayScore"))) {
                key = key.replace("game_", "");
                key = key.replace("_awayScore", "");
                key = key.replace("_homeScore", "");
                key = key.trim();

                if (keys.contains(key)) {
                    continue;
                }

                final String homeScore = map.get("game_" + key + "_homeScore");
                final String awayScore = map.get("game_" + key + "_awayScore");

                if (!ValidationUtils.isValidScore(homeScore, awayScore)) {
                    continue;
                }

                final Game game = dataService.findGameById(Long.parseLong(key));
                if (game == null) {
                    continue;
                }

                dataService.placeTip(game, Integer.parseInt(homeScore), Integer.parseInt(awayScore));
                keys.add(key);
                tipped++;

                playday = game.getPlayday().getNumber();
            }
        }
        //TODO Refactoring
        if (tipped > 0) {
            //flash.put("infomessage", Messages.get("controller.tipps.tippsstored"));
        } else {
            //flash.put("warningmessage", Messages.get("controller.tipps.novalidtipps"));
        }
        //flash.keep();

        return Results.redirect("/tips/playday/" + playday);
    }

    //TODO Refactroing
    public Result storeextratips() {
        final Map<String, String> map = null;//params.allSimple();
        for (final Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();

            if (StringUtils.isNotBlank(key) && key.contains("bonus_") && key.contains("_teamId")) {
                final String teamdId = null;//params.get(key);
                key = key.replace("bonus_", "");
                key = key.replace("_teamId", "");
                key = key.trim();

                final String bId = key;
                final String tId = teamdId;
                Long bonusTippId = null;
                Long teamId = null;

                if (StringUtils.isNotBlank(bId) && StringUtils.isNotBlank(tId)) {
                    bonusTippId = Long.parseLong(bId);
                    teamId = Long.parseLong(tId);
                } else {
                    playday(dataService.findCurrentPlayday().getNumber());
                }

                final Extra extra = dataService.findExtaById(bonusTippId);
                if (extra.isTipable()) {
                    final Team team = dataService.findTeamById(teamId);
                    dataService.placeExtraTip(extra, team);
                    //flash.put("infomessage", Messages.get("controller.tipps.bonussaved"));
                    //flash.keep();
                }
            }
        }

        return Results.redirect("/tips/playday/" + dataService.findCurrentPlayday().getNumber());
    }

    public Result standings() {
        final List<User> users = dataService.findAllActiveUsersOrderedByPlace();

        return Results.html().render(users);
    }
}