package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.PasswordEnctyptionFailedException;
import eu.vranckaert.episodeWatcher.exception.RegisterFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String LOG_TAG = UserService.class.getSimpleName();

    public boolean login(User user) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	boolean status = login(EpisodesService.getOkHttpClient(), user.getUsername(), user.getPassword());
        return status;
    }

    public boolean register(User user, String email) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        OkHttpClient httpClient = EpisodesService.getOkHttpClient();
    	boolean status = false;
		try {
			status = RegisterUser(httpClient, user.getUsername(), user.getPassword(), email);
		} catch (RegisterFailedException e) {
			e.printStackTrace();
		}
        return status;
    }

    public boolean login(OkHttpClient httpClient, String username, String password) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_FORM_PARAM_ACTION, MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE));
        Request request = EpisodesService.buildPostRequest(httpClient, MyEpisodeConstants.MYEPISODES_LOGIN_PAGE, nvps);

		boolean result = false;
		String responsePage = "";
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            responsePage = response.body().string();
        } catch (ClientProtocolException e) {
            String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Login to MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new LoginFailedException(message, e);
        }

        if (responsePage.contains("Wrong username/password") || responsePage.contains("ERR_INVALID_REQ")) {
            String message = "Login to MyEpisodes failed. Login page: " + MyEpisodeConstants.MYEPISODES_LOGIN_PAGE + " Username: " + username +
            					" Password: ***** Leaving with status code " + result;
            Log.w(LOG_TAG, message);
            throw new LoginFailedException(message);
        } else {
            Log.d(LOG_TAG, "Successful login to " + MyEpisodeConstants.MYEPISODES_LOGIN_PAGE);
            result = true;
        }
        return result;
    }

    private boolean RegisterUser(OkHttpClient httpClient, String username, String password, String email) throws RegisterFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_EMAIL, email));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_FORM_PARAM_ACTION, MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_ACTION_VALUE));

        Request request = EpisodesService.buildPostRequest(httpClient, MyEpisodeConstants.MYEPISODES_REGISTER_PAGE, nvps);

		boolean result = false;
		String responsePage = "";
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            responsePage = response.body().toString();
        } catch (ClientProtocolException e) {
            String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Login to MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new RegisterFailedException(message, e);
        }

        if (responsePage.contains("Username already exists, please choose another username.")) {
            String message = "Username already exists!";
            Log.w(LOG_TAG, message);
            throw new RegisterFailedException(message);
        } else if (responsePage.contains("Please fill in all fields.")) {
            String message = "Not all fields!";
            Log.w(LOG_TAG, message);
            throw new RegisterFailedException(message);
        } else if (responsePage.contains("Email already exists, please choose another email.")) {
            String message = "Email already excists";
            Log.w(LOG_TAG, message);
            throw new RegisterFailedException(message);
        }
        else {
            Log.i(LOG_TAG, "User succesfully created in. " + MyEpisodeConstants.MYEPISODES_REGISTER_PAGE);
            result = true;
        }
        return result;
    }

    public String encryptPassword(final String password) throws PasswordEnctyptionFailedException {
        String encryptedPwd = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(MyEpisodeConstants.PASSWORD_ENCRYPTION_TYPE);
            digest.reset();
            digest.update(password.getBytes());

            byte[] messageDigest = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++) {
                hexString.append(String.format("%02x", messageDigest[i]));
            }
            encryptedPwd = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            String message = "The password could not be encrypted because there is no such algorithm (" + MyEpisodeConstants.PASSWORD_ENCRYPTION_TYPE + ")";
            Log.e(LOG_TAG, message, e);
            throw new PasswordEnctyptionFailedException(message, e);
        }
        //Log.d(LOG_TAG, "The encrypted password is " + encryptedPwd); //don't print to the logs unless needed
        return encryptedPwd;
    }
}
