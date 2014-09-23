import java.util.List;

import com.google.common.base.Strings;
import com.tales.businessobjects.ObjectId;
import com.tales.client.http.ResourceResult;
import com.tales.parts.ArgumentParser;
import com.tales.rigs.objectid.client.IdBlock;
import com.tales.rigs.objectid.client.IdType;
import com.tales.rigs.objectid.client.ObjectIdClient;
import com.tales.rigs.objectid.client.ObjectIdManager;
import com.tales.system.configuration.ConfigurationManager;
import com.tales.system.configuration.MapSource;
import com.tales.system.configuration.PropertySource;

public TBD {
    public static void main( String[ ] theArgs ) throws Exception {
    	// get the configuration system up and running
    	ConfigurationManager configurationManager = new ConfigurationManager( );
    	// we prepare two sources for configurations
    	// first the command line source
    	configurationManager.addSource( new MapSource( "command-line", ArgumentParser.parse( theArgs ) ) );
		// second the file source, if the command-line indicates a file is to be used
    	String filename = configurationManager.getStringValue( "settings.file", null ); // we will store config in a file ideally
		if( !Strings.isNullOrEmpty( filename ) ) {
			configurationManager.addSource( new PropertySource( filename ) );
		}

		// now we prepare the client for talking to the server
		String serviceEndpoint = configurationManager.getStringValue( "user_service.endpoint" ); // no default, since we need it to run
    	ObjectIdClient client = new ObjectIdClient( serviceEndpoint, "UserAgentSample/1.0"  );
    	
    	ObjectIdManager manager = new ObjectIdManager( 5, 2, serviceEndpoint, "UserAgentSample/1.0");

    	// next we see what mode we are in, setup or not setup
		String operation = configurationManager.getStringValue( "operation", "get_id_types" );
		
		ResourceResult<IdType> idType;
		
		switch( operation ) {
		case "get_id_types": 
	    	ResourceResult<List<IdType>> idTypes = client.getTypes();
	    	if( idTypes.getResult() != null ) {
	    		logger.debug( "{} types returned", idTypes.getResult().size() );
	    	} else {
	    		logger.debug( "No types returned." );
	    	}
	    	break;
		case "get_id_type_by_id":
			idType = client.getType( 1 );
	    	if( idType.getResult( ) != null ) {
	    		logger.debug( "Last value: {}", idType.getResult().getLastValue() );
	    	} else {
	    		logger.debug( "Type not returned." );
	    	}
	    	break;

		case "get_id_type_by_name":
			idType = client.getType( "user" );
	    	if( idType.getResult( ) != null ) {
	    		logger.debug( "Last value: {}", idType.getResult().getLastValue() );
	    	} else {
	    		logger.debug( "Type not returned." );
	    	}
	    	break;

		case "generate_ids":
			ResourceResult<IdBlock> block = client.generateIds( "user", 2 );
	    	if( block.getResult( ) != null ) {
	    		logger.debug( "Start Id: {} | End Id: {}", block.getResult( ).getStartValue(), block.getResult( ).getEndValue() );
	    	} else {
	    		logger.debug( "Block not returned." );
	    	}
	    	break;
	    	
		case "setup_types":
			ResourceResult<Void> setup = client.setupTypes();
			if( setup.getStatus().getCode().isSuccess() ){
	    		logger.debug( "Setup was successful." );
			} else {
	    		logger.debug( "Setup was not successful." );
			}
			break;

		case "generate_oid":
			ObjectId id;
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			id = manager.generateObjectId( "user" );
			logger.debug( "Generated id '{}'.", id );
			break;

		}
		
    	System.console().writer().print( "Please <Enter> to quit ..." );
    	System.console().writer().flush();
    	System.console().readLine();
    	System.exit( 0 );
	}
}