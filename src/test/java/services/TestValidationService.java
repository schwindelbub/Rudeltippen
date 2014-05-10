package services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import ninja.NinjaTest;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public class TestValidationService extends NinjaTest {
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
    public void testIsValidEmail() {
        ValidationService validationService = getInjector().getInstance(ValidationService.class);

        assertTrue(validationService.isValidEmail("sk@svenkubiak.de"));
        assertTrue(validationService.isValidEmail("peter.pong@plong.com"));
        assertTrue(validationService.isValidEmail("han.solo.senior@sub.domain.com"));
        assertFalse(validationService.isValidEmail("sk"));
        assertFalse(validationService.isValidEmail("sk@"));
        assertFalse(validationService.isValidEmail("@"));
        assertFalse(validationService.isValidEmail("@com.de"));
        assertFalse(validationService.isValidEmail("sk@.de"));
    }

    @Test
    public void testUsernameAndEmail() {
        ValidationService validationService = getInjector().getInstance(ValidationService.class);

        assertTrue(validationService.isValidUsername("ahf_bA-SS747"));
        assertFalse(validationService.isValidUsername("ahf_bA-SS 747"));
        assertFalse(validationService.isValidUsername("ahf_bA-SS/747"));

        assertTrue(validationService.emailExists("user1@rudeltippen.de"));
        assertTrue(validationService.isValidEmail("user1@rudeltippen.de"));
        assertFalse(validationService.emailExists("foobar555@bar.com"));
        assertTrue(validationService.usernameExists("user5"));
    }

    @Test
    public void testValidScore() {
        ValidationService validationService = getInjector().getInstance(ValidationService.class);

        assertTrue(validationService.isValidScore("0", "0"));
        assertTrue(validationService.isValidScore("99", "99"));
        assertFalse(validationService.isValidScore("-1", "-1"));
        assertFalse(validationService.isValidScore("a", "b"));
        assertFalse(validationService.isValidScore("100", "1"));
        assertFalse(validationService.isValidScore("1", "100"));
        assertFalse(validationService.isValidScore("-1", "1"));
        assertFalse(validationService.isValidScore("1", "-51"));
    }
}