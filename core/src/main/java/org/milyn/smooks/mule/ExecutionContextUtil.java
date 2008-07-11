package org.milyn.smooks.mule;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    @SuppressWarnings( "unchecked" )
	public static Map getSerializableObjectsMap( final Map smooksAttribuesMap, boolean excludeNonSerializables )
	{
    	Map smooksExecutionContextMap;
    	if ( excludeNonSerializables ) {
    		smooksExecutionContextMap = new HashMap();

    		Set<Map.Entry> s = smooksAttribuesMap.entrySet();
    		for (Map.Entry me : s)
    		{
    			Object value = me.getValue();
    			if( value instanceof Serializable )
    			{
    				smooksExecutionContextMap.put( me.getKey(), value );
    			}
    		}
		} else {
			smooksExecutionContextMap = smooksAttribuesMap;
		}

		return Collections.unmodifiableMap(smooksExecutionContextMap);
	}
}
