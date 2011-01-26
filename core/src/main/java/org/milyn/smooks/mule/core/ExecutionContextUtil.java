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
import org.milyn.javabean.BeanAccessor;
import org.milyn.javabean.context.BeanContext;

/**
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public final class ExecutionContextUtil {

    public static final String BEAN_CONTEXT_KEY = "SMOOKS_BEAN_CONTEXT";

	private ExecutionContextUtil() {
	}

	/**
     * Will return a Map containing only the Serializable objects
     * that exist in the passed-in Map if excludeNonSerializables is true.
     *
     * @param executionContext 	- Map containing attributes from the Smooks ExecutionContext
     * @return Map	- Map containing only the Serializable objects from the passed-in map.
     */
	public static Map<Object, Object> getAtrributesMap( final ExecutionContext executionContext, boolean excludeNonSerializables )
	{
    	Map<Object, Object> attributes = executionContext.getAttributes();

    	Map<Object, Object> smooksExecutionContextMap;
        Map<String, Object> beanContextMap = executionContext.getBeanContext().getBeanMap();
    	if ( excludeNonSerializables ) {
            smooksExecutionContextMap = filterSerializable(attributes);
            smooksExecutionContextMap.put(BEAN_CONTEXT_KEY, filterSerializable(beanContextMap));
		} else {
			smooksExecutionContextMap = attributes;
            smooksExecutionContextMap.put(BEAN_CONTEXT_KEY, new HashMap<String, Object>(beanContextMap));
		}


		return Collections.unmodifiableMap(smooksExecutionContextMap);
	}

    private static <K, V> Map<K, V> filterSerializable(Map<K, V> inMap) {
        Map<K, V> outMap = new HashMap<K, V>();

        Set<Map.Entry<K, V> > s = inMap.entrySet();
        for (Map.Entry<K, V>  me : s)
        {
            V value = me.getValue();
            if( value instanceof Serializable)
            {
                outMap.put(me.getKey(), value);
            }
        }
        return outMap;
    }
}
