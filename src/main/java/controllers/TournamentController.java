package controllers;

import java.util.List;

import services.DataService;
import utils.AppUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import models.Bracket;
import models.Pagination;
import models.Playday;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class TournamentController extends RootController {

    @Inject
    private DataService dataService;

    public Result brackets() {
        final List<Bracket> brackets = dataService.findAllBrackets();
        return Results.html().render("brackets", brackets);
    }

    public Result playday(@PathParam("number") long number) {
        final Pagination pagination = AppUtils.getPagination(number, "/tournament/playday/", dataService.findAllPlaydaysOrderByNumber().size());
        final Playday playday = dataService.findPlaydaybByNumber(pagination.getNumberAsInt());

        return Results.html().render("playday", playday).render("pagination", pagination);
    }

    public Result bracket() {
        return Results.html();
    }
}