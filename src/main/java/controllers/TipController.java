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

import org.apache.commons.lang.StringUtils;

import services.ValidationService;
import utils.AppUtils;
import utils.ViewUtils;

public class TipController {

    public Result playday(final int number) {
        final Pagination pagination = ViewUtils.getPagination(number, "/tips/playday/");
        final Playday playday = Playday.find("byNumber", pagination.getNumberAsInt()).first();

        final List<Extra> extras = Extra.findAll();
        final boolean tippable = AppUtils.extrasTipable(extras);

        render(playday, number,pagination, extras, tippable);
    }

    public Result storetips() {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        int tipped = 0;
        int playday = 1;
        final List<String> keys = new ArrayList<String>();
        final Map<String, String> map = params.allSimple();
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

                if (!ValidationService.isValidScore(homeScore, awayScore)) {
                    continue;
                }

                final Game game = Game.findById(Long.parseLong(key));
                if (game == null) {
                    continue;
                }

                AppUtils.placeTip(game, Integer.parseInt(homeScore), Integer.parseInt(awayScore));
                keys.add(key);
                tipped++;

                playday = game.getPlayday().getNumber();
            }
        }
        if (tipped > 0) {
            flash.put("infomessage", Messages.get("controller.tipps.tippsstored"));
        } else {
            flash.put("warningmessage", Messages.get("controller.tipps.novalidtipps"));
        }
        flash.keep();

        redirect("/tips/playday/" + playday);
    }

    public Result storeextratips() {
        if (ValidationService.verifyAuthenticity()) { checkAuthenticity(); }

        final Map<String, String> map = params.allSimple();
        for (final Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();

            if (StringUtils.isNotBlank(key) && key.contains("bonus_") && key.contains("_teamId")) {
                final String teamdId = params.get(key);
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
                    playday(AppUtils.getCurrentPlayday().getNumber());
                }

                final Extra extra = Extra.findById(bonusTippId);
                if (extra.isTipable()) {
                    final Team team = Team.findById(teamId);
                    AppUtils.placeExtraTip(extra, team);
                    flash.put("infomessage", Messages.get("controller.tipps.bonussaved"));
                    flash.keep();
                }
            }
        }
        playday(AppUtils.getCurrentPlayday().getNumber());
    }

    public Result standings() {
        final List<User> users = User.find("SELECT u FROM User u WHERE active = true ORDER BY place ASC").fetch();
        render(users);
    }
}