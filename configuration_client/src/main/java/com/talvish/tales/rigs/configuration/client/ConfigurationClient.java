//***************************************************************************
//*  Copyright 2016 Joseph Molnar
//*
//*  Licensed under the Apache License, Version 2.0 (the "License");
//*  you may not use this file except in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*      http://www.apache.org/licenses/LICENSE-2.0
//*
//*  Unless required by applicable law or agreed to in writing, software
//*  distributed under the License is distributed on an "AS IS" BASIS,
//*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//*  See the License for the specific language governing permissions and
//*  limitations under the License.
//***************************************************************************
package com.talvish.tales.rigs.configuration.client;

import java.util.List;

import com.google.common.base.Strings;

import com.talvish.tales.client.http.ResourceClient;
import com.talvish.tales.client.http.ResourceMethod;
import com.talvish.tales.client.http.ResourceResult;
import com.talvish.tales.communication.HttpVerb;
import com.talvish.tales.parts.reflection.TypeUtility;
import com.talvish.tales.system.Conditions;

/**
* The client for talking to the ConfigurationService. 
* @author jmolnar
*
*/
public class ConfigurationClient extends ResourceClient {
    @SuppressWarnings("unused")
	private static List<Setting> settingTypes; // this is for reflection to pick up to get all type information
 
 
    /**
     * The constructor used to create the client.
     * @param theConfiguration the configuration to use to talk to the service
     * @param theUserAgent the user agent to use while talking to the service
     */
	public ConfigurationClient( ConfigurationConfiguration theConfiguration, String theUserAgent ) {
		super( theConfiguration, "/configuration", "20160701", theUserAgent ); 
 
		// we now define the methods that we are going to expose for calling
		this.methods = new ResourceMethod[ 1 ];
		
		this.methods[ 0 ] = this.defineMethod( "get_settings", TypeUtility.extractFieldType( this.getClass( ), "settingTypes" ), HttpVerb.GET, "settings" )
				.defineQueryParameter( "profile", String.class )
				.defineQueryParameter( "block", String.class );

	}
	
	/**
	 * Returns the settings for a particular profile/block combination.
	 * @return the list of available settings
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<List<Setting>> getSettings( String theProfile, String theBlock ) throws InterruptedException {
		Conditions.checkParameter( !Strings.isNullOrEmpty( theProfile ), "a profile name must be given" );
		Conditions.checkParameter( !Strings.isNullOrEmpty( theBlock ), "a block name must be given" );
		
		return this.createRequest( this.methods[ 0 ] )
				.setQueryParameter( "profile", theProfile )
				.setQueryParameter( "block", theBlock )
				.call();
	}
	

}