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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.talvish.tales.businessobjects.ObjectId;
import com.talvish.tales.client.http.ResourceResult;
import com.talvish.tales.communication.CommunicationException;

/**
 * This class is used to generate actual ObjectIds.
 * It handles all communication, block caching, etc.
 * @author jmolnar
 *
 */
public class ObjectIdManager {
	private static final Logger logger = LoggerFactory.getLogger( ObjectIdManager.class );

	private final Map<String,ObjectIdGenerator> generators = new HashMap<String,ObjectIdGenerator>( ); 
	private final long requestSize;
	private final long requestThreshold;
	private final ObjectIdClient client;
	
	private final Map<String,IdType> idTypesByName = new HashMap<String,IdType>( );
	private final Map<Integer,IdType> idTypesById = new HashMap<Integer, IdType>( );
	private final Object idTypeLock = new Object();
	private volatile LocalDateTime cacheExpiration = LocalDateTime.now(); 
	
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
		
		// TODO: consider starting a thread to get type information
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
	 * Gets type information by the type name.
	 * @param theTypeName the type name to get information for
	 * @return the type information, or null if not found
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	public IdType getType( String theTypeName ) throws InterruptedException {
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "need a type name" );
		
		if( !cacheExpiration.isAfter( LocalDateTime.now() ) ) {
			fetchTypes( ); // TODO: we aren't serving stale, we wait,but we could serve stale and background load
		}
		return idTypesByName.get( theTypeName );
	}
	
	/**
	 * Gets type information by the type id.
	 * @param theTypeId the type id to get information for
	 * @return the type information, or null if not found
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	public IdType getType( int theTypeId ) throws InterruptedException {
		Preconditions.checkArgument( theTypeId >= 0, "need a type id greater than 0" );
		
		if( !cacheExpiration.isAfter( LocalDateTime.now() ) ) {
			fetchTypes( ); // TODO: we aren't serving stale, we wait,but we could serve stale and background load
		}
		return idTypesById.get( theTypeId );
	}
	
	/**
	 * Helper method that grabs and caches all the type information
	 * @throws InterruptedException thrown if thread was interrupted
	 */
	private void fetchTypes( ) throws InterruptedException {
		synchronized( this.idTypeLock ) {
			ResourceResult<List<IdType>> result = client.getTypes();
			
			if( result.getStatus().getCode( ).isSuccess() ) {
				for( IdType type : result.getResult()) {
					idTypesByName.put( type.getName(),  type );
					idTypesById.put( type.getId(),  type );
				}
				LocalDateTime calculatedExpiration = result.calculateExpiration( );
				if( calculatedExpiration != null  ) {
					cacheExpiration = calculatedExpiration;
				} else {
					cacheExpiration = LocalDateTime.now( ).plusMinutes( 5l ); // at least make it cache for a few minutes
				}
				
				logger.info( "Retrieved {} types from the service and caching results until {}", idTypesByName.size(), cacheExpiration );
				
			} else {
				// TODO: the above doesn't handle errors from the server
				//       500 level errors we should throw back
				//       400 level errors we should throw an IllegalArgument, if we can tell it is our type
				//       200 level is fine
				throw new CommunicationException( String.format( 
						"Ran into trouble, '%s', trying to get type information", 
						result.getStatus().getCode( ) ) );
			}
		}
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
		ResourceResult<IdBlock> result = null;

		if( generator == null ) {
			// if we don't have a generator we create some ids and
			// later use the result to create and save the generator
			result = client.generateIds( theTypeName, requestSize );
			
		} else if( generator.getAvailableValues() <= requestThreshold ) {
			// we also generate ids if we are within threshold, we 
			// could collapse code here, but we may be putting in 
			// more logic in these two cases
			result = client.generateIds( theTypeName, requestSize );
		}
		
		if( result != null ) { 
			if( result.getStatus().getCode().isSuccess( ) ) {
				// if the generator is null then we didn't create/save
				// locally so we have to do that
				if( generator == null ) {
					generator = new ObjectIdGenerator( theTypeName, result.getResult().getTypeId() );
					generators.put( theTypeName, generator );
				}
				generator.addValues( result.getResult() );

			} else {
				// TODO: the above doesn't handle errors from the server
				//       500 level errors we should throw back
				//       400 level errors we should throw an IllegalArgument, if we can tell it is our type
				//       200 level is fine
				throw new CommunicationException( String.format( 
						"Ran into trouble, '%s', trying to increase values for ObjectIds of type '%s'", 
						result.getStatus().getCode(), 
						theTypeName ) );
			}
		}
		
		return generator;
	}
}
