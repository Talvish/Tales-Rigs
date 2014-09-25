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

/**
 * A class that manages and caches available values for the a 
 * particular type. This is a helper class for the ObjectIdManager.
 * The rules/mechanism for getting blocks to values to cache is
 * the responsibility of the ObjectIdManager.
 * @author jmolnar
 *
 */
public class ObjectIdGenerator {
	private final String typeName;
	private final int typeId;
	
	private final List<IdBlock> blocks = new LinkedList<>( ); // use a list since it is cheaper to add/remove values, and we always get the head
	private long nextValue = 0;
	private long availableValues = 0; // this is always associated to the first block
	
	/**
	 * Constructor taking the name and numeric id for the type this generator is for.
	 * @param theTypeName the name of the type this generator is for
	 * @param theTypeId the numeric of the type this generator is for
	 */
	public ObjectIdGenerator( String theTypeName, int theTypeId ) {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "cannot create an id generatork without a type name" );
		Preconditions.checkArgument( theTypeId > 0, "cannot create an id generator for type '%s' without a type id", theTypeId );
		
		typeName = theTypeName;
		typeId = theTypeId;
	}
	
	/**
	 * The name of the type this generator is for.
	 * @return the name of the associated type
	 */
	public String getTypeName( ) {
		return typeName;
	}
	
	/**
	 * The id of the type this generator is for.
	 * @return the id of the associated type.
	 */
	public int getTypeId( ) {
		return typeId;
	}
	
	/**
	 * Generates ObjectIds for this type based on IdBlocks that have been added to the generator.
	 * @return a generated ObjectId
	 */
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

	/**
	 * Adds a block of values to be used in the generation of ObjectIds.
	 * This is called by the ObjectIdManager. 
	 * @param aBlock the block of 
	 */
	public void addValues( IdBlock aBlock ) {
		Preconditions.checkNotNull( aBlock, "need a block to add a block" );
		Preconditions.checkArgument( aBlock.getTypeId() == this.typeId, "A block with type id '%s' is attempting to be added to a generator for type '%s'.", aBlock.getTypeId(), this.typeId );
		Preconditions.checkArgument( aBlock.getTypeName().equals( this.typeName ), "A block with type name '%s' is attempting to be added to a generator for type '%s'.", aBlock.getTypeName(), this.typeName );
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
