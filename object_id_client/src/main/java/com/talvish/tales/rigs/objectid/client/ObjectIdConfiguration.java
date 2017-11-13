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

import com.talvish.tales.client.http.ResourceConfigurationBase;
import com.talvish.tales.system.configuration.annotated.Setting;
import com.talvish.tales.system.configuration.annotated.Settings;
import com.talvish.tales.validation.Conditions;

/**
 * The configuration needed to setup the object id manager to retrieving object ids locally.
 * @author jmolnar
 *
 */
@Settings( prefix="object_id_service" )
public class ObjectIdConfiguration extends ResourceConfigurationBase<ObjectIdConfiguration> {
	
	@Setting( name="{prefix}.request_amount" )
	private long requestAmount = 100;

	@Setting( name="{prefix}.request_threshold" )
	private long thresholdAmount = 20;
	
	/**
	 * Default constructor for serialization.
	 */
	public ObjectIdConfiguration( ) {
	}
	
	/**
	 * The number of values that will be requested when new values are needed.
	 * @return the number of values that will be requested from the service
	 */
	public long getRequestAmount( ) {
		return requestAmount;
	}

	/**
	 * Sets the number of ids that are requested at one time.
	 * @param theAmount the number of ids to request at a time
	 * @return the configuration object so setters can be chained
	 */
	public ObjectIdConfiguration setRequestAmount( long theAmount ) {
		requestAmount = theAmount;
		return this;
	}
	
	/**
	 * This represents the number of values that must be left in the pool to cause a request to get more ids. 
	 * @return the number of values that must be left in the pool to cause a request to get more ids
	 */
	public long getRequestThreshold( ) {
		return thresholdAmount;
	}
	
	/**
	 * Sets the number of ids that must be in an id pool before a request is made to get more ids.
	 * @param theThreshold the number of is in the pool before a request is made
	 * @return the configuration object so setters can be chained
	 */
	public ObjectIdConfiguration setRequestThreshold( long theThreshold ) {
		Conditions.checkConfiguration( theThreshold > 0, "the threshold amount has to be greater than zero" );
		thresholdAmount = theThreshold;
		return this;
	}


	@Override
	public void validate( ) {
		super.validate( );
		Conditions.checkConfiguration( requestAmount > 0, "the request amount has to be greater than zero" );
		Conditions.checkConfiguration( thresholdAmount > 0, "the threshold amount has to be greater than zero" );
		Conditions.checkConfiguration( requestAmount > thresholdAmount, "the request amount '%s' has to be greater than the threshold amount '%s'", requestAmount, thresholdAmount );
	}
}
