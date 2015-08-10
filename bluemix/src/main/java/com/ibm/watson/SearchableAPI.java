package com.ibm.watson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

@Path("/search")
// This file is used for querying a specific corpus
public class SearchableAPI {

	private String apiURL = "/v1/searchable/";
	private String Service_Name = "concept_insights";
	
	// If running locally complete the variables below with the information in VCAP_SERVICES
	private String serverURL = "<url>";
	private String username = "<username>";
	private String password = "<password>";
	
	String func = "semanticSearch";


	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String getInformation(@FormParam("ids") String ids,
			@FormParam("corpusName") String corpusName) {

		HttpURLConnection conn = null;
		try {
			// Get the service endpoint details

			// 'VCAP_APPLICATION' is in JSON format, it contains useful
			// information
			// about a deployed application
			// 'VCAP_SERVICES' contains all the credentials of services bound to
			// this application.
			// String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

			// Find my service from VCAP_SERVICES in Bluemix

			
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

			String nameEn = URLEncoder.encode(ids, "UTF-8");
			String query = "func=" + func + "&ids=[\"/corpus/" + username + "/test/" + nameEn + "\"]";
			corpusName = URLEncoder.encode(corpusName, "UTF-8");
			String corpusURL = username + "/" + corpusName + "?";
			conn = (HttpURLConnection) new URL(serverURL + apiURL + corpusURL + query)
					.openConnection();
			conn.setRequestMethod("GET");
			String auth = username + ":" + password;
			conn.setRequestProperty("Authorization",
					"Basic " + Base64.encodeBase64String(auth.getBytes()));
			// make the connection
			conn.connect();

			// Read the response from the service
            BufferedReader rdr = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
            
			String line = "";
			StringBuffer buf = new StringBuffer();
			while ((line = rdr.readLine()) != null) {
				buf.append(line);
				buf.append("\n");
			}
			rdr.close();
			
			// Return the response from the service
			//System.out.println("the result is: " + buf.toString());
			return buf.toString();
			
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