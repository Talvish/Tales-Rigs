package com.talvish.tales.rigs.configuration.client;
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

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.talvish.tales.contracts.data.DataContract;
import com.talvish.tales.contracts.data.DataMember;
import com.talvish.tales.serialization.json.UnmappedName;
import com.talvish.tales.serialization.json.UnmappedValue;

/**
 * This class represents the wire version of a setting to return.
 * @author jmolnar
 */

/**
 * The client will support storing on disk, which we may want to support
 * as something we could load in a source without all the overhead
 * in it we should store the datetime of the request as well 
 * as the datetime of when it could expire.
 * @author jmolnar
 *
 */

@DataContract( name="com.talvish.tales.configuration.setting")
public class Setting {
	@UnmappedName( )
	@DataMember( name="name" )
	private String name;
	
	@UnmappedValue( )
	@DataMember( name="value" )
	private JsonElement value;

	@DataMember( name="description" )
	private String description;

	@DataMember( name="sensitive" )
	private boolean sensitive;
	
	@DataMember( name="source_name" )
	private String sourceName;
	
	
	/**
	 * A simple constructor needed for serialization.
	 */
	protected Setting( ) {
	}
	
	/**
	 * Constructor taking the required data to get a setting off the ground.
	 * It provides the first piece of history for how this setting came to be.
	 * This is only called when creating a leaf in the history. 
	 * @param theSetting the initial descriptor for the setting 
	 */
	public Setting( com.talvish.tales.system.configuration.hierarchical.Setting theSetting ) {
		Preconditions.checkArgument( theSetting != null, "Attempting to create a setting without a source setting." );
		name = theSetting.getName();
		description = theSetting.getDescription();
		value = theSetting.getValue();
		sensitive = theSetting.isSensitive( );
		sourceName = theSetting.getSourceName( );
	}

	/**
	 * The name given to the setting.
	 * This is required.
	 * @return
	 */
	public String getName( ) {
		return name;
	}
	
	/**
	 * The description given to the setting.
	 * @return the description of the block
	 */
	public String getDescription( ) {
		return description;
	}
	
	/**
	 * The value given to the setting.
	 * @return the value of the setting
	 */
	public JsonElement getValue( ) {
		return value;
	}
	
	/**
	 * Indicates if this setting is consider sensitive. If sensitive the intention is for the setting value to
	 * not be dumped into log files or be made generally visible.
	 * @return true means  
	 */
	public boolean isSensitive( ) {
		return sensitive;
	}
	
	/**
	 * A string version of the specific source, profile and block that sourced the setting.
	 * This is only set once the setting has been validated.
	 * @return the source name
	 */
	public String getSourceName( ) {
		return sourceName;
	}	
}