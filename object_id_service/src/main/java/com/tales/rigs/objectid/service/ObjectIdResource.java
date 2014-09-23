// ***************************************************************************
// *  Copyright (C) 2014 Joseph Molnar. All rights reserved.
// *
// *
// * This source file and its contents are the intellectual property of
// * Joseph Molnar. Except as specifically permitted, no portion of this 
// * source code may be modified, reproduced, copied or distributed without
// * prior written permission.
// *
// * This confidential source file contains trade secrets.
// *
// * The file is a derivative of samples included in the Tales framework that
// * are developed by Joseph Molnar.
// ***************************************************************************
package com.tales.rigs.objectid.service;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.tales.contracts.services.http.PathParam;
import com.tales.contracts.services.http.RequestParam;
import com.tales.contracts.services.http.ResourceContract;
import com.tales.contracts.services.http.ResourceOperation;
import com.tales.system.Conditions;

/**
 * HTTP resource contract for generated ids for client services.
 * @author Joseph Molnar
 *
 */
@ResourceContract( name="com.tales.object_id_contract", versions={ "20141001" } )
public class ObjectIdResource {
	private final ObjectIdEngine engine;
	
	/**
	 * Constructor taking the engine needed by the resource.
	 * @param theEngine the engine to use
	 */
	public ObjectIdResource( ObjectIdEngine theEngine ) {
		Preconditions.checkArgument( theEngine != null, "need an engine" );
		engine = theEngine;
	}
	
	/**
	 * Sets up the types, processing those that may not be ready.
	 */
	@ResourceOperation( name="setup_types", path="GET | POST : types/setup" )
	public void setupTypes( ) {
		engine.setupTypes();
	}

	/**
	 * Returns the list of id types in the system.
	 * @return the list of available IdTypes
	 */
	@ResourceOperation( name="get_id_types", path="GET : types" )
	public List<IdType> getTypes( ) {
		return engine.getTypes();
	}


	/**
	 * Returns a specific id type.
	 * @param theTypeId the type id to retrieve information for
	 * @return the information regarding the specified type id
	 */
	@ResourceOperation( name="get_id_type_by_id", path="GET : types/{type_id : [0-9]+}" ) // regex separates from below method, this must be a number
	public IdType getType( @PathParam( name="type_id" )int theTypeId ) { 
		Conditions.checkParameter( theTypeId > 0, "the type id must be greater than 0" );
		
		IdType idType = engine.getType( theTypeId );
		Conditions.checkFound( idType != null, "Could not find the type identified by the id '%s'.", theTypeId );
		
		return idType;
	}

	/**
	 * Returns a specific id type.
	 * @param theTypeName the type name to retrieve information for
	 * @return the information regarding the specified type name
	 */
	@ResourceOperation( name="get_id_type_by_name", path="GET : types/{type_name : [a-zA-Z_].*}" ) // regex separates from above method, this must start with a letter or underscore
	public IdType getType( @PathParam( name="type_name" )String theTypeName ) { 
		Conditions.checkParameter( !Strings.isNullOrEmpty( theTypeName ), "the type name must not be null or empty" );
		
		IdType idType = engine.getType( theTypeName );
		Conditions.checkFound( idType != null, "Could not find the type identified by the name '%s'.", theTypeName );
		
		return idType;
	}

	/**
	 * A request to generate a block of values for a particular type.
	 * @param theTypeId the type id to generate a block of values for
	 * @param theAmount the number of values to generate within the block
	 * @return returns the generate block
	 */
	@ResourceOperation( name="generate_ids", path="POST : types/{type_name}/generate_ids" )
	public IdBlock generateIds( 
			@PathParam( name="type_name" )String theTypeName,
			@RequestParam( name="amount")int theAmount ) { 
		Conditions.checkParameter( !Strings.isNullOrEmpty( theTypeName ), "the type name must not be null or empty" );
		Conditions.checkParameter( theAmount > 0, "the number of ids being requested must be greater than 0" );

		IdBlock idBlock = engine.generateIds( theTypeName, theAmount);
		Conditions.checkFound( idBlock != null, "Could not find the type identified by the name '%s'.", theTypeName );
		
		return idBlock;
	}
}
