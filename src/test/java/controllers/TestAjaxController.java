package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import main.TestBase;
import ninja.Result;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

public class TestAjaxController extends TestBase {
    
    @Test
    public void testWebserviceid() throws ClientProtocolException, IOException {
        String url = getServerAddress() + "/ajax/game/webserviceid/foo";
        
        HttpClient httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = httpclient.execute(new HttpPost(url));

        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogin(USER, USER);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogout();
        doLogin(ADMIN, ADMIN);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_400_BAD_REQUEST, response.getStatusLine().getStatusCode());
        
        doLogout();
    }
    
    @Test
    public void testKickoff() throws ClientProtocolException, IOException {
        String url = getServerAddress() + "/ajax/game/kickoff/foo";
        
        HttpClient httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = httpclient.execute(new HttpPost(url));

        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogin(USER, USER);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogout();
        doLogin(ADMIN, ADMIN);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_400_BAD_REQUEST, response.getStatusLine().getStatusCode());
        
        doLogout();
    }
    
    @Test
    public void testPlace() throws ClientProtocolException, IOException {
        String url = getServerAddress() + "/ajax/bracket/place/foo";
        
        HttpClient httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = httpclient.execute(new HttpPost(url));

        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogin(USER, USER);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogout();
        doLogin(ADMIN, ADMIN);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpPost(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_400_BAD_REQUEST, response.getStatusLine().getStatusCode());
        
        doLogout();
    }
    
    @Test
    public void testUpdatablegame() throws ClientProtocolException, IOException {
        String url = getServerAddress() + "/ajax/game/updatable/foo";
        
        HttpClient httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = httpclient.execute(new HttpGet(url));

        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogin(USER, USER);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpGet(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogout();
        doLogin(ADMIN, ADMIN);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpGet(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_400_BAD_REQUEST, response.getStatusLine().getStatusCode());
        
        doLogout();
    }
    
    @Test
    public void testUpdatablebracket() throws ClientProtocolException, IOException {
        String url = getServerAddress() + "/ajax/bracket/updatable/foo";
        
        HttpClient httpclient = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = httpclient.execute(new HttpGet(url));

        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogin(USER, USER);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpGet(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_303_SEE_OTHER, response.getStatusLine().getStatusCode());
        
        doLogout();
        doLogin(ADMIN, ADMIN);
        
        httpclient = HttpClientBuilder.create().setDefaultCookieStore(getCookies()).disableRedirectHandling().build();
        response = httpclient.execute(new HttpGet(url));
        
        assertNotNull(response);
        assertEquals(Result.SC_400_BAD_REQUEST, response.getStatusLine().getStatusCode());
        
        doLogout();
    }
}