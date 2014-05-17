package main;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.User;
import models.enums.Avatar;
import ninja.NinjaTest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import services.AuthService;
import services.CommonService;
import services.DataService;
import services.ImportService;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;

public class TestBase extends NinjaTest {
    private static final Map<String, String> EMPTYHEADERS = new HashMap<String, String>();
    private static final Logger LOG = LoggerFactory.getLogger(TestBase.class);
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static final int port = 28018;
    private static MongodExecutable mongodExecutable;
    public static final String ADMIN = "admin";
    public static final String USER = "user";

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
            
            getInjector().getInstance(ImportService.class).loadInitialData();

            User user = new User();
            final String salt = DigestUtils.sha512Hex(UUID.randomUUID().toString());
            user.setSalt(salt);
            user.setEmail("user@foo.bar");
            user.setUsername("user");
            user.setUserpass(getInjector().getInstance(AuthService.class).hashPassword("user", salt));
            user.setRegistered(new Date());
            user.setExtraPoints(0);
            user.setTipPoints(0);
            user.setPoints(0);
            user.setActive(true);
            user.setAdmin(false);
            user.setReminder(true);
            user.setNotification(true);
            user.setSendGameTips(true);
            user.setSendStandings(true);
            user.setCorrectResults(0);
            user.setCorrectDifferences(0);
            user.setCorrectTrends(0);
            user.setCorrectExtraTips(0);
            user.setPicture(getInjector().getInstance(CommonService.class).getUserPictureUrl(Avatar.GRAVATAR, user));
            user.setAvatar(Avatar.GRAVATAR);
            dataService.save(user);  
        } catch (Exception e) {
            LOG.error("Failed to start in memory mongodb for testing", e);
        }
    }
    
    public void doLogin(String username, String userpass) {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("username", username);
        formParameters.put("userpass", username);
        ninjaTestBrowser.makePostRequestWithFormParameters(getServerAddress() + "/auth/authenticate", EMPTYHEADERS, formParameters);
    }
    
    public void doLogout() {
        ninjaTestBrowser.makeRequest(getServerAddress() + "/auth/logout");
    }

    public BasicCookieStore getCookies() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        List<Cookie> cookies = ninjaTestBrowser.getCookies();
        for (Cookie cookie : cookies) {
            cookieStore.addCookie(cookie);
        }
        
        return cookieStore;
    }

    public HttpResponse makeGetRequest(String url, boolean disableRedirect) throws IOException, ClientProtocolException {
        HttpClient httpclient = null;
        
        if (disableRedirect) {
            httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        } else {
            httpclient = HttpClientBuilder.create().build();
        }
        
        return httpclient.execute(new HttpGet(getServerAddress() + url));
    }

    public HttpResponse makePostRequest(String url, boolean redirect) throws IOException, ClientProtocolException {
        HttpClient httpclient = null;
        
        if (redirect) {
            httpclient = HttpClientBuilder.create().build();
        } else {
            httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        }
        
        return httpclient.execute(new HttpPost(getServerAddress() + url));
    }

    @After
    public void shutdown() {
        mongodExecutable.stop();
    }
}