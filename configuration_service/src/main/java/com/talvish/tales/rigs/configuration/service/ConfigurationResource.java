// ***************************************************************************
// *  Copyright 2016 Joseph Molnar
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
package com.talvish.tales.rigs.configuration.service;

import java.util.List;

import com.google.common.base.Preconditions;
import com.talvish.tales.contracts.services.http.RequestParam;
import com.talvish.tales.contracts.services.http.ResourceContract;
import com.talvish.tales.contracts.services.http.ResourceOperation;
import com.talvish.tales.validation.constraints.NotEmpty;

@ResourceContract( name="com.tales.configuration_contract", versions={ "20160701" } )
public class ConfigurationResource {
	private final ConfigurationEngine engine;
	
	/**
	 * Constructor taking the engine needed by the resource.
	 * @param theEngine the engine to use
	 */
	public ConfigurationResource( ConfigurationEngine theEngine ) {
		Preconditions.checkArgument( theEngine != null, "need an engine" );
		engine = theEngine;
	}

	/**
	 * Gets the settings for the specific profile and block.
	 * @param theProfile
	 * @param theBlock
	 * @return
	 */
	@ResourceOperation( name="get_settings", path="GET : settings" )
	public List<Setting> getSettings( 
			@NotEmpty @RequestParam( name="profile" ) String theProfile, 
			@NotEmpty @RequestParam( name="block" ) String theBlock ) {
		List<Setting> settings = engine.getSettings( theProfile,  theBlock );

		if( settings != null ) {
			return settings;
		} else {
			return null; // TODO: need some errors 
		}
	}
}
