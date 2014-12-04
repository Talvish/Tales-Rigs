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
package com.talvish.tales.rigs.objectid.service;

import java.io.File;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
	@DataMember( name="source")private long source;
	@DataMember( name="last_value" )private long lastValue = 0;

	private final File file;
	
	/**
	 * If we add status we would add
	 * Last Request Time
	 * Average Block Size
	 * Total Requested
	 * Request Rate 
	 */
	

	
	/**
	 * Constructor used for serialization.
	 */
	protected IdType( ) {
		file = null;
	}

	/**
	 * Constructor taking the data elements needed for the type.
	 * @param theName the name of the type
	 * @param theDescription the description of the type
	 * @param theId the id of the type
	 * @param theSource the source this instance of the type is for
	 * @param theLastValue the last value generated for the type
	 * @param theFile the file where data is stored for the type
	 */
	public IdType( String theName, String theDescription, int theId, long theSource, long theLastValue, File theFile ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theName ), "cannot create an IdType without a name" );
		Preconditions.checkArgument( theId > 0, "the id %s for type '%s' must be greater than 0", theId, theName );
		Preconditions.checkArgument( theSource > 0, "the source %s for type '%s' must be greater than 0", theSource, theName );
		Preconditions.checkArgument( theLastValue >= 0, "the last value %s for type '%s' must be 0 or greater", theSource, theName );
		Preconditions.checkNotNull( theFile, "the file for type '%s' must not be null", theName );

		// save the passed in elements
		name = theName;
		description = theDescription;
		id = theId;
		source = theSource;
		lastValue = theLastValue;
		
		file = theFile;
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
	
	/**
	 * Gets the source associated with this particular id/value.
	 * @return the source
	 */
	public long getSource( ) {
		return source;
	}
	
	/**
	 * Returns the file associated with the type.
	 * @return the associated file
	 */
	public File getFile( ) {
		return file;
	}
	
	/**
	 * Returns the last value known to be used to generate ObjectIds for 
	 * the associated type.
	 * @return the last value
	 */
	public long getLastValue( ) {
		return lastValue;
	}

	/**
	 * Increments the last value number by the amount specified.
	 * This will essentially absorb those set of numbers is being usable if not used.
	 * This method is type safe.
	 * @param theAmount the amount of ids to absorb, make available for use
	 */
	public void incrementLastValue( long theAmount ) {
		synchronized ( this ) {
			Preconditions.checkArgument( ( Long.MAX_VALUE - theAmount ) > lastValue, "Could not set allocate %s values for type %s/%s on source  %s.", theAmount, name, id, source );

			long newValue = lastValue + theAmount;
			
			ObjectIdEngine.writeValidatedLastValue( name, lastValue, newValue, file);
			lastValue = newValue;
		}
	}
}
