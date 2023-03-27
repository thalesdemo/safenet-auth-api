/**
 * Copyright 2023 safenet-auth-api
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
 *
 * CustomTokenValidatorWrapperImpl extends the TokenValidatorWrapperImpl class
 * to override the buildingRequestXmlFromDto method, to change the 
 * agentId 14 (Shibboleth) to agentId 8 (Authentication SDK) in  the request XML 
 * of the TokenValidatorRequestDTO object.
 * 
 * @see TokenValidatorWrapperImpl
 * @see TokenValidatorRequestDTO
 * 
 * @author Cina Shaykhian
 * @contact hello@onewelco.me
 */
package com.thalesdemo.safenet.auth.api;

import com.safenet.keycloak.agent.tokenvalidatoradapter.dto.tv.TokenValidatorRequestDTO;
import com.safenet.keycloak.agent.tokenvalidatoradapter.tvadapter.TokenValidatorWrapperImpl;


public class CustomTokenValidatorWrapperImpl extends TokenValidatorWrapperImpl {

    /**
     * The agentId 8 (Authentication SDK) is used for the authentication logs.
     */

    private static final String AGENT_ID_AUTH_SDK = "8";


    /**
     * CustomTokenValidatorWrapperImpl constructor with primary and failover auth server base URLs and key file path.
     * @param primaryAuthServerUrl the primary auth server URL
     * @param failoverAuthServerUrl the secondary auth server URL
     * @param key the encryption key in base64 format
     */

    public CustomTokenValidatorWrapperImpl(String primaryAuthServerUrl, String failoverAuthServerUrl, String key) {
        super(primaryAuthServerUrl, failoverAuthServerUrl, key);
    }


    /**
     * Override package agentId 14 (Shibboleth) to agentId 8 (Authentication SDK) in the request XML of 
     * the TokenValidatorRequestDTO object under the TokenValidatorWrapperImpl class, in the XML tag <agentid>.
     * @param tvAuthDto the TokenValidatorRequestDTO 
     */

    @Override
    protected String buildingRequestXmlFromDto(TokenValidatorRequestDTO tvAuthDto) {
        String xml = super.buildingRequestXmlFromDto(tvAuthDto);
        return xml.replaceAll("<agentid>\\d+</agentid>", "<agentid>" + AGENT_ID_AUTH_SDK + "</agentid>");
    }

}

