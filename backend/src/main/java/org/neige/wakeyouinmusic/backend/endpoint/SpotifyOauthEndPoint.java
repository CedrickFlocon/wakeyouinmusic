package org.neige.wakeyouinmusic.backend.endpoint;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.google.common.io.BaseEncoding;

import org.neige.wakeyouinmusic.backend.converter.SpotifyTokenConverter;
import org.neige.wakeyouinmusic.backend.model.SpotifyToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;


@Api(name = "spotify", version = "v1",
        namespace = @ApiNamespace(ownerDomain = "backend.wakeyouinmusic.neige.org", ownerName = "backend.wakeyouinmusic.neige.org", packagePath = ""))
public class SpotifyOauthEndPoint {

    private static final Logger log = Logger.getLogger(SpotifyOauthEndPoint.class.getName());

    private static final String URL_SPOTIFY_TOKEN = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_REDIRECT_URI = "wakeyouinmusic://callback";
    private static final String ENV_SPOTIFY_CLIENT_SECRET = "SPOTIFY_CLIENT_SECRET";
    private static final String ENV_SPOTIFY_CLIENT_ID = "SPOTIFY_CLIENT_ID";
    private final String SPOTIFY_CLIENT_SECRET;
    private final String SPOTIFY_CLIENT_ID;

    public SpotifyOauthEndPoint() {
        SPOTIFY_CLIENT_ID = System.getenv().get(ENV_SPOTIFY_CLIENT_ID);
        SPOTIFY_CLIENT_SECRET = System.getenv().get(ENV_SPOTIFY_CLIENT_SECRET);
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @ApiMethod(name = "token")
    public SpotifyToken requestToken(@Named("code") String code) throws ForbiddenException {
        try {
            URL url = new URL(URL_SPOTIFY_TOKEN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + BaseEncoding.base64().encode((SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes()));
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(("grant_type=authorization_code&code=" + code + "&redirect_uri=" + SPOTIFY_REDIRECT_URI).getBytes("UTF-8"));
            os.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String returnData = getStringFromInputStream(connection.getInputStream());
                log.info("OK : " + returnData);
                Gson gson = new GsonBuilder().registerTypeAdapter(SpotifyToken.class, new SpotifyTokenConverter()).create();
                SpotifyToken spotifyToken = gson.fromJson(returnData, SpotifyToken.class);
                return spotifyToken;
            } else {
                log.info("Error : " + connection.getErrorStream());
                throw new ForbiddenException(String.valueOf(connection.getResponseCode()));
            }
        } catch (MalformedURLException e) {
            log.info("MalformedURLException : " + e.getMessage());
            throw new ForbiddenException("");
        } catch (IOException e) {
            log.info("IOException : " + e.getMessage());
            throw new ForbiddenException("");
        }
    }

    @ApiMethod(name = "refresh")
    public SpotifyToken requestRefreshToken(@Named("refreshToken") String refreshToken) throws ForbiddenException {
        try {
            URL url = new URL(URL_SPOTIFY_TOKEN);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + BaseEncoding.base64().encode((SPOTIFY_CLIENT_ID + ":" + SPOTIFY_CLIENT_SECRET).getBytes()));
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(("grant_type=refresh_token&refresh_token=" + refreshToken).getBytes("UTF-8"));
            os.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String returnData = getStringFromInputStream(connection.getInputStream());
                log.info("OK : " + returnData);
                Gson gson = new GsonBuilder().registerTypeAdapter(SpotifyToken.class, new SpotifyTokenConverter()).create();
                SpotifyToken spotifyToken = gson.fromJson(returnData, SpotifyToken.class);
                return spotifyToken;
            } else {
                log.info("Error : " + connection.getErrorStream());
                throw new ForbiddenException(String.valueOf(connection.getResponseCode()));
            }
        } catch (MalformedURLException e) {
            log.info("MalformedURLException : " + e.getMessage());
            throw new ForbiddenException("");
        } catch (IOException e) {
            log.info("IOException : " + e.getMessage());
            throw new ForbiddenException("");
        }
    }

}
