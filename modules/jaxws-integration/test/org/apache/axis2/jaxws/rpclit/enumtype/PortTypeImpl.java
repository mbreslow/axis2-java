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
package org.apache.axis2.jaxws.rpclit.enumtype;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType;
import org.apache.axis2.jaxws.TestLogger;
import org.test.rpclit.schema.ElementString;

@WebService(serviceName="RPCLitEnumService",
		endpointInterface="org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType")
public class PortTypeImpl implements PortType {

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType#echoString(javax.xml.ws.Holder)
     */
    public void echoString(Holder<ElementString> pString) {
       ElementString es = pString.value;
       if(es == es.A){
           TestLogger.logger.debug("Enum A");
       }
        TestLogger.logger.debug("resetting Enum to B");
       es = es.B;
       pString.value = es;
    }

}