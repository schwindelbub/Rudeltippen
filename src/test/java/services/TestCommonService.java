package services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Extra;
import models.Game;
import ninja.NinjaTest;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public class TestCommonService extends NinjaTest {
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
    public void testHashPassword() {
        assertEquals(getInjector().getInstance(AuthService.class).hashPassword("user22", "foo"), "bc423bd3c40919ed73f470d4182e2292f368607225f12f48599b9e9d2bea97586e0d930792e5381f1ebb75177abd75fc1a2ef879b2f275535d14e550c8d17b8b");
    }

    @Test
    public void testExtraTippable() {
        CommonService commonService = getInjector().getInstance(CommonService.class);

        Extra extra = new Extra();
        extra.setEnding(new Date());

        Extra extra2 = new Extra();
        extra2.setEnding(new Date(1371732121344L));

        List<Extra> extras = new ArrayList<Extra>();
        extras.add(extra);
        extras.add(extra2);

        assertFalse(commonService.extrasAreTipable(extras));

        extra.setEnding(new Date(9371732121344L));
        extra2.setEnding(new Date(9371732121344L));

        extras = new ArrayList<Extra>();
        extras.add(extra);
        extras.add(extra2);

        assertTrue(commonService.extrasAreTipable(extras));
    }

    @Test
    public void testAllReferencedGamesEnded() {
        CommonService commonService = getInjector().getInstance(CommonService.class);

        Game game = new Game();
        game.setEnded(true);

        Game game2 = new Game();
        game2.setEnded(false);

        List<Game> games = new ArrayList<Game>();
        games.add(game);
        games.add(game2);

        assertFalse(commonService.allReferencedGamesEnded(games));

        game2.setEnded(true);

        games = new ArrayList<Game>();
        games.add(game);
        games.add(game2);

        assertTrue(commonService.allReferencedGamesEnded(games));
    }

    @Test
    public void testGetTippPoints() {
        ResultService resultService = getInjector().getInstance(ResultService.class);

        final int pointsTipp = 4;
        final int pointsDiff = 2;
        final int pointsTrend = 1;

        assertEquals(resultService.getTipPoints(1, 0, 1, 0), pointsTipp);
        assertEquals(resultService.getTipPoints(0, 1, 0, 1), pointsTipp);
        assertEquals(resultService.getTipPoints(1, 1, 1, 1), pointsTipp);
        assertEquals(resultService.getTipPoints(2, 0, 5, 3), pointsDiff);
        assertEquals(resultService.getTipPoints(0, 2, 3, 5), pointsDiff);
        assertEquals(resultService.getTipPoints(2, 2, 1, 1), pointsDiff);
        assertEquals(resultService.getTipPoints(1, 0, 4, 0), pointsTrend);
        assertEquals(resultService.getTipPoints(0, 1, 0, 4), pointsTrend);
        assertEquals(resultService.getTipPointsTrend(1, 0, 3, 0), pointsTrend);
        assertEquals(resultService.getTipPointsTrend(4, 5, 3, 7), pointsTrend);
        assertEquals(resultService.getTipPointsTrend(1, 2, 2, 1), 0);
        assertEquals(resultService.getTipPointsOvertime(1, 1, 5, 4, 1, 1), pointsTipp);
        assertEquals(resultService.getTipPointsOvertime(1, 1, 5, 4, 0, 0), pointsDiff);
        assertEquals(resultService.getTipPointsOvertime(1, 1, 5, 4, 1, 0), 0);
        assertEquals(resultService.getTipPointsOvertime(1, 1, 4, 5, 0, 1), 0);
    }
}