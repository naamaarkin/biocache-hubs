/* *************************************************************************
 *  Copyright (C) 2011 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.hubs.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import scala.collection.immutable.Nil;

/**
 * Implementation of BieService.java that calls the bie-webapp application
 * via JSON REST web services.
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("bieRestService")
public class BieRestService implements BieService {
    @Inject
    private RestOperations restTemplate; // NB MappingJacksonHttpMessageConverter() injected by Spring
    /** URI prefix for bie-service - should be overridden in properties file */
    protected String bieUriPrefix = "http://localhost:9999/bie-webapp";

    private final static Logger logger = Logger.getLogger(BieRestService.class);
    
    /**
     * @see org.ala.hubs.service.BieService#getGuidForName(java.lang.String) 
     * 
     * @param name
     * @return guid
     */
    @Override
    public String getGuidForName(String name) {
        String guid = null;
        
        try {
            final String jsonUri = bieUriPrefix + "/ws/guid/" + name;            
            logger.info("Requesting: " + jsonUri);
            List<Object> jsonList = restTemplate.getForObject(jsonUri, List.class);
            
            if (!jsonList.isEmpty()) {
                Map<String, String> jsonMap = (Map<String, String>) jsonList.get(0);
                
                if (jsonMap.containsKey("identifier")) {
                    guid = jsonMap.get("identifier");
                }
            }
            
        } catch (Exception ex) {
            logger.error("RestTemplate error: " + ex.getMessage(), ex);
            //searchResults.setStatus("Error: " + ex.getMessage());
        }
        
        return guid;
    }

    /**
     * @see org.ala.hubs.service.BieService#getNamesForGuids(java.util.List)
     *
     * @param guids
     * @return
     */
    @Override
    public List<String> getNamesForGuids(List<String> guids) {
        List<String> names = null;

        try {
            final String jsonUri = bieUriPrefix + "/species/namesFromGuids.json";
            String params = "?guid=" + StringUtils.join(guids, "&guid=");
            names = restTemplate.postForObject(jsonUri + params, null, List.class);
        } catch (Exception ex) {
            logger.error("Requested URI: " + bieUriPrefix + "/species/namesFromGuids.json");
            logger.error("With POST body: guid=" + StringUtils.join(guids, "&guid="));
            logger.error("RestTemplate error: " + ex.getMessage(), ex);
        }
        
        return names;
    }
    
    public String getBieUriPrefix() {
        return bieUriPrefix;
    }

    public void setBieUriPrefix(String bieUriPrefix) {
        this.bieUriPrefix = bieUriPrefix;
    }

}