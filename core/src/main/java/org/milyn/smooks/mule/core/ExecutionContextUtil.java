/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.milyn.smooks.mule.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.milyn.container.ExecutionContext;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public final class ExecutionContextUtil {

	private ExecutionContextUtil() {
	}

	/**
     * Will return a Map containing only the Serializable objects
     * that exist in the passed-in Map if {@link #excludeNonSerializables} is true.
     *
     * @param smooksAttribuesMap 	- Map containing attributes from the Smooks ExecutionContext
     * @return Map	- Map containing only the Serializable objects from the passed-in map.
     */
	public static Map<Object, Object> getAtrributesMap( final ExecutionContext executionContext, boolean excludeNonSerializables )
	{
    	Map<Object, Object>  attributes = executionContext.getAttributes();

    	Map<Object, Object>  smooksExecutionContextMap;
    	if ( excludeNonSerializables ) {
    		smooksExecutionContextMap = new HashMap<Object, Object> ();

    		Set<Map.Entry<Object, Object> > s = attributes.entrySet();
    		for (Map.Entry<Object, Object>  me : s)
    		{
    			Object value = me.getValue();
    			if( value instanceof Serializable )
    			{
    				smooksExecutionContextMap.put( me.getKey(), value );
    			}
    		}
		} else {
			smooksExecutionContextMap = attributes;
		}

		return Collections.unmodifiableMap(smooksExecutionContextMap);
	}
}
