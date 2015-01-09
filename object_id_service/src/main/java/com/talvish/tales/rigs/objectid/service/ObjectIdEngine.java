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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.talvish.tales.parts.naming.NameManager;
import com.talvish.tales.parts.naming.NameValidator;
import com.talvish.tales.parts.naming.SegmentedLowercaseEntityNameValidator;
import com.talvish.tales.system.configuration.ConfigurationManager;

/**
 * The engine for generating ids for ObjectIds.
 * @author Joseph Molnar
 *
 */
public class ObjectIdEngine {
	private static final Logger logger = LoggerFactory.getLogger( ObjectIdEngine.class );

	private static String TYPE_NAME_VALIDATOR = "object_id_type_name";
	
	static {
		if( !NameManager.hasValidator( ObjectIdEngine.TYPE_NAME_VALIDATOR ) ) {
			NameManager.setValidator( ObjectIdEngine.TYPE_NAME_VALIDATOR, new SegmentedLowercaseEntityNameValidator() );
		}
	}
	
	private final ConfigurationManager configurationManager;

	private final File dataDirectory;
	private final long sourceId;
	
	private final int maximumCacheAge;

	// the following variables are volatile since they can 
	// change over the in-memory life-time as new are added
	private volatile Map<Integer,IdType> idTypesById = new HashMap<Integer,IdType>( );
	private volatile Map<String,IdType> idTypesByName = new HashMap<String,IdType>( );
	private volatile List<IdType> externalIdTypes;
	

	/**
	 * Constructor for engine, that takes the ConfigurationManager needed to load/check information.
	 * @param theConfigurationManager the configuration manager to use
	 */
	public ObjectIdEngine( ConfigurationManager theConfigurationManager ) {
		Preconditions.checkNotNull( theConfigurationManager, "the engine requires a configuration manager to load configuration details" );
		
		configurationManager = theConfigurationManager;
		
		String hostname = configurationManager.getStringValue( ConfigurationConstants.HOSTNAME );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "To setup the object id service a host name using setting '%s' must be given.", ConfigurationConstants.HOSTNAME );
		
		// get the underlying source identifier
		Long loadedSourceId = configurationManager.getLongValue( String.format( ConfigurationConstants.SOURCE_ID_FORMAT, hostname ) );
		Preconditions.checkArgument( loadedSourceId != null, "The source id from configuration must be set." );
		sourceId = loadedSourceId;
		Preconditions.checkArgument( sourceId > 0, "The source id from configuration must be greater than zero." );
		
		logger.info( "Service is using id source hostname '{}' which is source id '{}'.", hostname, sourceId );

		// get the caching age
		maximumCacheAge = configurationManager.getIntegerValue( ConfigurationConstants.MAXIMUM_CACHE_AGE, ConfigurationConstants.MAXIMUM_CACHE_AGE_DEFAULT);
		logger.info( "Service allows type caching for up to {} seconds.", maximumCacheAge );

		// now we get the data directory and make sure it exists
		dataDirectory = new File( configurationManager.getStringValue( ConfigurationConstants.DATA_DIRECTORY ) );
		Preconditions.checkState( !dataDirectory.exists() || dataDirectory.isDirectory(), "The specified data directory, '%s', is not a directory.", dataDirectory.toString( ) );
		if( !dataDirectory.exists( ) ) {
			logger.warn( "Creating nonexistent data directory '{}'.", dataDirectory.toString( ) );
			boolean directoryCreated = dataDirectory.mkdir();
			Preconditions.checkState( directoryCreated, "The specified data directory, '%s', could not be created.", dataDirectory.toString( ) );
		} else {
			logger.info( "Using data directory '{}'.", dataDirectory.toString( ) );
		}

