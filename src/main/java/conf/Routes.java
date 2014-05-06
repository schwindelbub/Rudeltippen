package conf;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.AdminController;
import controllers.ApplicationController;
import controllers.AuthController;
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
        router.GET().route("/system/init").with(SystemController.class, "init");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");
        //router.GET().route("/.*").with(ApplicationController.class, "index");

        /**
        # Home page
        GET     /                                       Application.index

        # Ignore favicon requests
        GET     /favicon.ico                            404

        # Map static resources from the /app/public folder to the /public path
        GET     /public/                                staticDir:public
        GET     /robots.txt                             staticFile:public/robots.txt

        # App routes
        GET     /rules                              application.rules
        GET     /statistics                         application.statistics
        GET     /auth/confirm/{token}               auth.confirm
        GET     /auth/password/{token}              auth.password
        GET     /tips/playday/{number}              tips.playday
        GET     /standings                          tips.standings
        GET     /overview/playday/{number}          overview.playday
        GET     /overview/playday/{number}/{start}  overview.lazy
        GET     /overview/extras/{number}           overview.extras
        GET     /admin/changeactive/{userid}        admin.changeactive
        GET     /admin/changeadmin/{userid}         admin.changeadmin
        GET     /admin/deleteuser/{userid}          admin.deleteuser
        GET     /admin/results/{number}             admin.results
        GET     /admin/runjob/{name}                admin.runjob
        GET     /admin/jobstatus/{name}             admin.jobstatus
        GET     /users/show/{username}              users.show
        POST    /users/updatepicture/{picture}      users.updatepicture
        GET     /tournament/brackets                tournament.brackets
        GET     /tournament/playday/{number}        tournament.playday
        GET     /system/updatekickoff/{number}      system.updatekickoff
        POST    /ajax/game/webserviceid/{gameid}    ajax.webserviceid
        POST    /ajax/game/kickoff/{gameid}         ajax.kickoff
        POST    /ajax/bracket/place/{teamid}        ajax.place
        GET     /ajax/bracket/updateble/{bracketid} ajax.updateblebracket
        GET     /ajax/game/updateble/{gameid}       ajax.updateblegame
         *       /{controller}/{action}              {controller}.{action}
         *       */
    }

}
