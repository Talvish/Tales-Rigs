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
package com.talvish.tales.rigs.configuration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.talvish.tales.services.Service;
import com.talvish.tales.services.http.HttpInterface;
import com.talvish.tales.services.http.ServiceConstants;

/**
* The main class for the service, which sets up the engine
* and binds the engine to the resource and the resource to
* a particular path.
* @author Joseph Molnar
*
*/
public class ConfigurationService extends Service {
	private static final Logger logger = LoggerFactory.getLogger( ConfigurationEngine.class );

	private ConfigurationEngine engine;

	public ConfigurationService( ) {
		super( "configuration_service", "Configuration Service", "A service that provides config for other services." );
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		engine = new ConfigurationEngine( this.getConfigurationManager().getValues( ConfigurationEngineConfiguration.class ) );
		this.interfaceManager.getInterface( ServiceConstants.INTERNAL_INTERFACE_NAME, HttpInterface.class ).bind( new ConfigurationResource( engine ), "/configuration" );
	}
}

