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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tales.businessobjects.ObjectId;
import com.tales.client.http.ResourceResult;
import com.tales.communication.CommunicationException;

/**
 * This class is used to generate actual ObjectIds.
 * It handles all communication, block caching, etc.
 * @author jmolnar
 *
 */
public class ObjectIdManager {
	private final Map<String,ObjectIdGenerator> generators = new HashMap<String,ObjectIdGenerator>( ); 
	private final long requestSize;
	private final long requestThreshold;
	private final ObjectIdClient client;
	
	// TODO: add async handling, which will be part of internalPrepare and may impact generateObjectId
	//       (e.g. force requests, or what if some are outstanding, etc)
	//private final ExecutorService executorService = Executors.newSingleThreadExecutor(); 
	
	/**
	 * Constructor taking the required elements to work
	 * @param theRequestSize the number of values that will be requested from the service whenever a new set of values are needed
	 * @param theRequestThreshold when there are this number of unused values for a type it will request for more values from the service
	 * @param theServiceEndpoint the endpoint where the object id service is located
	 * @param theUserAgent the user agent to use when communicating with other services
	 */
	public ObjectIdManager( long theRequestSize, long theRequestThreshold, String theServiceEndpoint, String theUserAgent ) {
		Preconditions.checkArgument( theRequestSize > 0, "the request size must be greater than 0" );
		Preconditions.checkArgument( theRequestThreshold > 0, "the request threshold must be greater than 0" );
		Preconditions.checkArgument( theRequestThreshold <= theRequestSize, "the request size, %s, should be at least the same size is the theshold, %s, and ideally bigger", theRequestSize, theRequestThreshold );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theServiceEndpoint ), "the service endpoint must be specified" );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theUserAgent ), "the user agent must be specified" );
		
		requestSize = theRequestSize;
		requestThreshold = theRequestThreshold;
		
		client = new ObjectIdClient( theServiceEndpoint, theUserAgent );
	}
	
	/**
	 * Will generate an Objectid for a particular type
	 * @param theTypeName the type to generate an ObjectId for
	 * @return the generated ObjectId
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	public ObjectId generateObjectId( String theTypeName ) throws InterruptedException {
		return internalPrepare( theTypeName ).generateObjectId( ); // this will validate the name
	}

	/**
	 * Forces to manage to evaluate whether there are a enough values for a given type
	 * @param theTypeName the name of the type to prepare
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	public void prepare( String theTypeName ) throws InterruptedException {
		internalPrepare( theTypeName ); // this will validate the name
	}
	
	/**
	 * Forces the manager to evaluate whether there are a enough values for the specified types
	 * @param theTypeNames the name of the types to prepare
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	public void prepare( String ... theTypeNames ) throws InterruptedException {
		Preconditions.checkNotNull( theTypeNames, "need type names to prepare" );

		for( String typeName : theTypeNames ) {
			internalPrepare( typeName );
		}
	}	

	/**
	 * Forces the manager to evaluate whether there are enough values for a given type
	 * and returns the generator to use in case an ObjectId is to be generated
	 * @param theTypeName the name of the type to prepare
	 * @return the generator for the given type
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	private ObjectIdGenerator internalPrepare( String theTypeName ) throws InterruptedException {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "need a type name to prepare" );
		
		ObjectIdGenerator generator = generators.get( theTypeName );
		ResourceResult<IdBlock> response = null;

		if( generator == null ) {
			// if we don't have 
			response = client.generateIds( theTypeName, requestSize );
			generator = new ObjectIdGenerator( theTypeName, response.getResult().getTypeId() );
			generators.put( theTypeName, generator );
		} else if( generator.getAvailableValues() <= requestThreshold ) {
			response = client.generateIds( theTypeName, requestSize );
		}
		if( response != null ) { 
			if( response.getStatus().getCode().isSuccess( ) ) {
				generator.addValues( response.getResult() );
			} else {
				// TODO: the above doesn't handle errors from the server
				//       500 level errors we should throw back
				//       400 level errors we should throw an IllegalArgument, if we can tell it is our type
				//       200 level is fine
				throw new CommunicationException( String.format( 
						"Ran into trouble, '%s', trying to increase values for ObjectIds of type '%s'", 
						response.getStatus().getCode(), 
						generator.getTypeName() ) );
			}
		}
		
		return generator;
	}
}
