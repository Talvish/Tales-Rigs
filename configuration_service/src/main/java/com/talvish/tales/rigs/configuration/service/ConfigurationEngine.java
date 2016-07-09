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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import com.talvish.tales.contracts.data.DataContractTypeSource;
import com.talvish.tales.serialization.json.JsonTranslationFacility;
import com.talvish.tales.system.configuration.hierarchical.SourceManager;

// TODO: consider an expiration in the LoadedSettings in the config settings which will cause a removal of
//       a setting so it can be refreshed (though it may require a larger set to rest)


public class ConfigurationEngine {
	private static final Logger logger = LoggerFactory.getLogger( ConfigurationEngine.class );

	private final SourceManager settingsSource;
	
	public ConfigurationEngine( ConfigurationEngineConfiguration theConfig ) {
		Preconditions.checkNotNull( theConfig, "need configuration" );
		
		// so we need a json translation facility
		// and we need a source manager
		logger.info( "Serving configuration from file '{}'", theConfig.getSettingsFilename( ) );
		settingsSource = new SourceManager( theConfig.getSettingsFilename(), new JsonTranslationFacility( new DataContractTypeSource( ) ) );
	}
	
	public void getSetting( String theProfile, String theBlock, String theSetting ) {
		
	}
	
	public List<Setting> getSettings( String theProfile, String theBlock ) {
		Map<String,com.talvish.tales.system.configuration.hierarchical.Setting> extractedSettings = settingsSource.extractSettings(theProfile, theBlock);
		ArrayList<Setting> returnSettings = new ArrayList<>( extractedSettings.size( ) );
		
		// TODO: there are plenty of failures here that we need to deal with, like the existence of the profile and the block,
		//       we made the change to check/get BUT the descriptors aren't public, need to decide if I really want that
		
		for( com.talvish.tales.system.configuration.hierarchical.Setting setting : extractedSettings.values() ) {
			returnSettings.add( new Setting( setting ) );
		}
		return returnSettings;
	}
}
