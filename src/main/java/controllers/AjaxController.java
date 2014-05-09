package controllers;

import java.text.SimpleDateFormat;
import java.util.Locale;

import models.Bracket;
import models.Game;
import models.Team;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;

import org.apache.commons.lang.StringUtils;

import services.DataService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import filters.AuthorizationFilter;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
@FilterWith(AuthorizationFilter.class)
public class AjaxController extends RootController {

    @Inject
    private DataService dataService;

    public Result webserviceid(@PathParam("gameId") String gameId, Context context) {
        Game game = dataService.findGameById(gameId);
        if (game != null) {
            final String webserviceID = context.getParameter("value");
            if (StringUtils.isNotBlank(webserviceID)) {
                game.setWebserviceID(webserviceID);
                dataService.save(game);

                return Results.ok();
            }
        }
        return Results.badRequest();
    }

    public Result kickoff(@PathParam("gameId") String gameId, Context context) {
        Game game = dataService.findGameById(gameId);
        if (game != null) {
            final String kickoff = context.getParameter("value");
            if (StringUtils.isNotBlank(kickoff)) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.ENGLISH);
                    game.setKickoff(simpleDateFormat.parse(kickoff));
                    game.setUpdateble(false);
                    dataService.save(game);
                } catch (Exception e) {
                    return Results.badRequest();
                }
                return Results.ok();
            }
        }
        return Results.badRequest();
    }

    public Result place(@PathParam("teamId") String teamId, Context context) {
        Team team = dataService.findTeamById(teamId);
        if (team != null) {
            final String place = context.getParameter("value");
            if (StringUtils.isNotBlank(place)) {
                team.setPlace(Integer.valueOf(place));
                dataService.save(team);

                Bracket bracket = team.getBracket();
                bracket.setUpdateble(false);
                dataService.save(bracket);

                return Results.ok();
            }
        }
        return Results.badRequest();
    }

    public Result updateblegame(@PathParam("gameId") String gameId) {
        Game game = dataService.findGameById(gameId);
        if (game != null) {
            game.setUpdateble(!game.isUpdateble());
            dataService.save(game);

            return Results.ok();
        }
        return Results.badRequest();
    }

    public Result updateblebracket(@PathParam("bracketId") String bracketId) {
        Bracket bracket = dataService.findBracketById(bracketId);
        if (bracket != null) {
            bracket.setUpdateble(!bracket.isUpdateble());
            dataService.save(bracket);

            return Results.ok();
        }
        return Results.badRequest();
    }
}