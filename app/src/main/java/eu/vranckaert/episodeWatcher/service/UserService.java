package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

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
        HttpClient httpClient = new DefaultHttpClient();
    	boolean status = login(httpClient, user.getUsername(), user.getPassword());
        httpClient.getConnectionManager().shutdown();
        return status;
    }

    public boolean register(User user, String email) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        HttpClient httpClient = new DefaultHttpClient();
    	boolean status = false;
		try {
			status = RegisterUser(httpClient, user.getUsername(), user.getPassword(), email);
		} catch (RegisterFailedException e) {
			e.printStackTrace();
		}
        httpClient.getConnectionManager().shutdown();
        return status;
    }

    public boolean login(HttpClient httpClient, String username, String password) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	HttpPost post = new HttpPost(MyEpisodeConstants.MYEPISODES_LOGIN_PAGE);

    	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_FORM_PARAM_ACTION, MyEpisodeConstants.MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE));

        try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			String message = "Could not start logon because the HTTP post encoding is not supported";
			Log.e(LOG_TAG, message, e);
			throw new UnsupportedHttpPostEncodingException(message, e);
		}

		boolean result = false;
		String responsePage = "";
        HttpResponse response;
        try {
            response = httpClient.execute(post);
            responsePage = EntityUtils.toString(response.getEntity());
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

        if (responsePage.contains("Wrong username/password") || responsePage.contains("Username not found") || responsePage.contains("ERR_INVALID_REQ")) { // || !responsePage.contains(username)) {
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

    private boolean RegisterUser(HttpClient httpClient, String username, String password, String email) throws RegisterFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	HttpPost post = new HttpPost(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE);

    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_EMAIL, email));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_FORM_PARAM_ACTION, MyEpisodeConstants.MYEPISODES_REGISTER_PAGE_PARAM_ACTION_VALUE));

        try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			String message = "Could not start logon because the HTTP post encoding is not supported";
			Log.e(LOG_TAG, message, e);
			throw new UnsupportedHttpPostEncodingException(message, e);
		}

		boolean result = false;
		String responsePage = "";
        HttpResponse response;
        try {
            response = httpClient.execute(post);
            responsePage = EntityUtils.toString(response.getEntity());
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
