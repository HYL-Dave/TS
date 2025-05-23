package com.Common.Model;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
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

		@Override
		public boolean verify(String s, SSLSession sslSession) {
			return true;
		}
	}

	public String post(String uRL, String data) throws IOException {

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

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();

		HttpPost httppost = new HttpPost(uRL);
		RequestConfig reqConfig = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000)
				.setSocketTimeout(10000).build();
		httppost.setConfig(reqConfig);

		StringEntity postingString = new StringEntity(data);
		String returnCode = null;
		try {
			httppost.setHeader("Content-type", "application/json");
			httppost.setEntity(postingString);

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
				Log4j.log.error("Error occurred: " + e.getClass() + ":" + e.getMessage() + ", Full Stacktrace: "
						+ new Gson().toJson(e.getStackTrace()));
			} finally {
				close(httpclient);
			}
		}
		return returnCode;
	}

	public String postNoSSL(String uRL, String data) throws IOException {
		String returnCode = null;

		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(uRL);
		httpPost.setHeader("Accept-Language", "UTF-8");
		httpPost.setHeader("Content-type", "application/json");

		try {
			StringEntity postingString = new StringEntity(data);
			httpPost.setEntity(postingString);

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				returnCode = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (IOException e) {
			throw e;
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