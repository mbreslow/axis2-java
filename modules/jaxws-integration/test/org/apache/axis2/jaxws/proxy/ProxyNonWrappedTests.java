/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.proxy;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.sei.DocLitnonWrappedProxy;
import org.apache.axis2.jaxws.proxy.doclitnonwrapped.sei.ProxyDocLitUnwrappedService;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;
import org.test.proxy.doclitnonwrapped.Invoke;
import org.test.proxy.doclitnonwrapped.ObjectFactory;
import org.test.proxy.doclitnonwrapped.ReturnType;

/**
 * This test cases will use proxy NON wrapped wsdl to invoke methods
 * on a deployed Server Endpoint.
 */
public class ProxyNonWrappedTests extends TestCase {

    QName serviceName = new QName("http://doclitnonwrapped.proxy.test.org", "ProxyDocLitUnwrappedService");
    private String axisEndpoint = "http://localhost:6060/axis2/services/ProxyDocLitUnwrappedService.DocLitnonWrappedImplPort";
    private QName portName = new QName("http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedPort");
    private String wsdlLocation = System.getProperty("basedir",".")+"/"+"test-resources/wsdl/ProxyDocLitnonWrapped.wsdl";

    /**
     * @param arg0
     */
    public ProxyNonWrappedTests(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }
    

    public ProxyNonWrappedTests() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    static {
        BasicConfigurator.configure();
    }

    public void setUp() {
    	TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
    	StartServer startServer = new StartServer("server1");
    	startServer.testStartServer();
    }
    
    public void tearDown() {
    	TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
    	StopServer stopServer = new StopServer("server1");
    	stopServer.testStopServer();
    }
    
    public void testInvoke(){
        TestLogger.logger.debug("-----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        TestLogger.logger.debug(">>Testing Sync Inovoke on Proxy DocLit non-wrapped");
        ObjectFactory factory = new ObjectFactory();
        Invoke invokeObj = factory.createInvoke();
        invokeObj.setInvokeStr("test request for twoWay Operation");
        Service service = Service.create(null, serviceName);
        assertNotNull(service);
        DocLitnonWrappedProxy proxy = service.getPort(portName, DocLitnonWrappedProxy.class);
        assertNotNull(proxy);
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        ReturnType response = proxy.invoke(invokeObj);
        assertNotNull(response);
        TestLogger.logger.debug(">>Response =" + response.getReturnStr());

        TestLogger.logger.debug("-------------------------------------");
    }
    
    public void testNullInvoke(){
        TestLogger.logger.debug("-----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        TestLogger.logger.debug(">>Testing Sync Invoke on Proxy DocLit bare with a null parameter");
        ObjectFactory factory = new ObjectFactory();
        Invoke invokeObj = null;
        Service service = Service.create(null, serviceName);
        assertNotNull(service);
        DocLitnonWrappedProxy proxy = service.getPort(portName, DocLitnonWrappedProxy.class);
        assertNotNull(proxy);
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        ReturnType response = proxy.invoke(invokeObj);
        assertNull(response);

        TestLogger.logger.debug("-------------------------------------");
    }
    
    public void testInvokeAsyncCallback(){
        try{
            TestLogger.logger.debug("---------------------------------------");
            TestLogger.logger.debug("DocLitNonWrapped test case: " + getName());
            //Create wsdl url
            File wsdl= new File(wsdlLocation); 
            URL wsdlUrl = wsdl.toURL(); 
            ObjectFactory factory = new ObjectFactory();
            //create input object to web service operation
            Invoke invokeObj = factory.createInvoke();
            invokeObj.setInvokeStr("test request for twoWay Async Operation");
            //Create Service
            ProxyDocLitUnwrappedService service = new ProxyDocLitUnwrappedService(wsdlUrl, serviceName);
            //Create proxy
            DocLitnonWrappedProxy proxy = service.getProxyDocLitnonWrappedPort();
            TestLogger.logger.debug(">>Invoking Binding Provider property");
            //Setup Endpoint url -- optional.
            BindingProvider p = (BindingProvider)proxy;
                p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
            TestLogger.logger.debug(">> Invoking Proxy Asynchronous Callback");
            AsyncHandler<ReturnType> handler = new AsyncCallback();
            //Invoke operation Asynchronously.
            Future<?> monitor = proxy.invokeAsync(invokeObj, handler);
            while(!monitor.isDone()){
                Thread.sleep(1000);
            }
            TestLogger.logger.debug("---------------------------------------");
        }catch(Exception e){ 
            e.printStackTrace(); 
            fail("Exception received" + e);
        }
    }
    
    public void testInvokeAsyncPolling(){
        
    }

}

