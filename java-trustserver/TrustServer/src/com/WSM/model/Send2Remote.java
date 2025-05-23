package com.WSM.model;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.toppanidgate.fidouaf.common.model.Log4j;

@SuppressWarnings("deprecation")
public class Send2Remote {
	private static int Timeout = 15000;
	private static final int TotalConn = 100;

	private static PoolingHttpClientConnectionManager cm = null;
	private static RequestConfig reqConfig = null;

	// default timeout
	public Send2Remote() {
		reqConfig = RequestConfig.custom().setConnectTimeout(Timeout).setConnectionRequestTimeout(Timeout)
				.setSocketTimeout(Timeout).build();
	}

	// change timeout to custom second
	public Send2Remote(final int connection_Timeout_sec) {
		if (connection_Timeout_sec > 1 && connection_Timeout_sec < Integer.MAX_VALUE / 10000) {
			Timeout = connection_Timeout_sec * 1000;
		}

		reqConfig = RequestConfig.custom().setConnectTimeout(Timeout).setConnectionRequestTimeout(Timeout)
				.setSocketTimeout(Timeout).build();
	}

	private static final class TrustStg implements TrustStrategy {
		@Override
		public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			return true;
		}
	}

	private static final class X509HostnameVerifierImplementation implements X509HostnameVerifier {
		@Override
		public void verify(String host, SSLSocket ssl) throws IOException {
		}

		@Override
		public void verify(String host, X509Certificate cert) throws SSLException {
		}

		@Override
		public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
		}

//		@Override
//		public boolean verify(String s, SSLSession sslSession) {
//			return true;
//		}
		
		@Override
		public boolean verify(String hostname, SSLSession sslSession) {
//			Log4j.log.debug("### hostname: " + hostname + "/n ### sslSession:" + sslSession + "/n ### equals: "
//					+ hostname.equals(sslSession.getPeerHost()));
			return hostname.equals(sslSession.getPeerHost());
		}
	}

	// general function in case that you don't want to write connection way check
	public String sendPost(String uRL, String apiKey, String data) throws IOException {
		String returnMsg;

		if (uRL.indexOf("https:") > -1)
			returnMsg = post(uRL, apiKey, data);
		else
			returnMsg = postNoSSL(uRL, apiKey, data);

		return returnMsg;
	}

	// general function in case that you don't want to write connection way check
	public String sendPostWithForm(String uRL, List<NameValuePair> formparams) throws IOException {
		String returnMsg;

		if (uRL.indexOf("https:") > -1)
			returnMsg = postWithForm(uRL, formparams);
		else
			returnMsg = postNoSslWithForm(uRL, formparams);

		return returnMsg;
	}

	public String post(String uRL, String apiKey, String data) throws IOException {
		if (cm == null) {
			SSLContextBuilder builder = SSLContexts.custom();
			try {
				builder.loadTrustMaterial(null, new TrustStg());
			} catch (NoSuchAlgorithmException | KeyStoreException e1) {
				Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
						+ new Gson().toJson(e1.getStackTrace()));
				return null;
			}

			SSLContext sslContext = null;
			try {
				sslContext = builder.build();
			} catch (KeyManagementException | NoSuchAlgorithmException e1) {
				Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
						+ new Gson().toJson(e1.getStackTrace()));
				return null;
			}

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
					new String[] { "TLSv1.1", "TLSv1.2" }, null, new X509HostnameVerifierImplementation());

			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("https", sslsf).build();

			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			cm.setMaxTotal(TotalConn);
			cm.setDefaultMaxPerRoute(25);
		}

		String returnCode = null;

		HttpClient httpclient = HttpClients.custom().setConnectionManagerShared(true).setConnectionManager(cm).build();

		HttpPost httppost = new HttpPost(uRL);
		httppost.setConfig(reqConfig);
		httppost.setHeader("Accept-Language", "UTF-8");
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader("apiKey", apiKey);
		httppost.setEntity(new StringEntity(data, "UTF-8"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			returnCode = EntityUtils.toString(entity, "UTF-8");
		}

		EntityUtils.consumeQuietly(response.getEntity());

		return returnCode;
	}

	public String postNoSSL(String uRL, String apiKey, String data) throws IOException {
		String returnCode = null;

		if (cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(TotalConn);
			cm.setDefaultMaxPerRoute(25);
		}

		HttpClient httpclient = HttpClients.custom().setConnectionManagerShared(true).setConnectionManager(cm).build();
		HttpPost httpPost = new HttpPost(uRL);
		httpPost.setConfig(reqConfig);
		httpPost.setHeader("Accept-Language", "UTF-8");
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setHeader("apiKey", apiKey);
		httpPost.setEntity(new StringEntity(data, "UTF-8"));

		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			returnCode = EntityUtils.toString(entity, "UTF-8");
		}

		EntityUtils.consumeQuietly(response.getEntity());

		return returnCode;
	}

	public String postWithForm(String uRL, List<NameValuePair> formparams) throws IOException {
		if (cm == null) {
			SSLContextBuilder builder = SSLContexts.custom();
			try {
				builder.loadTrustMaterial(null, new TrustStg());
			} catch (NoSuchAlgorithmException | KeyStoreException e1) {
				Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
						+ new Gson().toJson(e1.getStackTrace()));
				return null;
			}

			SSLContext sslContext = null;
			try {
				sslContext = builder.build();
			} catch (KeyManagementException | NoSuchAlgorithmException e1) {
				Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
						+ new Gson().toJson(e1.getStackTrace()));
				return null;
			}

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
					new String[] { "TLSv1.1", "TLSv1.2" }, null, new X509HostnameVerifierImplementation());

			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("https", sslsf).build();

			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			cm.setMaxTotal(TotalConn);
			cm.setDefaultMaxPerRoute(25);
		}

		String returnCode = null;

		HttpClient httpclient = HttpClients.custom().setConnectionManagerShared(true).setConnectionManager(cm).build();

		HttpPost httppost = new HttpPost(uRL);
		httppost.setConfig(reqConfig);
		httppost.setHeader("Accept-Language", "UTF-8");
		httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			returnCode = EntityUtils.toString(entity, "UTF-8");
		}

		EntityUtils.consumeQuietly(response.getEntity());

		return returnCode;
	}

	public String postNoSslWithForm(String uRL, List<NameValuePair> formparams) throws IOException {
		String returnCode = null;

		if (cm == null) {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(TotalConn);
			cm.setDefaultMaxPerRoute(25);
		}

		HttpClient httpclient = HttpClients.custom().setConnectionManagerShared(true).setConnectionManager(cm).build();
		HttpPost httpPost = new HttpPost(uRL);
		httpPost.setConfig(reqConfig);
		httpPost.setHeader("Accept-Language", "UTF-8");
		httpPost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));

		HttpResponse response = httpclient.execute(httpPost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			returnCode = EntityUtils.toString(entity, "UTF-8");
		}

		EntityUtils.consumeQuietly(response.getEntity());

		return returnCode;
	}
}