		processTypes( false );
	}
	
	/**
	 * The allows maximum age for caches of type information.
	 * This doesn't impact blocks since once allocated nothing
	 * will ever attempt to use the values.
	 * @return the maximum cache age clients may used
	 */
	public int getMaximumCacheAge( ) {
		return this.maximumCacheAge;
	}
	
	/**
	 * Returns the list of id types in the system.
	 * @return the list of available IdTypes
	 */
	public List<IdType> getTypes( ) {
		return externalIdTypes;
	}
	
	/**
	 * Returns a specific id type.
	 * @param theTypeId the type id to retrieve information for
	 * @return the information regarding the specified type id
	 */
	public IdType getType( int theTypeId ) { 
		Preconditions.checkArgument( theTypeId > 0, "the type id must be greater than 0" );
		
		return this.idTypesById.get( theTypeId );
	}

	/**
	 * Returns a specific id type.
	 * @param theTypeName the type name to retrieve information for
	 * @return the information regarding the specified type name
	 */
	public IdType getType( String theTypeName ) { 
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "the type name must not be null or empty" );
		
		return this.idTypesByName.get( theTypeName );
	}
	
	/**
	 * A request to generate a block of values for a particular type.
	 * This doesn't use numbers to lessen chance of error with id creation.
	 * @param theTypeName the type name to generate a block of values for
	 * @param theAmount the number of values to generate within the block
	 * @return returns the generate block
	 */
	public IdBlock generateIds( String theTypeName, long theAmount ) { 
		Preconditions.checkArgument( !Strings.isNullOrEmpty( theTypeName ), "the type name must not be null or empty" );
		Preconditions.checkArgument( theAmount > 0, "the number of ids being requested must be greater than 0" );

		IdBlock idBlock = null;
		IdType idType = this.idTypesByName.get( theTypeName ); 
	
		if( idType != null ) {
			// get the last value so we can re-use it (and not worry about it changing)
			long lastValue = idType.getLastValue();
			// next we increment the last value, which will if we overflow
			idType.incrementLastValue( theAmount ); // if the amount is too big, this will throw an exception
			// now calculate our change
			long startValue = lastValue + 1;
			long endValue = lastValue + theAmount;
			// and generate the block 
			idBlock = new IdBlock( idType.getSource(), idType.getName(), idType.getId(), startValue, endValue );
		}
		return idBlock;
	}

	/**
	 * This method is called to have a look at the config information
	 * and based on the config create any missing types.
	 */
	public void setupTypes( ) {
		processTypes( true );
	}
	
	/**
	 * Helper method that makes sure types are loaded and 
	 * may run setup (create the files) if requested.
	 * @param allowSetup if true means that files that are missing will be setup
	 */
	private void processTypes( boolean allowSetup ) {
		// we can assume the directories are fine, since they were needed for start-up
		List<String> supportedTypes = configurationManager.getListValue( ConfigurationConstants.SUPPORTED_TYPES, String.class );
		Preconditions.checkArgument( supportedTypes.size() > 0, "The list of types supported by the id service must be greater than 0" );
		
		// verify all of the different types
		IdType idType;
		
		Map<Integer,IdType> foundIdTypesById = new HashMap<Integer,IdType>( );
		Map<String,IdType> foundIdTypesByName = new HashMap<String,IdType>( );
		
		for( String foundTypeName : supportedTypes ) {
			idType = processType( foundTypeName, sourceId, allowSetup );
			if( idType != null ) {
				Preconditions.checkState( !foundIdTypesByName.containsKey( idType.getName( ) ), "The type name '%s' is being used by another configuration entry.", idType.getName() ); 
				Preconditions.checkState( !foundIdTypesById.containsKey( idType.getId( ) ), "The type name '%s' has type id '%s' which is being used by another configuration entry.", idType.getName(), idType.getId() );
	
				foundIdTypesById.put( idType.getId(), idType );
				foundIdTypesByName.put( idType.getName( ), idType );
			}
		}
		// we over-write the existing idTypes
		idTypesById = foundIdTypesById;
		idTypesByName = foundIdTypesByName;
		// and we reset the external version, the gap of time between these where the wrong ones
		// could be returned is acceptable and not problematic
		externalIdTypes = Collections.unmodifiableList( new ArrayList<IdType>( idTypesById.values( ) ) );
	}
	
	/**
	 * Helper method that attempts to load and verify information about the type. 
	 * Depending on whether we are in setup mode, it will also create the type
	 * files.
	 * @param theRequestedTypeName the type we are processing
	 * @param theSourceId the source we are processing the type
	 * @param allowSetup whether we are in setup mode or not
	 * @return the IdType to use if an okay type, maybe null if we are not in setup mode and the type doesn't have an existing last value file
	 */
	private IdType processType( String theRequestedTypeName, long theSourceId, boolean allowSetup ) {
		NameValidator nameValidator = NameManager.getValidator( ObjectIdEngine.TYPE_NAME_VALIDATOR );		
		
		Map<String,String> typeConfiguration;
		String typeName;
		String typeDescription;
		String typeIdString;
		int typeId;
		long typeLastValue;
		
		IdType idType = null;
		
		// verify we have the name, it is set right and we have more detailed configuration
		Preconditions.checkArgument( nameValidator.isValid( theRequestedTypeName ), String.format( "The type name '%s' does not conform to validator '%s'.", theRequestedTypeName, nameValidator.getClass().getSimpleName() ) );
		typeConfiguration = configurationManager.getMapValue( String.format( ConfigurationConstants.TYPE_DETAILS_FORMAT, theRequestedTypeName ), String.class, String.class );
		Preconditions.checkArgument(  typeConfiguration != null && typeConfiguration.size() > 0, "The type '%s' does not have a definition set.", theRequestedTypeName );
		
		// now verify the more detailed configuration
		typeName = typeConfiguration.get( ConfigurationConstants.TYPE_DETAILS_NAME );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( typeName ), "The detailed type name for '%s' is not set.", theRequestedTypeName );
		Preconditions.checkArgument( nameValidator.isValid( typeName ), String.format( "The detailed type name '%s' for config type name '%s' does not conform to validator '%s'.", typeName, theRequestedTypeName, nameValidator.getClass().getSimpleName() ) );
		typeDescription = typeConfiguration.get( ConfigurationConstants.TYPE_DETAILS_DESCRIPTION );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( typeDescription ), "The detailed type description for '%s' is not set.", theRequestedTypeName );
		typeIdString = typeConfiguration.get( ConfigurationConstants.TYPE_DETAILS_ID );
		Preconditions.checkArgument( !Strings.isNullOrEmpty( typeIdString ), "The detailed type id for '%s' is not set.", theRequestedTypeName );
		try {
			typeId = Integer.parseInt( typeIdString );
		} catch( NumberFormatException e ) {
			throw new IllegalArgumentException( String.format( "The detailed type id for '%s' is '%s', which is not a valid number.", typeName, typeIdString ), e );
		}
		
		// now we see if we have the data for the type on disk (we should)
		String typeFilename = String.format( ConfigurationConstants.TYPE_FILENAME_FORMAT , theSourceId, typeId, typeName );
		File typeFile = new File( dataDirectory, typeFilename );

		
		Preconditions.checkState( !typeFile.exists() || typeFile.isFile(), "The specified filename, '%s', for type '%s' is not a file.", typeFile.toString( ), typeName );
		
		// we have four main conditions at this point
		// a) file doesn't exist, but we are in setup mode so we create the types and continue
		// b) file doesn't exist, but we ARE NOT in setup mode so we create the types and continue
		// c) file does exist and we have the type loaded in memory, so we ignore
		// d) file does exist and we don't have the type loaded in memory, so we load the file and continue

		if( !typeFile.exists( ) ) {
			if( allowSetup ) {
				// file doesn't exist, but we can set it up so we need to ...
				
				// first, create the file ...
				logger.info( "Creating file '{}' for type '{}' and setting last value to 0.", typeFile.toString( ) );
				boolean fileCreated;
				try {
					fileCreated = typeFile.createNewFile();
				} catch (IOException e) {
					throw new IllegalStateException( String.format( "File '%s' for type '%s' ran into an issue while being created.", typeFile.toString(), typeName ), e );
				}
				Preconditions.checkState( fileCreated, "File '%s' for type '%s' could not be created.", typeFile.toString( ), typeName );

				typeLastValue = 0;
				
				//  second, we need to make sure the default value for last value is there
				writeLastValue( typeName, typeLastValue, typeFile );
				// now create the type so we can return it
				idType = new IdType( 
						typeName, 
						typeDescription, 
						typeId, 
						this.sourceId,
						typeLastValue, 
						typeFile );
			
			} else {
				logger.warn( "Skipping creation of file '{}' for type '{}' since we aren't setting up types.", typeFile.toString( ), typeName );
			}
			
		} else {
			// since the file says it is exists, let's make sure it an actual usable file
			Preconditions.checkState( typeFile.isFile(), "The specified type file, '%s', for '%s' is not a file.", typeFile, typeName ); 
			Preconditions.checkState( typeFile.canRead(), "The specified type file, '%s', for '%s' is not readable.", typeFile, typeName);
			Preconditions.checkState( typeFile.canWrite(), "The specified type file, '%s', for '%s' is not writeable.", typeFile, typeName);

			idType = this.idTypesById.get( typeId );
			if( idType != null ) {
				// let's make sure we are using the same type information
				Preconditions.checkState( idType.getName().equals( typeName ), "The type '%s' is set to use type id '%s' but that id is being used, in memory, by type '%s'.", typeName, typeId, idType.getName( ) );
				// if we are, then simply use the one we loaded
				logger.info( "Not loading file '{}' for type '{}' since the type is already in memory.", typeFile.toString( ), typeName );
			} else {
				typeLastValue = readLastValue( typeName, typeFile );
				logger.warn( "Loading file '{}' for type '{}' since it is not in memory and found last value of {}.", typeFile.toString( ), typeName, typeLastValue );
				// now create the type so we can return it
				idType = new IdType( 
						typeName, 
						typeDescription, 
						typeId, 
						this.sourceId,
						typeLastValue, 
						typeFile );
			}
		}
		
		return idType;
	}
	
	/**
	 * Helper method for reading the last value from the file.
	 * It presumes opens/closes have already happened.
	 * @param theFile the pointer used to help error messages
	 * @return the value read
	 */
	private static long readLastValue( String theTypeName, File theFile ) {
		RandomAccessFile fileAccess = null;
		long lastValue;
		try {

			fileAccess = new RandomAccessFile( theFile, "rwd" );
			lastValue = readLastValue( theTypeName, fileAccess, theFile );
			
			return lastValue;
		
		} catch( IOException e ) {
			throw new IllegalStateException( String.format( "Had trouble writing to type file '%s' for type '%s'.", theFile.toString(), theTypeName ), e );
		} finally {
			if( fileAccess != null ) {
				try {
					fileAccess.close();
				} catch (IOException e) {
					// will absorb but warn
					logger.warn( "Received an exception while trying to close file '{}' for type '{}'.", theFile.toString(), theTypeName );
				}
			}
		}
	}
	
	/**
	 * Helper method for reading the last value from the file.
	 * It presumes opens/closes have already happened.
	 * @param theFileAccess the file object that will be used to read/write from
	 * @param theFile the pointer used to help error messages
	 * @return the value read
	 */
	protected static long readLastValue( String theTypeName, RandomAccessFile theFileAccess, File theFile ) {
		long lastValue = 0;

		try {
			theFileAccess.seek( 0 );
			lastValue = theFileAccess.readLong();

			Preconditions.checkState( theFileAccess.readLine() == null, "File '%s' for type '%s' contains more than the last value.", theFile.toString(), theTypeName );
			Preconditions.checkState( lastValue >= 0, "The last value for type '%s' is '%s', which is not the correct range.", theTypeName, lastValue );
			
			return lastValue;
			
		} catch( IOException e ) {
			throw new IllegalStateException( String.format( "Had trouble reading from type file '%s' for type '%s'.", theFile.toString(), theTypeName ), e );
		}
	}

	
	/**
	 * Helper method for writing the last value from the file.
	 * It presumes opens/closes have already happened.
	 * @param theValue the value to write into the file
	 * @param theFile the pointer used to help error messages
	 */
	private static void writeLastValue( String theTypeName, long theValue, File theFile ) {
		RandomAccessFile fileAccess = null;
		try {

			fileAccess = new RandomAccessFile( theFile, "rwd" );
			writeLastValue( theTypeName, theValue, fileAccess, theFile );
		
		} catch( IOException e ) {
			throw new IllegalStateException( String.format( "Had trouble writing to type file '%s' for type '%s'.", theFile.toString(), theTypeName ), e );
		} finally {
			if( fileAccess != null ) {
				try {
					fileAccess.close();
				} catch (IOException e) {
					// will absorb but warn
					logger.warn( "Received an exception while trying to close file '{}' for type '{}'.", theFile.toString(), theTypeName );
				}
			}
		}
	}
	
	/**
	 * Helper method for writing the last value from the file.
	 * It presumes opens/closes have already happened.
	 * @param theValue the value to write into the file
	 * @param theFileAccess the file object that will be used to read/write from
	 * @param theFile the pointer used to help error messages
	 */
	protected static void writeLastValue( String theTypeName, long theValue, RandomAccessFile theFileAccess, File theFile ) {
		try {
			theFileAccess.seek( 0 ); // note, since we can assume the size given we are writing longs, we don't need to theFileAccess.setLength( 0 ); 
			theFileAccess.writeLong( theValue ); // Long.toString seems to add a space representing a positive-side of a signed number
		} catch( IOException e ) {
			throw new IllegalStateException( String.format( "Had trouble writing to type file '%s' for type '%s'.", theFile.toString(), theTypeName ), e );
		}
	}
	
	/**
	 * Helper method that writes the last value into the appropriate file for the type but validates
	 * that the exist value in memory value is the one from the file first, to ensure there are no
	 * sync issues.
	 * @param theTypeName the type the write is for
	 * @param theOldValue the old value that will be checked against the file to ensure they are the same
	 * @param theNewValue the new value to store in teh file
	 * @param theFile the file location
	 */
	protected static void writeValidatedLastValue( String theTypeName, long theOldValue, long theNewValue, File theFile ) {
		RandomAccessFile fileAccess = null;
		try {
			fileAccess = new RandomAccessFile( theFile, "rwd" );
			
			// first we read the existing value and make sure it is okay
			long fileLastValue = ObjectIdEngine.readLastValue( theTypeName, fileAccess, theFile );
			if( fileLastValue != theOldValue ) {
				throw new IllegalStateException( String.format( "Attempting to increment last value of type '%s' in file '%s' and found that the last value on disk is %s, while in memory is %s.", theTypeName, theFile.toString(), fileLastValue, theOldValue ) ); 
			} else {
				ObjectIdEngine.writeLastValue( theTypeName, theNewValue, fileAccess, theFile );
			}
		} catch( FileNotFoundException e ) {
			// this shouldn't happen given the checks we have when starting/processing types
			throw new IllegalStateException( String.format( "Could not find type file '%s' for type '%s'.", theFile.toString(), theTypeName ), e );
		} finally {
			if( fileAccess != null ) {
				try {
					fileAccess.close();
				} catch (IOException e) {
					// will absorb but warn
					logger.warn( "Received an exception while trying to close file '{}' for type '{}'.", theFile.toString(), theTypeName );
				}
			}
		}		
	}
}
