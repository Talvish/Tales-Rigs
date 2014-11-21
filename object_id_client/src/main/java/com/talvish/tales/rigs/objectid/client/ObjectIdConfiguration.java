// ***************************************************************************
// *  Copyright 2014 Joseph Molnar
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// ***************************************************************************
package com.talvish.tales.rigs.objectid.client;

import com.google.common.base.Preconditions;

import com.talvish.tales.client.http.ResourceConfiguration;
import com.talvish.tales.system.configuration.ConfigurationManager;

/**
 * The configuration needed to setup the object id manager to retrieving object ids locally.
 * @author jmolnar
 *
 */
public class ObjectIdConfiguration extends ResourceConfiguration {
	private long requestAmount;
	private long thresholdAmount;
	
	/**
	 * Constructor for the needed configuration.
	 * @param theEndpoint the endpoint where the object id service is located
	 * @param theRequestAmount the number of values that will be requested from the service whenever a new set of values are needed
	 * @param theRequestThreshold when there are this number of unused values for a type it will request for more values from the service
	 */
	public ObjectIdConfiguration( String theEndpoint, long theRequestAmount, long theRequestThreshold ) {
		super( theEndpoint, false );
		Preconditions.checkArgument( theRequestAmount > 0, "the request amount has to be greater than zero" );
		Preconditions.checkArgument( theRequestThreshold > 0, "the threshold amount has to be greater than zero" );
		Preconditions.checkArgument( theRequestAmount > theRequestThreshold, "the request amount '%s' has to be greater than the threshold amount '%s'", theRequestAmount, theRequestThreshold );
		
		requestAmount = theRequestAmount;
		thresholdAmount = theRequestThreshold;
	}
	
	/**
	 * The number of values that will be requested when new values are needed.
	 * @return the number of values that will be requested from the service
	 */
	public long getRequestAmount( ) {
		return requestAmount;
	}
	
	/**
	 * This represents the number of values that must be left in the pool to cause a request to get more ids. 
	 * @return the number of values that must be left in the pool to cause a request to get more ids
	 */
	public long getRequestThreshold( ) {
		return thresholdAmount;
	}
	
	

	public static final String SERVICE_NAME					= "object_id_service";
	
	public static final String ENDPOINT_SETTING				= SERVICE_NAME + ".endpoint";
	public static final String REQUEST_AMOUNT_SETTING		= SERVICE_NAME + ".request_amount";
	public static final String REQUEST_THRESHOLD_SETTING	= SERVICE_NAME + ".request_threshold";

	public static final long DEFAULT_REQUEST_AMOUNT			= 100;
	public static final long DEFAULT_THRESHOLD_AMOUNT		= 20;

	/**
	 * Helper method that will look for and load configuration via the configuration manager.
	 * @param theConfigurationManager the configuration manager to use to load the configuration
	 * @return the loaded configuration
	 */
	public static ObjectIdConfiguration loadConfiguration( ConfigurationManager theConfigurationManager ) {
    	String endpoint= theConfigurationManager.getStringValue( ENDPOINT_SETTING );
    	long requestAmount = theConfigurationManager.getLongValue( 
    			REQUEST_AMOUNT_SETTING,
    			DEFAULT_REQUEST_AMOUNT );    	
    	long requestThreshold = theConfigurationManager.getLongValue(
    			REQUEST_THRESHOLD_SETTING,
    			DEFAULT_THRESHOLD_AMOUNT );

    	return new ObjectIdConfiguration( endpoint, requestAmount, requestThreshold );
	}}
