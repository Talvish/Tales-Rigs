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

import java.util.List;

import com.google.common.base.Strings;
import com.talvish.tales.client.http.ResourceClient;
import com.talvish.tales.client.http.ResourceMethod;
import com.talvish.tales.client.http.ResourceResult;
import com.talvish.tales.communication.HttpVerb;
import com.talvish.tales.parts.reflection.TypeUtility;
import com.talvish.tales.system.Conditions;

/**
 * The client for talking to the ObjectIdService. The client is 
 * used to aid in the generation of ObjectIds. ObjectIds allows
 * objects to be globally identifiable in a way that also conveys
 * the type of the object.
 * @author jmolnar
 *
 */
public class ObjectIdClient extends ResourceClient {
    @SuppressWarnings("unused")
	private static List<IdType> idTypes; // this is for reflection to pick up to get all type information
    
    /**
     * The constructor required to create a client.
     * @param theConfiguration the configuration to use to talk to the server.
     * @param theUserAgent the user agent to use while talking to the service
     */
	public ObjectIdClient( ObjectIdConfiguration theConfiguration, String theUserAgent ) {
		super( theConfiguration, "/id", "20141001", theUserAgent ); 

		// we need to retrieve some type information using a bit of a hack
		// due to Java's lack of full type information for generics
//		Field idTypesField = null;
//		try {
//			idTypesField = this.getClass().getDeclaredField( "idTypes" );
//		} catch (NoSuchFieldException e) {
//			throw new IllegalStateException( "Could not create the client due to an issue getting the the field needed for retrieving type information.", e );
//		} catch (SecurityException e) {
//			throw new IllegalStateException( "Could not create the client due to a security issue getting the the field needed for retrieving type information.", e );
//		}
		
		// we now define the methods that we are going to expose for calling
		this.methods = new ResourceMethod[ 5 ];
		
		this.methods[ 0 ] = this.defineMethod( "setup_types", Void.class, HttpVerb.POST, "types/setup" );

		this.methods[ 1 ] = this.defineMethod( "get_id_types", TypeUtility.extractFieldType( this.getClass( ), "idTypes" ), HttpVerb.GET, "types" );

		this.methods[ 2 ] = this.defineMethod( "get_id_type_by_id", IdType.class, HttpVerb.GET, "types/{type_id}" )
				.definePathParameter( "type_id", Integer.class );

		this.methods[ 3 ] = this.defineMethod( "get_id_type_by_name", IdType.class, HttpVerb.GET, "types/{type_name}" )
				.definePathParameter( "type_name", String.class );

		this.methods[ 4 ] = this.defineMethod( "generate_ids", IdBlock.class, HttpVerb.POST, "types/{type_name}/generate_ids" )
				.definePathParameter( "type_name", String.class )
				.defineBodyParameter( "amount", Long.class );

	}
	
	/**
	 * Sets up the types, processing those that may not be ready.
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<Void> setupTypes( ) throws InterruptedException {
		return this.createRequest( this.methods[ 0 ] ).call();
	}
	
	/**
	 * Returns the list of id types in the system.
	 * @return the list of available IdTypes
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<List<IdType>> getTypes( ) throws InterruptedException {
		return this.createRequest( this.methods[ 1 ] ).call();
	}
	
	/**
	 * Returns a specific id type.
	 * @param theTypeId the type id to retrieve information for
	 * @return the information regarding the specified type id
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<IdType> getType( int theTypeId ) throws InterruptedException { 
		Conditions.checkParameter( theTypeId > 0, "the type id must be greater than 0" );
		
		return this.createRequest( this.methods[ 2 ], theTypeId ).call();
	}

	/**
	 * Returns a specific id type.
	 * @param theTypeName the type name to retrieve information for
	 * @return the information regarding the specified type name
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<IdType> getType( String theTypeName ) throws InterruptedException { 
		Conditions.checkParameter( !Strings.isNullOrEmpty( theTypeName ), "the type name must be given" );
		
		return this.createRequest( this.methods[ 3 ], theTypeName ).call();
	}

	/**
	 * A request to generate a block of values for a particular type.
	 * @param theTypeName the type name to generate ids for
	 * @param theAmount the number of values to generate within the block
	 * @throws InterruptedException thrown if the calling thread is interrupted
	 */
	public ResourceResult<IdBlock> generateIds( String theTypeName, long theAmount ) throws InterruptedException { 
		Conditions.checkParameter( !Strings.isNullOrEmpty( theTypeName ), "the type name must be given" );
		Conditions.checkParameter( theAmount > 0, "the number of ids being requested must be greater than 0" );

		return this.createRequest( this.methods[ 4 ], theTypeName )
				.setBodyParameter( "amount", theAmount )
				.call();
	}
}