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
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;

@Path("/createDoc")
// This file is used for sending documents to Concept Insights service and saving all documents in a specific corpus.
public class CIcorpus {

	private String apiURL = "/v1/corpus/";
	private String Service_Name = "concept_insights";
	
	// If running locally complete the variables below with the information in VCAP_SERVICES
	private String serverURL = "<url>";
	private String username = "<username>";
	private String password = "<password>";


	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String getCollections(@FormParam("name") String name,
			@FormParam("data") String data,
			@FormParam("corpusName") String corpusName) {

		HttpURLConnection conn = null;
		
		try {
			JSONObject pAO = new JSONObject();
			pAO.put("name", name);
			pAO.put("data", data);
			JSONArray partArr = new JSONArray();
			partArr.put(pAO);
			JSONObject reqData = new JSONObject();
			reqData.put("label", name);
			reqData.put("parts", partArr);
			//System.out.println("test data is :" + reqData.toString());
			
			// Get the service endpoint details

			// 'VCAP_APPLICATION' is in JSON format, it contains useful
			// information
			// about a deployed application
			// 'VCAP_SERVICES' contains all the credentials of services bound to
			// this application.

			// Find my service from VCAP_SERVICES in BlueMix
			
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES"); 
			//System.out.println("vcapservice is : " + VCAP_SERVICES);
			JSONObject serviceInfo = new JSONObject(VCAP_SERVICES);
			//System.out.println("Service Info is: " + serviceInfo);
			
			// Get the Service Credentials for Watson AlchemyAPI
			JSONObject credentials = serviceInfo.getJSONArray(Service_Name)
					.getJSONObject(0).getJSONObject("credentials");


			serverURL = credentials.getString("url");
			username = (String)credentials.get("username");
			password = (String)credentials.get("password");
			
			// Prepare the HTTP connection to the service
			String nameEn = URLEncoder.encode(name, "UTF-8");
			corpusName = URLEncoder.encode(corpusName, "UTF-8");
			String corpusURL = username + "/" + corpusName + "/" + nameEn;
			conn = (HttpURLConnection) new URL(serverURL + apiURL + corpusURL)
					.openConnection();
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length",
					String.valueOf(reqData.length()));
			String auth = username + ":" + password;
			conn.setRequestProperty("Authorization",
					"Basic " + Base64.encodeBase64String(auth.getBytes()));
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			// make the connection
			conn.connect();
			DataOutputStream output = null;
			output = new DataOutputStream(conn.getOutputStream());
			output.writeBytes(reqData.toString());
			
			output.flush();
            output.close();
			
            // Read the response status from service
            StringBuilder builder = new StringBuilder();
			builder.append(conn.getResponseCode()).append(" ")
					.append(conn.getResponseMessage()).append("\n");
			//System.out.println("The response status is : " + builder.toString());
			return builder.toString();
		} catch(Exception e){
			e.printStackTrace();
			return "{\"error\":\"" + e.getClass().getName() + "\"}";
		}finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "{\"error\":\"" + e.getClass().getName() + "\"}";
			}
		}
		

	}


}