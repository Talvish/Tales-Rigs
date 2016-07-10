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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.talvish.tales.client.http.ResourceResult;
import com.talvish.tales.system.configuration.ConfigurationSource;
import com.talvish.tales.system.configuration.LoadedSetting;
import com.talvish.tales.system.configuration.SettingValueHelper;
import com.talvish.tales.system.configuration.SourceConfiguration;

/**
 * This class represents a config source where the data comes
 * from the configuration service.
 * @author jmolnar
 *
 */
@SourceConfiguration( settingsClass = ConfigurationConfiguration.class )
public class ConfigurationServiceSource implements ConfigurationSource {
	private final ConfigurationClient client;
	private final String sourceName;
	private final String profile;
	private final String block;
	
	private Map<String, Setting> settings = new HashMap<>( 0 ); 


	public ConfigurationServiceSource( ConfigurationConfiguration theConfiguration ) {
		Preconditions.checkNotNull( theConfiguration, "need the configuration for the client" );
		
		profile = theConfiguration.getProfile( );
		block = theConfiguration.getBlock( );
		sourceName = "[" + theConfiguration.getEndpoint() + "]";
		client = null;
	}

	public ConfigurationServiceSource( String theProfile, String theBlock ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theProfile ), "need a profile" );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theBlock ), "need a block" );
		
		profile = theProfile;
		block = theBlock;
		sourceName = "a"; // TODO: fix this
		client = null;
	}
	
	
	protected void fetchSettings( ) {
		try {
			ResourceResult<List<Setting>> settingsResult = client.getSettings( profile, block );
			
			// TODO: check the result
			
			List<Setting> settingsList = settingsResult.getResult( );
			Map<String, Setting> settingsMap = new HashMap<>( settingsList.size( ) );
			
			for( Setting setting : settingsList ) {
				settingsMap.put( setting.getName(), setting );
			}
			
			settings = settingsMap;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Called to find out if the source contains the requested value.
	 * @param theName the name of the value to retrieve
	 * @return the value retrieved
	 */
	public boolean contains( String theName ) {
		return settings.containsKey( theName );
	}
	
	/**
	 * Gets the configuration value for name given. If the value
	 * doesn't exist a null is returned.
	 * @param theName the name of the value to retrieve
	 * @param theType the type of the value to retrieve
	 * @return a configuration setting generated for value, if found, null otherwise
	 */
	public LoadedSetting getValue( String theName, Class<?> theType ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theName ), "Name value is null or empty.");
		Preconditions.checkNotNull( theType, "Need a type to be able to translate." );

		LoadedSetting setting = null;
		Setting networkSetting = settings.get( theName );
		if( networkSetting != null ) {
			setting = SettingValueHelper.generateValue( theName, networkSetting.getValue(), networkSetting.getDescription(), networkSetting.isSensitive(), generateSourceName( networkSetting ), theType );
		}
		return setting;
	}
	
	/**
	 * Gets the configuration value, as a list, for the name given. If the value
	 * doesn't exist a null is returned.
	 * @param theName the name of the value to retrieve
	 * @param theElementType the type of the element of the list retrieve
	 * @return a configuration setting generated for value, if found, null otherwise
	 */
	public LoadedSetting getList( String theName, Class<?> theElementType ) {
//		Preconditions.checkArgument( !Strings.isNullOrEmpty( theName ), "Name value is null or empty.");
//		Preconditions.checkNotNull( theElementType, "Need an element type to be able to translate." );
//
//		LoadedSetting setting = null;
//		if( properties.containsKey( theName ) ) {
//			setting = SettingValueHelper.generateList( theName, properties.getProperty( theName ), null, false, sourceName, theElementType );
//		}
//		return setting;
		return null;
	}
	
	/**
	 * Gets the configuration value, as a list, for the name given. If the value
	 * doesn't exist a null is returned.
	 * @param theName the name of the value to retrieve
	 * @param theKeyType the type of the key of the map to retrieve
	 * @param theValueType the type of the value of the map to retrieve
	 * @return a configuration setting generated for value, if found, null otherwise
	 */
	public LoadedSetting getMap( String theName, Class<?> theKeyType, Class<?> theValueType ) {
//		Preconditions.checkArgument( !Strings.isNullOrEmpty( theName ), "Name value is null or empty.");
//		Preconditions.checkNotNull( theKeyType, "Need a key type to be able to translate." );
//		Preconditions.checkNotNull( theValueType, "Need a value type to be able to translate." );
//
//		LoadedSetting setting = null;
//		if( properties.containsKey( theName ) ) {
//			setting = SettingValueHelper.generateMap( theName, properties.getProperty( theName ), null, false, sourceName, theKeyType, theValueType );
//		}
//		return setting;
		return null;
	}
	
	/**
	 * Returns the name given to this source. 
	 * If a name wasn't given on construction
	 * then the source path is the name.
	 */
	@Override
	public String getName() {
		return sourceName;
	}

	/**
	 * The profile this client is getting settings from.
	 * @return the profile being used
	 */
	public String getProfile( ) {
		return profile;
	}
	
	/**
	 * The block this client is getting settings from.
	 * @return the block being used
	 */
	public String getBlock( ) {
		return block;
	}

	/**
	 * Helper method that generates a source name for a particular setting.
	 * @param theSetting the setting to generate a source name for
	 * @return the generated source name
	 */
	private String generateSourceName( Setting theSetting ) {
		return sourceName + "." + theSetting.getSourceName( );
	}
}
