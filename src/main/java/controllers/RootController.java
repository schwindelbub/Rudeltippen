package controllers;

import models.User;
import utils.AppUtils;

public class RootController {
    @Before
    protected static void init() {
        AppUtils.setAppLanguage();

        if (!AppUtils.appIsInizialized()) {
            redirect("/system/setup");
        }

        final User connectedUser = AppUtils.getConnectedUser();
        if (connectedUser != null) {
            renderArgs.put("connectedUser", connectedUser);
        } else {
            renderArgs.put("connectedUser", null);
        }

        renderArgs.put("currentPlayday", AppUtils.getCurrentPlayday());
    }
}