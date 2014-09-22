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

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tales.businessobjects.ObjectId;

public class ObjectIdGenerator {
	private final String typeName;
	private final int typeId;
	
	private final List<IdBlock> blocks = new LinkedList<>( ); // use a list since it is cheaper to add/remove values, and we always get the head
	private long nextValue = 0;
	private long availableValues = 0; // this is always associated to the first block
	
	public ObjectIdGenerator( String theTypeName, int theTypeId ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "cannot create an id generatork without a type name" );
		Preconditions.checkArgument( theTypeId > 0, "cannot create an id generator for type '%s' without a type id", theTypeId );
		
		typeName = theTypeName;
		typeId = theTypeId;
	}
	
	public String getTypeName( ) {
		return typeName;
	}
	
	public int getTypeId( ) {
		return typeId;
	}
	
	public ObjectId generateObjectId( ) {
		synchronized( this ) {
			long currentValue = nextValue;
			
			IdBlock currentBlock;
			
			currentBlock = blocks.get( 0 );
			if( currentBlock.getEndValue() == nextValue ) {
				blocks.remove( 0 );
				currentBlock = blocks.get( 0 );
				
				if( currentBlock == null ) {
					throw new IllegalStateException( String.format( "Ran out of IdBlocks while attempting to get a value for type '%s'.", typeName ) );
				} else {
					nextValue = currentBlock.getStartValue();
				}
			} else {
				nextValue += 1;
			}
			availableValues -= 1;
			
			return new ObjectId( currentValue, typeId, currentBlock.getSourceId( ) );
		}
	}

	public void addValues( IdBlock aBlock ) {
		Preconditions.checkNotNull( aBlock, "need a block to add a block" );
		synchronized( this ) {
			blocks.add( aBlock );
			if( blocks.size() == 1 ) {
				// if we have only one block, then we are on our first block
				// and need to set the next value to the start
				nextValue = aBlock.getStartValue();
				// we reset these (not a +=, but =) since no others value should be available
				// technically if we get to zero the 'getNextValue' method should throw
				// an exception if more blocks weren't added
				availableValues = ( aBlock.getEndValue() - aBlock.getStartValue() ) + 1;
			} else {
				availableValues += ( aBlock.getEndValue() - aBlock.getStartValue() ) + 1;
			}
		}		
	}
	
	/**
	 * This is used to determine if we should be adding more blocks.
	 * @return returns the number of values available
	 */
	public long getAvailableValues( ) {
		return availableValues;
	}
}
