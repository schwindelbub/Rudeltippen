package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Extra;
import models.Game;
import models.User;
import models.enums.ConfirmationType;
import ninja.NinjaTest;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public class TestMailService extends NinjaTest {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static int port = 28018;
    private static MongodExecutable mongodExecutable;

    @Before
    public void init() throws IOException {
        mongodExecutable = starter.prepare(new MongodConfigBuilder()
        .version(Version.Main.V2_5)
        .net(new Net(port, false))
        .build());

        mongodExecutable.start();

        DataService ds = getInjector().getInstance(DataService.class);
        ds.setMongoClient(new MongoClient("localhost", port));
    }

    @Test
    public void testReminder() {
        User user = new User();
        user.setEmail("sk@svenkubiak.de");

        List<Game> games = new ArrayList<Game>();
        List<Extra> extras = new ArrayList<Extra>();

        getInjector().getInstance(MailService.class).reminder(user, games, extras);
    }

    @Test
    public void testConfirm() {
        User user = new User();
        user.setEmail("sk@svenkubiak.de");

        getInjector().getInstance(MailService.class).confirm(user, "foo", ConfirmationType.ACTIVATION);
    }

    @Test
    public void testNewUser() {
        User user = new User();
        user.setEmail("sk@svenkubiak.de");

        User admin = new User();
        admin.setEmail("sk@svenkubiak.com");

        getInjector().getInstance(MailService.class).newuser(user, admin);
    }

    @Test
    public void testError() {
        getInjector().getInstance(MailService.class).error("foo", "sk@svenkubiak.de");
    }

    @Test
    public void testNotifications() {
        User user = new User();
        user.setEmail("sk@svenkubiak.de");

        getInjector().getInstance(MailService.class).notifications("foo","bar", user);
    }

    @Test
    public void testSendGameTips() {
        User user = new User();
        user.setEmail("sk@svenkubiak.de");

        List<Game> games = new ArrayList<Game>();

        getInjector().getInstance(MailService.class).gametips(user, games);
    }

    @Test
    public void testSendRudelmail() {
        String [] recipients = new String [1];
        recipients[0] = "sk@svenkubiak.de";

        getInjector().getInstance(MailService.class).rudelmail("foo", "bar", recipients, "sk@svenkubiak.com");
    }
}