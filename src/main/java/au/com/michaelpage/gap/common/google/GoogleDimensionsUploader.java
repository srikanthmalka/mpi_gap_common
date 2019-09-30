package au.com.michaelpage.gap.common.google;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.michaelpage.gap.common.Settings;
import au.com.michaelpage.gap.common.util.Util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.CustomDataSource;
import com.google.api.services.analytics.model.CustomDataSources;
import com.google.api.services.analytics.model.CustomDimension;
import com.google.api.services.analytics.model.CustomDimensions;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;

public class GoogleDimensionsUploader {
	
	private static final Logger logger = LoggerFactory.getLogger(GoogleDimensionsUploader.class);
			
	private static HttpTransport HTTP_TRANSPORT;
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private static final String SERVICE_ACCOUNT_EMAIL = "986550503095-pajp4c6ogkrk9uuricge64t3l7i9mpuv@developer.gserviceaccount.com";
	private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "c:\\GTS\\Settings\\Google-Analytics-ba52d495af23.p12";
	
	private static final String CLOUD_STORAGE_SERVICE_ACCOUNT_EMAIL = "cloudstorageaccount@mp-uid-all-touchpoints.iam.gserviceaccount.com";
	private static final String CLOUD_STORAGE_SERVICE_ACCOUNT_PKCS12_FILE_PATH = "c:\\GTS\\Settings\\Google-Cloud-Storage-0b65540e4f30.p12";
	private static final String CLOUD_STORAGE_BACKET_NAME = "pagegroup-uploads";
	
