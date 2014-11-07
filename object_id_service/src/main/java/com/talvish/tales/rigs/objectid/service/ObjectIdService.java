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

import com.talvish.tales.services.Service;
import com.talvish.tales.services.http.HttpInterface;
import com.talvish.tales.services.http.ServiceConstants;

/**
 * The main class for the service, which sets up the engine
 * and binds the engine to the resource and the resource to
 * a particular path.
 * @author Joseph Molnar
 *
 */
public class ObjectIdService extends Service {
	private ObjectIdEngine engine;

	public ObjectIdService( ) {
		super( "com.talvish.tales.rigs.objectid.service", "Object Id Service", "A service that generates object ids for other service." );
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		engine = new ObjectIdEngine( this.getConfigurationManager( ) );
		this.interfaceManager.getInterface( ServiceConstants.INTERNAL_INTERFACE_NAME, HttpInterface.class ).bind( new ObjectIdResource( engine ), "/id" );

		// TODO: add status, it would be nice to track the total number of ids generated for the different types
		//this.statusManager.register( "object_id_engine_status", engine.getStatus( ) );
	}
}
