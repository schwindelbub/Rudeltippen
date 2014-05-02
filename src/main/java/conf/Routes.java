package conf;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.ApplicationController;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {

        router.GET().route("/").with(ApplicationController.class, "index");
        router.GET().route("/hello_world.json").with(ApplicationController.class, "helloWorldJson");


        ///////////////////////////////////////////////////////////////////////
        // Assets (pictures / javascript)
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/assets/webjars/{fileName: .*}").with(AssetsController.class, "serveWebJars");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");

        ///////////////////////////////////////////////////////////////////////
        // Index / Catchall shows index page
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/.*").with(ApplicationController.class, "index");

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
