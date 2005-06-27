/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis.receivers;

import java.lang.reflect.Method;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.DependencyManager;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.impl.llom.soap11.SOAP11Constants;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

/**
 * This is a Simple java Provider.
 */
public class RawXMLINOutMessageRecevier
        extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {
    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Field scope
     */
    private String scope;

    /**
     * Field method
     */
    private Method method;

    /**
     * Field classLoader
     */
    private ClassLoader classLoader;

    /**
     * Constructor RawXMLProvider
     */
    public RawXMLINOutMessageRecevier() {
        scope = Constants.APPLICATION_SCOPE;
    }

    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newmsgContext)
            throws AxisFault {
        try {

            SOAPFactory fac = null;
            String nsURI = msgContext.getEnvelope().getNamespace().getName();
            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
                fac = OMAbstractFactory.getSOAP12Factory();
            } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
                fac = OMAbstractFactory.getSOAP11Factory();
            }else {
                throw new AxisFault("Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
            }


            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class ImplClass = obj.getClass();
            DependencyManager.configureBusinussLogicProvider(obj, msgContext);

            OperationDescription op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }
            Class[] parameters = method.getParameterTypes();
            if ((parameters != null)
                    && (parameters.length == 1)
                    && OMElement.class.getName().equals(parameters[0].getName())) {
                OMElement methodElement = msgContext.getEnvelope().getBody().getFirstElement();

                OMElement parmeter = null;
                SOAPEnvelope envelope = null;

                String style = msgContext.getOperationContext().getAxisOperation().getStyle();

                if (WSDLService.STYLE_DOC.equals(style)) {
                    parmeter = methodElement;
                    Object[] parms = new Object[]{parmeter};

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);
                    envelope = fac.getDefaultEnvelope();
                    envelope.getBody().setFirstChild(result);

                } else if (WSDLService.STYLE_RPC.equals(style)) {
                    parmeter = methodElement.getFirstElement();
                    Object[] parms = new Object[]{parmeter};

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);
                    envelope = fac.getDefaultEnvelope();

                    OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
                    OMElement responseMethodName = fac.createOMElement(methodName + "Response", ns);
                    responseMethodName.addChild(result);
                    envelope.getBody().addChild(responseMethodName);
                } else {
                    throw new AxisFault("Unknown style ");
                }
                newmsgContext.setEnvelope(envelope);
            } else if ((parameters != null) && (parameters.length == 0)) {

                SOAPEnvelope envelope = fac.getDefaultEnvelope();
                String style = msgContext.getOperationContext().getAxisOperation().getStyle();

                if (WSDLService.STYLE_DOC.equals(style)) {

                    Object[] parms = new Object[0];

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);
                    envelope.getBody().setFirstChild(result);

                } else if (WSDLService.STYLE_RPC.equals(style)) {

                    Object[] parms = new Object[0];

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);

                    OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
                    OMElement responseMethodName = fac.createOMElement(methodName + "Response", ns);
                    responseMethodName.addChild(result);
                    envelope.getBody().addChild(responseMethodName);
                } else {
                    throw new AxisFault("Unknown style ");
                }
                newmsgContext.setEnvelope(envelope);
            } else {
                throw new AxisFault("Raw Xml provider supports only the methods bearing the signature public OMElement "
                        + "&lt;method-name&gt;(OMElement) where the method name is anything");
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }
}
