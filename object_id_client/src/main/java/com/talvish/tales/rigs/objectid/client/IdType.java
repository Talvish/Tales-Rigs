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

import com.talvish.tales.businessobjects.TimestampedBase;
import com.talvish.tales.contracts.data.DataContract;
import com.talvish.tales.contracts.data.DataMember;

/**
 * The information for a particular type.
 * @author jmolnar
 *
 */
@DataContract( name ="com.talvish.tales.object_id.id_type")
public class IdType extends TimestampedBase {
	@DataMember( name="name")private String name;
	@DataMember( name="description" )private String description;
	
	@DataMember( name="id" )private int id;

	// we don't expose these two since they aren't relevant from the client's perspective
	// but we leave it on this class since it can be look at for debugging purposes
	@DataMember( name="source")private long source; 
	@DataMember( name="last_value" )private long lastValue;

	/**
	 * Constructor used for serialization.
	 */
	protected IdType( ) {
		
	}
	
	/**
	 * Returns the name for the type.
	 * @return the name
	 */
	public String getName( ) {
		return name;
	}
	
	/**
	 * Returns the description for the type.
	 * @return the description
	 */
	public String getDescription( ) {
		return description;
	}
		
	/**
	 * Gets the type's id value.
	 * @return the type id
	 */
	public int getId( ) {
		return id;
	}
}
