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

package org.milyn.smooks.mule;

import static org.milyn.smooks.mule.Constants.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;
import static org.milyn.smooks.mule.Constants.MESSAGE_PROPERTY_KEY_PROFILE;
import static org.mule.config.i18n.MessageFactory.createStaticMessage;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.container.plugin.PayloadProcessor;
import org.milyn.event.report.HtmlReportGenerator;
import org.mule.config.i18n.Message;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The Transformer intended to be used with the Mule ESB.
 * <p/>
 * <h3>Usage:</h3>
 * <pre>
 * Declare the transformer in the Mule configuration file:
 * &lt;transformers&gt;
 *      &lt;transformer name="SmooksTransformer" className="org.milyn.smooks.mule.Transformer"&gt;
 *      	&lt;properties&gt;
 *      		&lt;property name="configFile" value="smooks-config.xml"/&gt;
 *      	&lt;/properties&gt;
 *      &lt;/transformer&gt;
 * &lt;/transformers&gt;
 *
 * Configure the router with a transformer:
 * &lt;inbound-router&gt;
 *     &lt;endpoint address="stream://System.in"  transformers="SmooksTransformer"/&gt;
 * &lt;/inbound-router&gt;</pre>
 *
 * <h3>Description of configuration properties</h3>
 * <ul>
 * <li><i>configFile</i> - the Smooks configuration file. Can be a path on the file system or on the classpath.
 * <li><i>resultType</i> - type of result expected from Smooks ("STRING", "BYTES", "JAVA", "RESULT", "NORESULT"). Default is "STRING".
 * <li><i>javaResultBeanId</i> - specifies the Smooks bean context beanId to be mapped as the result when the resultType is "JAVA".  If not specified,
 *                               the whole bean context bean Map is mapped as the result.
 * <li><i>resultClass</i> - When the resultType is set to "RESULT" then this attribute defines the Result Class which will be used.
 * 							The class must implement the {@link javax.xml.transform.Result} interface and must have an argumentless constructor.
 * <li><i>resultFactoryClass</i> - When the resultType is set to "RESULT" then this attribute defines the ResultFactory	Class which will be used
 * 								   to create the	Result Class. The class must implement the	{@link org.milyn.smooks.mule.ResultFactory} interface and
 * 								   must have an argumentless constructor.
 * <li><i>profile</i> - the smooks profile to execute. If a profile name was found on the message then that one is used.
 * <li><i>profileMessagePropertyKey</i> - the message property to look for a possible profile name. If the property is set and the value is a string then
 *                                        that value is used as profile name. Default "MessageProfile".
 * <li><i>executionContextAsMessageProperty</i> - If set to "true" then the attributes map of the Smooks execution context is added to the message properties.
 * 												  The property key is defined with the executionContextMessagePropertyKey property. Default is "false"
 * <li><i>executionContextMessagePropertyKey</i> - The property key under which the execution context is put. Default is "SmooksExecutionContext"
 * <li><i>excludeNonSerializables</i> - if true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
 * <li><i>reportPath</i> - specifies the path and file name for generating a Smooks Execution Report.  This is a development tool.
 * </ul>
 *
 * <h3>Accessing Smooks ExecutionContext attributes</h3>
 * After Smooks finished filtering they payload and if the "executionContextAsMessageProperty" property is set to <code>true</code>
 * then the transform method will make the attributes that have been set in the the ExecutionContext available for
 * other actions in the Mule ESB by setting the attributes map as a property of the message.
 * The attributes can be accessed by using the key defined under the property "executionContextMessagePropertyKey". Default
 * "SmooksExecutionContext" is used, which is set under the constant {@link #MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT}.
 * An example of accessing the attributes map is:
 * <pre>
 * umoEventContext.getMessage().get( SmooksTransformer.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT );
 * </pre>
 *
 * <h3>Specifying the Source and Result Types</h3>
 * From the object payload data type, this Transformer is able to automatically determine the type of
 * {@link javax.xml.transform.Source} to use (via the Smooks {@link PayloadProcessor}).  The
 * {@link javax.xml.transform.Result} type to be used can be specified via the "resultType"
 * property, as outlined above. If the result is required but shouldn't be a String, Bytes or Java then it
 * is also possible to set the "resultClass" property or the "resultFactoryClass" properties. The resultType
 * must be set to "RESULT" then.
 * By defining the resultClass property with a classname the transformer will try to instantiate that class.
 * The defined class must have a argumentless constructor and must implement the {@link javax.xml.transform.Result} interface.
 * The resulting Result object will be used by Smooks as result and will be returned as the transformation result.
 * If you need to use a factory class to instantiate the result class then this is also possible. By
 * setting the "resultFactoryClass" with the class name of the factory the transformer will instantiate
 * that factory an use its createResult() method to instantiate the Result object.
 * The Factory must implement the {@link org.milyn.smooks.mule.ResultFactory} and have an argumentless constructor.
 *
 * <p/>
 * It is expected that the above mechanism will be satisfactory for most use cases, but not all.
 * For the other use cases, this transformer supports {@link org.milyn.container.plugin.SourceResult}
 * payloads. This allows you to manually specify other Source and Result
 * types.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class Transformer extends AbstractEventAwareTransformer
{

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger( Transformer.class );

	/*
	 * Smooks payload processor
	 */
	private SmooksPayloadProcessor smooksPayloadProcessor;

	/*
	 * Smooks instance
	 */
	private Smooks smooks;

	/*
	 * The expected result type.
	 */
	private String resultType;

	/*
	 * Filename for smooks configuration. Default is smooks-config.xml
	 */
    private String configFile;

    /*
	 * The smooks profile to be used
	 */
    private String profile;

	/*
	 * The key name where the message profile can be located
	 */
    private String profileMessagePropertyKey = MESSAGE_PROPERTY_KEY_PROFILE;


    /*
     * If true, then the execution context is set as property on the message
     */
    private boolean executionContextAsMessageProperty = false;

	/*
     * The key name of the execution context message property
     */
    private String executionContextMessagePropertyKey = MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;

    /*
     * If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	private boolean excludeNonSerializables = true;

	/*
	 * Path where the Smooks Report will be generated.
	 */
	private String reportPath;

	/*
	 * Bean Id under which the JavaResult bean will be bound
	 */
	private String javaResultBeanId;

	/*
	 * The classname of the class that will be used as Result object
	 */
	private String resultClass;

	/*
	 * The classname of the factory class that will be used to create the Result object
	 */
	private String resultFactoryClass;


	@Override
	public void initialise() throws InitialisationException {

		//	Create the Smooks instance
		smooks = createSmooksInstance();

		// Create the payload processor
		smooksPayloadProcessor = createSmooksPayloadProcessor();

	}

	/**
	 * @return the executionContextAsMessageProperty
	 */
	public boolean isExecutionContextAsMessageProperty() {
		return executionContextAsMessageProperty;
	}

	/**
	 * @return the executionContextMessagePropertyKey
	 */
	public String getExecutionContextMessagePropertyKey() {
		return executionContextMessagePropertyKey;
	}

	/**
	 * @return the excludeNonSerializables
	 */
	public boolean isExcludeNonSerializables() {
		return excludeNonSerializables;
	}

	/**
	 * @return the reportPath
	 */
	public String getReportPath() {
		return reportPath;
	}

	/**
	 * @return the javaResultBeanId
	 */
	public String getJavaResultBeanId() {
		return javaResultBeanId;
	}

	public String getConfigFile()
	{
		return configFile;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * @return the profileMessagePropertyKey
	 */
	public String getProfileMessagePropertyKey() {
		return profileMessagePropertyKey;
	}

	/**
	 * @param profileMessagePropertyKey the profileMessagePropertyKey to set
	 */
	public void setProfileMessagePropertyKey(String profileMessagePropertyKey) {
		this.profileMessagePropertyKey = profileMessagePropertyKey;
	}

	/**
	 * @return the resultType
	 */
	public String getResultType() {
		return resultType;
	}

	public void setConfigFile( final String configFile )
	{
		this.configFile = configFile;
	}

	public String getResultClass() {
		return resultClass;
	}

	public void setResultClass(String resultClass) {
		this.resultClass = resultClass;
	}


	public String getResultFactoryClass() {
		return resultFactoryClass;
	}


	public void setResultFactoryClass(String resultFactoryClass) {
		this.resultFactoryClass = resultFactoryClass;
	}

    @Override
	public Object clone() throws CloneNotSupportedException
    {
    	return this;
    }

	public void setResultType( final String resultType )
	{
		this.resultType = resultType;
	}

    /**
	 * @param setExecutionContextMessageProperty the setExecutionContextMessageProperty to set
	 */
	public void setExecutionContextAsMessageProperty(
			boolean executionContextMessageProperty) {
		this.executionContextAsMessageProperty = executionContextMessageProperty;
	}

	/**
	 * @param executionContextMessagePropertyKey the executionContextMessagePropertyKey to set
	 */
	public void setExecutionContextMessagePropertyKey(
			String executionContextMessagePropertyKey) {

		if ( executionContextMessagePropertyKey == null )
		{
			throw new IllegalArgumentException( "'executionContextMessagePropertyKey' can not be set to null." );
		}
		if ( executionContextMessagePropertyKey.length() == 0 )
		{
			throw new IllegalArgumentException( "'executionContextMessagePropertyKey' can not be set to an empty string." );
		}

		this.executionContextMessagePropertyKey = executionContextMessagePropertyKey;
	}

    /**
     * @param excludeNonSerializables  - If true, non serializable attributes from the Smooks ExecutionContext will no be included. Default is true.
     */
	public void setExcludeNonSerializables( boolean excludeNonSerializables )
	{
		this.excludeNonSerializables = excludeNonSerializables;
	}

	public void setReportPath( final String reportPath )
	{
		this.reportPath = reportPath;
	}

	public void setJavaResultBeanId( final String javaResultBeanId )
	{
		this.javaResultBeanId = javaResultBeanId;
	}



	//	protected

	@Override
	public Object transform( final Object payload, String encoding, UMOEventContext umoEventContext ) throws TransformerException
	{
        //	Create Smooks ExecutionContext.
		ExecutionContext executionContext;

		String profile = retrieveProfile(umoEventContext.getMessage());
		if(profile != null) {
			executionContext = smooks.createExecutionContext(profile);
		} else {
			executionContext = smooks.createExecutionContext();
		}

		//	Add smooks reporting if configured
		addReportingSupport( executionContext );

        //	Use the Smooks PayloadProcessor to execute the transformation....
        final Object transformedPayload = smooksPayloadProcessor.process( payload, executionContext );

        if(executionContextAsMessageProperty) {

        	//	Set the Smooks Excecution properties on the Mule Message object
        	umoEventContext.getMessage().setProperty( executionContextMessagePropertyKey, ExecutionContextUtil.getAtrributesMap(executionContext, excludeNonSerializables) );
        }

		return transformedPayload;
	}


	private String retrieveProfile(UMOMessage message) {

		Object messageProfile = message.getProperty(profileMessagePropertyKey);

		if(messageProfile != null && messageProfile instanceof String) {

			return (String) messageProfile;

		}

		return profile;
	}

	private Smooks createSmooksInstance() throws InitialisationException
	{
		if ( configFile == null )
		{
			final Message errorMsg = createStaticMessage( "'smooksConfigFile' parameter must be specified" );
			throw new InitialisationException( errorMsg, this );
		}

		try
		{
			return new Smooks ( configFile );
		}
		catch ( final IOException e)
		{
			final Message errorMsg = createStaticMessage( "IOException while trying to get smooks instance: " );
			throw new InitialisationException( errorMsg, e);
		}
		catch ( final SAXException e)
		{
			final Message errorMsg = createStaticMessage( "SAXException while trying to get smooks instance: " );
			throw new InitialisationException( errorMsg, e );
		}
	}


	private SmooksPayloadProcessor createSmooksPayloadProcessor() throws InitialisationException {
		// determine the ResultType
		ResultType resultType = getResultTypeEnum();

		//	Create the Smooks payload processor
		SmooksPayloadProcessor payloadProcessor;
		if(resultType == ResultType.RESULT) {
			payloadProcessor = new SmooksPayloadProcessor( smooks, resultType, createSourceResultFactory());
		} else {
			payloadProcessor = new SmooksPayloadProcessor( smooks, resultType );
		}

		//	set the JavaResult beanId if specified
		if ( resultType == ResultType.JAVA && javaResultBeanId != null )
		{
			payloadProcessor.setJavaResultBeanId( javaResultBeanId );
        }

		return payloadProcessor;
	}

	/**
	 * Creates a SourceResult Factory
	 *
	 * @return
	 * @throws InitialisationException
	 */
	private SourceResultFactory createSourceResultFactory() throws InitialisationException {

		ResultFactory resultFactory;
		if(!StringUtils.isBlank(resultClass)) {
			try {
				resultFactory = new ClassNameResultFactory(resultClass);
			} catch (ClassNotFoundException e) {
				final Message errorMsg = createStaticMessage( "The class '"+ resultClass +"' definend in the 'resultClass' property can't be found.");
				throw new InitialisationException(errorMsg, e, this);
			} catch (IllegalArgumentException e) {
				final Message errorMsg = createStaticMessage( e.getMessage() );
				throw new InitialisationException(errorMsg, e, this);
			}

		} else if(!StringUtils.isBlank(resultFactoryClass)) {
			try {
				resultFactory = (ResultFactory) ClassUtils.getClass(this.getClass().getClassLoader(), resultFactoryClass).newInstance();
			} catch (ClassNotFoundException e) {
				final Message errorMsg = createStaticMessage( "The class '"+ resultFactoryClass +"' definend in the 'resultFactoryClass' property can't be found.");
				throw new InitialisationException(errorMsg, e, this);
			} catch (InstantiationException e) {
				final Message errorMsg = createStaticMessage( "The class '"+ resultFactoryClass +"' definend in the 'resultFactoryClass' property can't be instantiated.");
				throw new InitialisationException(errorMsg, e, this);
			} catch (IllegalAccessException e) {
				final Message errorMsg = createStaticMessage( "The class '"+ resultFactoryClass +"' definend in the 'resultFactoryClass' property can't be instantiated.");
				throw new InitialisationException(errorMsg, e, this);
			} catch (ClassCastException e) {
				final Message errorMsg = createStaticMessage( "The class '" + resultFactoryClass + "' does not implement the 'org.milyn.smooks.mule.ResultFactory' interface." );
				throw new InitialisationException(errorMsg, e, this);
			}

		} else {
			final Message errorMsg = createStaticMessage( "The resultType is '" + resultType + "' but no 'resultClass' or 'resultFactoryClass' is correctly defined. On of those need to be defined.");

			throw new InitialisationException(errorMsg, this);
		}

		return new GenericSourceResultFactory(resultFactory);
	}

	private ResultType getResultTypeEnum() throws InitialisationException
	{
		ResultType resultType = ResultType.STRING;
		if ( this.resultType != null )
		{
			try
	        {
	            resultType = ResultType.valueOf( this.resultType );
	        }
	        catch ( final IllegalArgumentException e )
	        {
    			final Message errorMsg = createStaticMessage( "Invalid 'resultType' config value '" + resultType + "'.  Valid values are: " + Arrays.asList(ResultType.values() ) );
	            throw new InitialisationException(errorMsg, e, this);
	        }
		}
		return resultType;
	}

	private void addReportingSupport( final ExecutionContext executionContext ) throws TransformerException
	{
		if( reportPath != null )
		{
            try
            {
            	log.info( "Using Smooks Reporting. Will generate report in file [" + reportPath  + "]. Do not use in production evironment as this will have negative impact on performance!");
                executionContext.setEventListener( new HtmlReportGenerator( reportPath ) );
            }
            catch ( final IOException e)
            {
    			final Message errorMsg = createStaticMessage( "Failed to create HtmlReportGenerator instance." );
	            throw new TransformerException( errorMsg, this, e );
            }
        }
	}

}