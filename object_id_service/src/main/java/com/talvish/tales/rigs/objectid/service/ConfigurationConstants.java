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

/**
 * Constants used by the ObjectId Service.
 * @author jmolnar
 *
 */
class ConfigurationConstants {
	public static final String HOSTNAME = "object_id_engine.source";
	public static final String SOURCE_ID_FORMAT = "object_id_engine.sources.%s.id"; // this takes the hostname
	
	public static final String MAXIMUM_CACHE_AGE = "id_source.maximum_cache_age";
	public static final int MAXIMUM_CACHE_AGE_DEFAULT = 86400;
	
	public static final String DATA_DIRECTORY = "object_id_engine.data_directory";
	public static final String SUPPORTED_TYPES = "object_id_engine.types";
	public static final String TYPE_DETAILS_FORMAT = SUPPORTED_TYPES + ".%s";
	public static final String TYPE_FILENAME_FORMAT = "%s.%s.%s.details"; // which is the source id, then type id, then type name
	
	public static final String TYPE_DETAILS_NAME = "name";
	public static final String TYPE_DETAILS_DESCRIPTION ="description";
	public static final String TYPE_DETAILS_ID = "id";
}
