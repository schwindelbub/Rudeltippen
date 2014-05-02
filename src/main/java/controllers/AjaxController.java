package controllers;

import java.text.SimpleDateFormat;
import java.util.Locale;

import models.Bracket;
import models.Game;
import models.Team;
import ninja.Result;
import ninja.Results;

import org.apache.commons.lang.StringUtils;

import services.DataService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AjaxController {

    @Inject
    private DataService dataService;

    //TODO Refactoring
    public Result webserviceid(final long gameid) {
        Game game = dataService.findGameById(gameid);
        if (game != null) {
            final String webserviceID = null;//params.get("value");
            if (StringUtils.isNotBlank(webserviceID)) {
                game.setWebserviceID(webserviceID);
                dataService.save(game);

                return Results.ok();
            }
        }
        return Results.badRequest();
    }

    //TODO Refactoring
    public Result kickoff(final long gameid) {
        Game game = dataService.findGameById(gameid);
        if (game != null) {
            final String kickoff = null;//params.get("value");
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

    //TODO Refactoring
    public Result place(final long teamid) {
        Team team = dataService.findTeamById(teamid);
        if (team != null) {
            final String place = null;//params.get("value");
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

    public Result updateblegame(final long gameid) {
        Game game = dataService.findGameById(gameid);
        if (game != null) {
            game.setUpdateble(!game.isUpdateble());
            dataService.save(game);

            return Results.ok();
        }
        return Results.badRequest();
    }

    public Result updateblebracket(final long bracketid) {
        Bracket bracket = dataService.findBracketById(bracketid);
        if (bracket != null) {
            bracket.setUpdateble(!bracket.isUpdateble());
            dataService.save(bracket);

            return Results.ok();
        }
        return Results.badRequest();
    }
}