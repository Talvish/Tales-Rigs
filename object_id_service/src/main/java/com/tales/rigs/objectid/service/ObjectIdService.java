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
package com.tales.rigs.objectid.service;

import com.google.common.base.Strings;
import com.tales.services.http.HttpInterface;
import com.tales.services.http.HttpService;
import com.tales.system.configuration.PropertySource;

/**
 * The is an example of a user service that is built using many of the patterns
 * I've tried to use when building up service while taking advantage of the
 * capabilities of the tales framework.
 * This is nearly a fully working sample. While it runs, it currently does not
 * do any real persistence.
 * <br>
 * For browsing samples, this should the LAST to look at.
 * @author Joseph Molnar
 *
 */
public class ObjectIdService extends HttpService {
	private ObjectIdEngine engine;

	protected ObjectIdService( ) {
		super( "object_id_service", "Object Id Service", "A service that generates object ids for other service." );
	}
	
	@Override
	protected void onInitializeConfiguration() {
		String filename = this.getConfigurationManager( ).getStringValue( "settings.file", null ); // get a config filename	 from command-line, if available
		
		if( !Strings.isNullOrEmpty( filename ) ) {
			this.getConfigurationManager( ).addSource( new PropertySource( filename) );
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		
		HttpInterface httpInterface = new HttpInterface( "internal", this );		
		this.interfaceManager.register( httpInterface );
		
		engine = new ObjectIdEngine( this.getConfigurationManager( ) );
		httpInterface.bind( new ObjectIdResource( engine ), "/id" );
		// TODO: add status, it would be nice to track the total number of ids generated for the different types
		//this.statusManager.register( "object_id_engine_status", engine.getStatus( ) );
	}
	
    public static void main( String[ ] args ) throws Exception {
    	ObjectIdService service = new ObjectIdService( );
    	
    	service.start( args );
    	service.run( );
    	service.stop( );
	}
}
