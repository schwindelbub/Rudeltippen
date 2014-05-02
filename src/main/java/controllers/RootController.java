package controllers;

import com.google.inject.Singleton;

import models.User;
import utils.AppUtils;

@Singleton
public class RootController {
    //TODO Refactoring
    //    @Before
    //    protected static void init() {
    //        AppUtils.setAppLanguage();
    //
    //        if (!AppUtils.appIsInizialized()) {
    //            redirect("/system/setup");
    //        }
    //
    //        final User connectedUser = AppUtils.getConnectedUser();
    //        if (connectedUser != null) {
    //            renderArgs.put("connectedUser", connectedUser);
    //        } else {
    //            renderArgs.put("connectedUser", null);
    //        }
    //
    //        renderArgs.put("currentPlayday", AppUtils.getCurrentPlayday());
    //    }
}