package com.toppanidgate.WSM.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
//	private static final Logger logger = LogManager.getLogger(Send2Remote.class);


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

		/**
		 * ESUN 用到，CHB 沒用到
		 */
		@Override
		public boolean verify(String hostname, SSLSession sslSession) {
			return hostname.equals(sslSession.getPeerHost());
		}
	}

	// post form param with ssl
	public String post(String uRL, List<NameValuePair> formparams) throws IOException {

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
				new String[] { "TLSv1.2", "TLSv1.3" }, null, new X509HostnameVerifierImplementation());

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslsf).build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();

		HttpPost httppost = new HttpPost(uRL);
		RequestConfig reqConfig = RequestConfig.custom()
				.setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000)
				.setSocketTimeout(10000)
				.build();
		httppost.setConfig(reqConfig);

		UrlEncodedFormEntity uefEntity;
		String returnCode = null;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);

			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					returnCode = EntityUtils.toString(entity, "UTF-8");
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (UnsupportedEncodingException e1) {
			throw e1;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				Log4j.log.error(e.getMessage());
			} finally {
				close(httpclient);
			}
		}
		return returnCode;
	}

	//* post json string with ssl
	public String post_JSON(String uRL, String json) throws IOException {

		SSLContextBuilder builder = SSLContexts.custom();
		try {
			builder.loadTrustMaterial(null, new TrustStg());
		} catch (NoSuchAlgorithmException | KeyStoreException e1) {
			Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
					+ new Gson().toJson(e1.getStackTrace()));
//			Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
//					+ new Gson().toJson(e1.getStackTrace()));
			return null;
		}

		SSLContext sslContext = null;
		try {
			sslContext = builder.build();
		} catch (KeyManagementException | NoSuchAlgorithmException e1) {
//			Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
//					+ new Gson().toJson(e1.getStackTrace()));
			Log4j.log.error("Error occurred: " + e1.getClass() + ":" + e1.getMessage() + ", Full Stacktrace: "
					+ new Gson().toJson(e1.getStackTrace()));
			return null;
		}

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
				new String[] { "TLSv1.2", "TLSv1.3" }, null, new X509HostnameVerifierImplementation());

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslsf).build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();

		HttpPost httppost = new HttpPost(uRL);
		RequestConfig reqConfig = RequestConfig.custom()
				.setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000)
				.setSocketTimeout(10000)
				.build();
		httppost.setConfig(reqConfig);

		String returnCode = null;
		try {
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httppost.setEntity(requestEntity);

			CloseableHttpResponse response = httpclient.execute(httppost);
//			 Log4j.log.info("*** response:" + response);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					returnCode = EntityUtils.toString(entity, "UTF-8");
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (UnsupportedEncodingException e1) {
			throw e1;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				Log4j.log.error(e.getMessage());
			} finally {
				close(httpclient);
			}
		}
		return returnCode;
	}

	// post form param without ssl
	public String postNoSSL(String uRL, List<NameValuePair> formparams) throws IOException {
		String returnCode = null;

		HttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(uRL);
			httpPost.setHeader("Accept-Language", "UTF-8");

			UrlEncodedFormEntity uefEntity = null;
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httpPost.setEntity(uefEntity);

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				returnCode = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (IOException e) {
			Log4j.log.error("*** IOException@postNoSSL:{}", e.getMessage());
			throw e;
		} catch (Exception e) {
			Log4j.log.error("*** Exception@postNoSSL:{}", e.getMessage());
			throw e;
		} finally {
			if (httpClient != null)
				((Closeable) httpClient).close();
		}

		return returnCode;
	}
	
	// post json string without ssl
	public String postNoSSL_JSON(String uRL, String json) throws IOException {
		String returnCode = null;

		HttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(uRL);
			httpPost.setHeader("Accept-Language", "UTF-8");

			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				returnCode = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (IOException e) {
			Log4j.log.error("*** IOException@postNoSSL:{}", e.getMessage());
			throw e;
		} finally {
			if (httpClient != null)
				((Closeable) httpClient).close();
		}

		return returnCode;
	}

	public void close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException ignore) {
			}
		}
	}

}