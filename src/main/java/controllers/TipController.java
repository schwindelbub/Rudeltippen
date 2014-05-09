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
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import ninja.session.FlashScope;

import org.apache.commons.lang.StringUtils;

import services.CommonService;
import services.DataService;
import services.I18nService;
import services.ValidationService;

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
    
    @Inject
    private I18nService i18nService;
    
    @Inject
    private ValidationService validationService;

    @Inject
    private CommonService commonService;
    
    public Result playday(@PathParam("number") long number) {
        final Pagination pagination = commonService.getPagination(number, "/tips/playday/", dataService.findAllPlaydaysOrderByNumber().size());
        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());

        final List<Extra> extras = dataService.findAllExtras();
        final boolean tippable = commonService.extrasAreTipable(extras);

        return Results.html().render(playday).render(number).render(pagination).render(extras).render(tippable);
    }

    public Result storetips(FlashScope flashScope, Context context) {
        int tipped = 0;
        int playday = 1;
        final List<String> keys = new ArrayList<String>();
        final Map<String, String> map = commonService.convertParamaters(context.getParameters());
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

                if (!validationService.isValidScore(homeScore, awayScore)) {
                    continue;
                }

                final Game game = dataService.findGameById(key);
                if (game == null) {
                    continue;
                }

                dataService.saveGameTip(game, Integer.parseInt(homeScore), Integer.parseInt(awayScore), context.getAttribute("connectedUser", User.class));
                keys.add(key);
                tipped++;

                playday = game.getPlayday().getNumber();
            }
        }

        if (tipped > 0) {
            flashScope.success(i18nService.get("controller.tipps.tippsstored"));
        } else {
            flashScope.put("warning", i18nService.get("controller.tipps.novalidtipps"));
        }

        return Results.redirect("/tips/playday/" + playday);
    }

    public Result storeextratips(FlashScope flashScope, Context context) {
        final Map<String, String> map = commonService.convertParamaters(context.getParameters());
        for (final Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();

            if (StringUtils.isNotBlank(key) && key.contains("bonus_") && key.contains("_teamId")) {
                final String teamdId = context.getParameter(key);
                key = key.replace("bonus_", "");
                key = key.replace("_teamId", "");
                key = key.trim();

                final String bId = key;
                final String tId = teamdId;

                if (StringUtils.isNotBlank(bId) || StringUtils.isNotBlank(tId)) {
                    return Results.redirect("/tips/playday/" + dataService.findCurrentPlayday().getNumber());
                }

                final Extra extra = dataService.findExtaById(bId);
                if (commonService.extraIsTipable(extra)) {
                    final Team team = dataService.findTeamById(tId);
                    dataService.saveExtraTip(extra, team, context.getAttribute("connectedUser", User.class));
                    flashScope.success(i18nService.get("controller.tipps.bonussaved"));
                }
            }
        }

        return Results.redirect("/tips/playday/" + dataService.findCurrentPlayday().getNumber());
    }

    public Result standings() {
        final List<User> users = dataService.findAllActiveUsersOrderedByPlace();
        return Results.html().render("users", users);
    }
}