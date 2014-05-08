package conf;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.AdminController;
import controllers.ApplicationController;
import controllers.AuthController;
import controllers.OverviewController;
import controllers.SystemController;
import controllers.TipController;
import controllers.TournamentController;
import controllers.UserController;

/**
 * 
 * @author svenkubiak
 *
 */
public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {
        router.GET().route("/").with(ApplicationController.class, "index");
        router.GET().route("/auth/login").with(AuthController.class, "login");
        router.GET().route("/auth/logout").with(AuthController.class, "logout");
        router.GET().route("/auth/register").with(AuthController.class, "register");
        router.GET().route("/auth/forgotten").with(AuthController.class, "forgotten");
        router.GET().route("/admin/rudelmail").with(AdminController.class, "rudelmail");
        router.GET().route("/admin/settings").with(AdminController.class, "settings");
        router.GET().route("/admin/users").with(AdminController.class, "users");
        router.GET().route("/admin/changeactive/{userid}").with(AdminController.class, "changeactive");
        router.GET().route("/admin/changeadmin/{userid}").with(AdminController.class, "changeadmin");
        router.GET().route("/admin/deleteuser/{userid}").with(AdminController.class, "deleteuser");
        router.GET().route("/admin/results/{number}").with(AdminController.class, "results");
        router.GET().route("/admin/tournament").with(AdminController.class, "tournament");
        router.GET().route("/overview/playday/{number}").with(OverviewController.class, "playday");
        router.GET().route("/overview/extras").with(OverviewController.class, "extras");
        router.GET().route("/rules").with(ApplicationController.class, "rules");
        router.GET().route("/tournament/brackets").with(TournamentController.class, "brackets");
        router.GET().route("/users/profile").with(UserController.class, "profile");
        router.GET().route("/users/show/{username}").with(UserController.class, "show");
        router.GET().route("/tournament/playday/{number}").with(TournamentController.class, "playday");
        router.GET().route("/tips/playday/{number}").with(TipController.class, "playday");        
        router.GET().route("/tips/standings").with(TipController.class, "standings");
        router.POST().route("/auth/renew").with(AuthController.class, "renew");
        router.POST().route("/auth/reset").with(AuthController.class, "reset");
        router.POST().route("/auth/authenticate").with(AuthController.class, "authenticate");
        router.POST().route("/auth/create").with(AuthController.class, "create");
        router.GET().route("/setup").with(SystemController.class, "setup");
        router.GET().route("/statistics").with(ApplicationController.class, "statistics");
        router.GET().route("/system/init").with(SystemController.class, "init");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");
        router.GET().route("/robots.txt").with(AssetsController.class, "serveStatic");
    }
}