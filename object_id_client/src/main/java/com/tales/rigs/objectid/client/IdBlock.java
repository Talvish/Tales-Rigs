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
package com.tales.rigs.objectid.client;

import com.tales.contracts.data.DataContract;
import com.tales.contracts.data.DataMember;


// TODO: the items in object id/business objects could perhaps all be under the solutions package?
//       also, need to look at serializers for objects
/**
 * The block describing a set of values that 
 * a client can use for generating ids.
 * @author jmolnar
 *
 */
@DataContract( name ="com.tales.service.id_block")
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
