package controllers;

import java.util.List;

import ninja.Result;
import models.Bracket;
import models.Pagination;
import models.Playday;

public class TournamentController {
    public Result brackets() {
        final List<Bracket> brackets = Bracket.findAll();
        render(brackets);
    }

    public Result playday(final long number) {
        final Pagination pagination = ViewUtils.getPagination(number, "/tournament/playday/");
        final Playday playday = Playday.find("byNumber", pagination.getNumberAsInt()).first();

        render(playday, pagination);
    }

    public Result bracket() {
        render();
    }
}