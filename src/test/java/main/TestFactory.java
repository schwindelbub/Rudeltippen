package main;

import java.util.UUID;

import models.Settings;
import models.enums.Constants;
import ninja.NinjaTest;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.DataService;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public class TestFactory extends NinjaTest {
    private static final Logger LOG = LoggerFactory.getLogger(TestFactory.class);
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static final int port = 28018;
    private static MongodExecutable mongodExecutable;

    @Before
    public void init() {
        try {
            mongodExecutable = starter.prepare(new MongodConfigBuilder()
            .version(Version.Main.V2_6)
            .net(new Net(port, false))
            .build());

            mongodExecutable.start();

            DataService dataService = getInjector().getInstance(DataService.class);
            dataService.setMongoClient(new MongoClient("localhost", port));
            dataService.dropDatabase();

            Settings settings = new Settings();
            settings.setAppName(Constants.APPNAME.get());
            settings.setPointsGameWin(3);
            settings.setPointsGameDraw(1);
            settings.setAppSalt(DigestUtils.sha512Hex(UUID.randomUUID().toString()));
            settings.setGameName("Rudeltippen");
            settings.setPointsTip(4);
            settings.setPointsTipDiff(2);
            settings.setPointsTipTrend(1);
            settings.setMinutesBeforeTip(5);
            settings.setPlayoffs(false);
            settings.setNumPrePlayoffGames(12);
            settings.setInformOnNewTipper(true);
            settings.setEnableRegistration(true);
            dataService.save(settings);

        } catch (Exception e) {
            LOG.error("Failed to start in memory mongodb for testing", e);
        }
    }

    @After
    public void shutdown() {
        mongodExecutable.stop();
    }
}