package com.ibm.watson;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.wink.json4j.JSONObject;

@Path("/createCorpus")
// This file is used for creating new private corpus in Concept Insights
// service
public class CreateCorpus {

	private String apiURL = "/v1/corpus/";
	private String Service_Name = "concept_insights";

	// If running locally complete the variables below with the information in
	// VCAP_SERVICES
	private String serverURL = "<url>";
	private String username = "<username>";
	private String password = "<password>";

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String getInformation(@FormParam("corpusName") String corpusName) {
		HttpURLConnection conn = null;
		HttpURLConnection createCorpus = null;

		try {
			// System.out.println("test data is :" + reqData.toString());

			// Get the service endpoint details

			// 'VCAP_APPLICATION' is in JSON format, it contains useful
			// information
			// about a deployed application
			// 'VCAP_SERVICES' contains all the credentials of services bound to
			// this application.
			// String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

			// Find my service from VCAP_SERVICES in Bluemix

			String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
			JSONObject serviceInfo = new JSONObject(VCAP_SERVICES);

			// Get the Service Credentials for Watson Concept Insights
			JSONObject credentials = serviceInfo.getJSONArray(Service_Name)
					.getJSONObject(0).getJSONObject("credentials");

			serverURL = credentials.getString("url");
			username = (String) credentials.get("username");
			password = (String) credentials.get("password");

			// Prepare the HTTP connection to the service

			String corpusId = URLEncoder.encode(corpusName, "UTF-8");
			conn = (HttpURLConnection) new URL(serverURL + apiURL + username
					+ "/" + corpusId + "?limit=2").openConnection();
			conn.setRequestMethod("GET");
			String auth = username + ":" + password;
			conn.setRequestProperty("Authorization",
					"Basic " + Base64.encodeBase64String(auth.getBytes()));

			// make the connection
			conn.connect();

			// Read the response from the service
			if (conn.getResponseCode() == 200) {
				return "This Corpus is already existing!";
			} else {
				String reqData = "{\"access\":\"private\"}";
				String corpusURL = username + "/" + corpusId;
				createCorpus = (HttpURLConnection) new URL(serverURL + apiURL
						+ corpusURL).openConnection();
				// System.out.println(serverURL + apiURL + corpusURL);
				createCorpus.setRequestMethod("PUT");
				createCorpus.setRequestProperty("Connection", "Keep-Alive");
				createCorpus.setRequestProperty("Content-Type",
						"application/json");
				createCorpus.setRequestProperty("Content-Length",
						String.valueOf(reqData.length()));
				createCorpus.setRequestProperty("Authorization", "Basic "
						+ Base64.encodeBase64String(auth.getBytes()));
				createCorpus.setDoInput(true);
				createCorpus.setDoOutput(true);
				createCorpus.setUseCaches(false);
				// make the connection
				createCorpus.connect();

				DataOutputStream output = null;
				output = new DataOutputStream(createCorpus.getOutputStream());
				output.writeBytes(reqData.toString());

				output.flush();
				output.close();

				// Read the response status from service
				StringBuilder builder = new StringBuilder();
				builder.append(createCorpus.getResponseCode()).append(" ")
						.append(createCorpus.getResponseMessage()).append("\n");
				return builder.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\":\"" + e.getClass().getName() + "\"}";
		} finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
				if (createCorpus != null) {
					createCorpus.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "{\"error\":\"" + e.getClass().getName() + "\"}";
			}
		}

	}
}