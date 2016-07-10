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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.talvish.tales.client.http.ResourceConfigurationBase;
import com.talvish.tales.system.configuration.annotated.Setting;
import com.talvish.tales.system.configuration.annotated.Settings;

/**
 * The configuration for communication with the configuration service.
 * @author jmolnar
 *
 */
@Settings( prefix="configuration_service" )
public class ConfigurationConfiguration extends ResourceConfigurationBase<ConfigurationConfiguration> {
	@Setting( name="{prefix}.profile", required=true )
	private String profile;

	@Setting( name="{prefix}.block", required=true )
	private String block;

	
	@Setting( name="{prefix}.clear_cache" )
	private boolean clearCache = false;
	
	/**
	 * Gets the profile that this configuration should used.
	 * @return the profile to use for the configuration
	 */
	public String getProfile( ) {
		return profile;
	}

	/**
	 * Sets the profile to use for getting configuration.
	 * @param theProfile the profile to use, the value must be set
	 */
	public void setProfile( String theProfile ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theProfile ), "need a profile" );
		profile = theProfile;
	}

	/**
	 * Gets the block that this configuration should used.
	 * @return the block to use for the configuration
	 */
	public String getBlock( ) {
		return block;
	}

	/**
	 * Sets the block to use for getting configuration.
	 * @param theBlock the profile to use, the value must be set
	 */
	public void setBlock( String theBlock ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theBlock ), "need a block" );
		block = theBlock;
	}

	/**
	 * Indicates if any local cache should be cleared.
	 * @return true if the cache should be clear, false if not
	 */
	public boolean shouldClearCache(  ) {
		return clearCache;
	}
	
	/**
	 * Sets whether the cache should be cleared.
	 * @param shouldClearCache true if the cache should be clear, false if not
	 * @return this configuration object to chain settings
	 */
	public ConfigurationConfiguration setClearCache( boolean shouldClearCache ) {
		clearCache = shouldClearCache;
		return this;
	}
}
