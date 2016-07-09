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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.talvish.tales.system.configuration.annotated.Setting;
import com.talvish.tales.system.configuration.annotated.Settings;

@Settings( prefix="configuration_engine" )
public class ConfigurationEngineConfiguration {
	@Setting( name="{prefix}.settings.file", required=true )
	private String settingsFilename;
	
	//@Setting( name="{prefix}.settings.profile", required=true )
	//private String settingsProfile;


	
	public String getSettingsFilename( ) {
		return settingsFilename;
	}
	
	public ConfigurationEngineConfiguration setSettingsFilename( String theFile ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theFile ), "a file must be provided" );
		settingsFilename = theFile;
		return this;
	}
	
//	public String getSettingsProfile( ) {
//		return settingsProfile;
//	}
//	
//	public ConfigurationEngineConfiguration setSettingsProfile( String theProfile ) {
//		Preconditions.checkArgument( !Strings.isNullOrEmpty( theProfile ), "a profile must be provided" );
//		settingsProfile = theProfile;
//		return this;
//	}
}