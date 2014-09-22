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

import com.google.common.base.Preconditions;
import com.tales.client.http.ResourceResult;

// TODO: need to figure out how to provide help on the response when it comes to 
public class ResultHelper {
	boolean processResult( ResourceResult<?> theResult ) {
		Preconditions.checkNotNull( theResult, "need a result to process it" );

		switch( theResult.getStatus().getCode( ) ) {
		case CALLER_BAD_INPUT:
			
		case CALLER_BAD_STATE:
		case CALLER_BAD_VERSION:
		case CALLER_NOT_FOUND:
		case CALLER_TIMEOUT:
		case CALLER_UNAUTHORIZED:
		
		case DEPENDENCY_BAD_DATA:
		case DEPENDENCY_CANNOT_COMMUNICATE:
		case DEPENDENCY_CANNOT_CONNECT:
		case DEPENDENCY_ERROR:
		case DEPENDENCY_TIMEOUT:
		case DEPENDENCY_UNAVAILABLE:
		
		case LOCAL_ERROR:				// server-issue, probably dont' want to punish again
		case LOCAL_NOT_IMPLEMENTED:		// TODO: we need retry information
		case LOCAL_TIMEOUT:				// commu
		case LOCAL_UNAVAILABLE:			// try again (if timeout)
		
		case OPERATION_ASYNC:			// come back and check later
		case OPERATION_COMPLETED:		// success
		case OPERATION_CREATED:			// success
		case OPERATION_NOT_MODIFIED:	// success, nothing happened
		case OPERATION_RETRY:			// try again
			break;
		default:
				
		}
		
		return false;
	}

}
