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
package org.milyn.smooks.mule.core.ext;

import java.util.UUID;

import org.milyn.SmooksException;
import org.milyn.cdr.Parameter;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.javabean.DataDecoder;
import org.milyn.smooks.mule.core.Constants;
import org.milyn.smooks.mule.core.MuleDispatcher;
import org.milyn.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Type decoder parameter mapping visitor.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class DecodeParamResolver implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);

    	NodeList decodeParams = element.getElementsByTagNameNS(Constants.MULE_SMOOKS_NAMESPACE, "decodeParam");

    	// Check if this property has decode parameters.
    	if(decodeParams.getLength() > 0) {

    		try {
    			// Create a new resource configuration for the configured decoder
                SmooksResourceConfiguration decoderConfig = new SmooksResourceConfiguration();
                extensionContext.addResource(decoderConfig);

            	// retrieve the original decoder name
            	String decoderName = element.getAttribute("decoder");

            	// Create the data decoder. This also checks if the data decoder can be found
            	DataDecoder decoder = DataDecoder.Factory.create(decoderName);

            	// generate a random new decoder name
                String reType = UUID.randomUUID().toString();

                // set the new decoder name in the xml
                element.setAttribute("decoder", reType);

                // Configure the new decoder config...
                decoderConfig.setSelector("decoder:" + reType);
                decoderConfig.setTargetProfile(extensionContext.getDefaultProfile());
                decoderConfig.setResource(decoder.getClass().getName());
                for(int j = 0; j < decodeParams.getLength(); j++) {
                    Element decoderParam = (Element) decodeParams.item(j);
                    decoderConfig.setParameter(decoderParam.getAttribute("name"), DomUtils.getAllText(decoderParam, true));
                }
    		 } finally {
    			 extensionContext.getResourceStack().pop();
    	     }
    	}
    }
}