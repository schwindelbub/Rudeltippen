package controllers;

import java.text.SimpleDateFormat;
import java.util.Locale;

import models.Bracket;
import models.Game;
import models.Team;
import ninja.Result;

import org.apache.commons.lang.StringUtils;

public class AjaxController {

    public Result webserviceid(final long gameid) {
        Game game = Game.findById(gameid);
        if (game != null) {
            final String webserviceID = params.get("value");
            if (StringUtils.isNotBlank(webserviceID)) {
                game.setWebserviceID(webserviceID);
                game._save();
                ok();
            }
        }
        badRequest();
    }

    public Result kickoff(final long gameid) {
        Game game = Game.findById(gameid);
        if (game != null) {
            final String kickoff = params.get("value");
            if (StringUtils.isNotBlank(kickoff)) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.ENGLISH);
                    game.setKickoff(simpleDateFormat.parse(kickoff));
                    game.setUpdateble(false);
                    game._save();
                } catch (Exception e) {
                    badRequest();
                }
                ok();
            }
        }
        badRequest();
    }

    public Result place(final long teamid) {
        Team team = Team.findById(teamid);
        if (team != null) {
            final String place = params.get("value");
            if (StringUtils.isNotBlank(place)) {
                team.setPlace(Integer.valueOf(place));
                team._save();

                Bracket bracket = team.getBracket();
                bracket.setUpdateble(false);
                bracket._save();

                ok();
            }
        }
        badRequest();
    }

    public Result updateblegame(final long gameid) {
        Game game = Game.findById(gameid);
        if (game != null) {
            game.setUpdateble(!game.isUpdateble());
            game._save();
            ok();
        }
        badRequest();
    }

    public Result updateblebracket(final long bracketid) {
        Bracket bracket = Bracket.findById(bracketid);
        if (bracket != null) {
            bracket.setUpdateble(!bracket.isUpdateble());
            bracket._save();
            ok();
        }
        badRequest();
    }
}