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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.talvish.tales.contracts.data.DataContract;
import com.talvish.tales.contracts.data.DataMember;


/**
 * The block describing a set of values that 
 * a client can use for generating ids.
 * @author jmolnar
 *
 */
@DataContract( name ="com.talvish.tales.object_id.id_block")
public class IdBlock {
	@DataMember( name="type_name")private String typeName;
	@DataMember( name="type_id" )private int typeId;
	@DataMember( name="source_id" )private long sourceId;
	@DataMember( name="start_value" )private long startValue;
	@DataMember( name="end_value" )private long endValue;
	
	/**
	 * A constructor to use for serialization.
	 */
	protected IdBlock( ) {
	}
	
	/**
	 * The primary constructor used for generating a block.
	 * @param theSourceId the source that generated the block
	 * @param theTypeName the name of the type that the id was created for
	 * @param theTypeId the type id to use for object ids created from the block
	 * @param theStartValue the starting value for the block of ids that can be created
	 * @param theEndValue the ending value for the block of ids that can be created
	 */
	public IdBlock( long theSourceId, String theTypeName, int theTypeId, long theStartValue, long theEndValue ) {
		Preconditions.checkArgument( theSourceId > 0, "the source id must be greater than 0" );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "the type name must not be null" );
		Preconditions.checkArgument( theTypeId > 0, "the type id must be greater than 0 for type '%s'", theTypeName );
		Preconditions.checkArgument( theStartValue > 0, "the start value must be greater than 0 for type '%s'", theTypeName );
		Preconditions.checkArgument( theEndValue >= theStartValue, "the end value, %s, must be greater than the start value, %s, for type '%s'", theTypeName, theStartValue, theEndValue );

		sourceId = theSourceId;
		typeId = theTypeId;
		startValue = theStartValue;
		endValue = theEndValue;
		typeName = theTypeName;
	}
	
	/**
	 * The type name to use for object ids created from the block.
	 * @return the type name
	 */
	public String getTypeName( ) {
		return typeName;
	}
	
	/**
	 * The type id to use for object ids created from the block.
	 * @return the type id
	 */
	public int getTypeId( ) {
		return typeId;
	}
	
	/**
	 * The source that generated the block
	 * @return the source that generated the block
	 */
	public long getSourceId( ) {
		return sourceId;
	}
	
	/**
	 * The starting value for the block of ids that can be created.
	 * @return the starting value
	 */
	public long getStartValue( ) {
		return startValue;
	}
	
	/**
	 * The ending value for the block of ids that can be created.
	 * @return the ending value
	 */
	public long getEndValue( ) {
		return endValue;
	}
}