	private static final String ACCOUNT_ID = "7535549";
	
	
	public void upload(String fileName) {
		
		// File names contain all necessary information
		// Name of the data set, profileId and custom data source id 
		// Example: Consultant---UA-7535549-12---0CcQ0IqgR86fPhMiunAmhQ.csv
		String[] fileNameSplit = fileName.split("\\\\");
		
		String nameWithoutExt = fileNameSplit[fileNameSplit.length - 1].substring(0, fileNameSplit[fileNameSplit.length - 1].lastIndexOf(".csv"));
		String[] customDataSourceInfo = nameWithoutExt.split("---");
		String customDataSourceName = customDataSourceInfo[0];
		String profileId = customDataSourceInfo[1];
		String customDataSourceId = customDataSourceInfo[2];
		
		String cloudFileName = Settings.INSTANCE.getHostName() + "/" + customDataSourceName + "-" + fileNameSplit[fileNameSplit.length - 3] + ".csv";
		
		GoogleUploaderDao googleUploaderDao = new GoogleUploaderDao();
		
		logger.info("Started uploading {} dimensions file {} [accountId: {}, profileId: {}, customDataSourceId: {}]", 
				customDataSourceName, fileName, ACCOUNT_ID, profileId, customDataSourceId);

		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			
			Analytics analytics = getAnalyticsService();
			
//			CustomDataSources sources = null;
//			try {
//			  sources = analytics.management().
//			      customDataSources().list(accountId, webPropertyId).execute();
//			  
//				for (CustomDataSource source : sources.getItems()) {
//
//					  System.out.println("Account Id              = " + source.getAccountId());
//					  System.out.println("Property Id             = " + source.getWebPropertyId());
//					  System.out.println("Custom Data Source Id   = " + source.getId());
//					  System.out.println("Custom Data Source Kind = " + source.getKind());
//					  System.out.println("Custom Data Source Type = " + source.getType());
//					  System.out.println("Custom Data Source Name = " + source.getName());
//					  System.out.println("Custom Data Source Description = "
//					      + source.getDescription());
//					  System.out.println("Custom Data Source Upload Type = "
//					      + source.getUploadType());
//					  System.out.println("\n");
//					  
//					  try {
//						  CustomDimensions dimensions = analytics.management().customDimensions()
//						      .list(accountId, webPropertyId).execute();
//						  
//						  for (CustomDimension dimension : dimensions.getItems()) {
//							  System.out.println("Dimension Kind: " + dimension.getKind());
//							  System.out.println("Dimension Id: " + dimension.getId());
//							  System.out.println("Account ID: " + dimension.getAccountId());
//							  System.out.println("Property ID: " + dimension.getWebPropertyId());
//							  System.out.println("Dimension Name: " + dimension.getName());
//							  System.out.println("Dimension Index: " + dimension.getIndex());
//							  System.out.println("Dimension Scope: " + dimension.getScope());
//							  System.out.println("Dimension Active: " + dimension.getActive());
//							  System.out.println("Dimension Created: " + dimension.getCreated());
//							  System.out.println("Dimension Updated: " + dimension.getUpdated());
//							}						  
//
//						} catch (GoogleJsonResponseException e) {
//						  System.err.println("There was a service error: "
//						      + e.getDetails().getCode() + " : "
//						      + e.getDetails().getMessage());
//						}
//					  
//					  
//					  
//					}			
//			  
//			} catch (GoogleJsonResponseException e) {
//			  System.err.println("There was a service error: "
//			      + e.getDetails().getCode() + " : "
//			      + e.getDetails().getMessage());
//			}

			File file = new File(fileName);
			InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", new FileInputStream(file));
			mediaContent.setLength(file.length());
			analytics.management().uploads().uploadData(ACCOUNT_ID, profileId, customDataSourceId, mediaContent).execute();
			
			if ("Consultant".equalsIgnoreCase(customDataSourceName) && (Boolean.parseBoolean(Settings.INSTANCE.getOptionValue("broadbean:uploadToGoogleCloud"))
					|| Boolean.parseBoolean(Settings.INSTANCE.getOptionValue("rpm:uploadToGoogleCloud")))) {
				uploadIntoCloud(fileName, cloudFileName);
			}
			
			googleUploaderDao.updateFileUploadStatus(fileName, true);
			
			logger.info("Finished uploading dimensions file {} [accountId: {}, webPropertyId: {}]", fileName, ACCOUNT_ID, profileId);
			
		} catch (Throwable t) {
			googleUploaderDao.updateFileUploadStatus(fileName, false);
			logger.info("An error occured during uploading of dimensions file {}, please check log files for details]", fileName);
			logger.debug("ERROR: " + t.getMessage(), t);
		}
		
	} 
	
	private Analytics getAnalyticsService() {

		Collection<String> ANALYTICS_SCOPE_EDIT = new ArrayList<String>();
		ANALYTICS_SCOPE_EDIT.add(AnalyticsScopes.ANALYTICS_EDIT);
		ANALYTICS_SCOPE_EDIT.add(AnalyticsScopes.ANALYTICS_READONLY);
		
		   GoogleCredential credential = null;
		   try {
		        credential = new GoogleCredential.Builder()
		        	.setTransport(HTTP_TRANSPORT)
		              .setJsonFactory(JSON_FACTORY)
		              .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
		              .setServiceAccountScopes(ANALYTICS_SCOPE_EDIT)
		              .setServiceAccountPrivateKeyFromP12File(new File(SERVICE_ACCOUNT_PKCS12_FILE_PATH)) 
		              .build();
		        
			     // Set up and return Google Analytics API client.
			     Analytics analytics = new Analytics.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
			          "GTS").build();
			     
			     return analytics;
		        
		     } catch (Throwable t) {
		    	 logger.debug("ERROR: Unable to get Analytics service. " + t.getMessage(), t);
		    	 throw new RuntimeException(t);
		     }
	}

	private Storage getStorageService() {
		Collection<String> STORAGE_SCOPE_EDIT = new ArrayList<String>();
		STORAGE_SCOPE_EDIT.add(StorageScopes.DEVSTORAGE_READ_WRITE);
		

		GoogleCredential credential = null;
		try {
			credential = new GoogleCredential.Builder()
		        	.setTransport(HTTP_TRANSPORT)
		              .setJsonFactory(JSON_FACTORY)
		              .setServiceAccountId(CLOUD_STORAGE_SERVICE_ACCOUNT_EMAIL)
		              .setServiceAccountScopes(STORAGE_SCOPE_EDIT)
		              .setServiceAccountPrivateKeyFromP12File(new File(CLOUD_STORAGE_SERVICE_ACCOUNT_PKCS12_FILE_PATH))
		              .build();
		        
			     // Set up and return Google Storage API client.
			Storage storage = new Storage.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
		          "GTS").build();
		     
			return storage;
		    
		} catch (Throwable t) {
			 logger.debug("ERROR: Unable to get Storage service. " + t.getMessage(), t);
			 throw new RuntimeException(t);
		}
	}
	
	private void uploadIntoCloud(String fileName, String cloudFileName) {
		Storage storage = getStorageService();
		
		try {
			InputStreamContent contentStream = new InputStreamContent("text/csv", new FileInputStream(fileName));
			
			StorageObject objectMetadata = new StorageObject()
			      // Set the destination object name
			      .setName(cloudFileName);
			      // Set the access control list to publicly read-only
//			      .setAcl(Arrays.asList(
//			          new ObjectAccessControl().setEntity("allUsers").setRole("READER")));


			// Do the insert
			  Storage.Objects.Insert insertRequest = storage.objects().insert(
					  CLOUD_STORAGE_BACKET_NAME, objectMetadata, contentStream);

			  insertRequest.execute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
