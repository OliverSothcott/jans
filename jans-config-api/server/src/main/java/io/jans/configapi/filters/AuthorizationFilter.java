/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.configapi.filters;

import io.jans.configapi.core.rest.ProtectedApi;
import io.jans.configapi.security.service.AuthorizationService;
import io.jans.configapi.util.ApiConstants;
import io.jans.configapi.util.DataProcessingUtil;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * @author Mougang T.Gasmyr
 */
@Provider
@ProtectedApi
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Inject
    Logger log;

    @Context
    UriInfo info;

    @Context
    HttpServletRequest request;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    AuthorizationService authorizationService;
    
    @Inject
    DataProcessingUtil dataProcessingUtil;

    @SuppressWarnings({ "all" })
    public void filter(ContainerRequestContext context) {
        log.info("=======================================================================");
        log.info("====== context = " + context + " , info.getAbsolutePath() = " + info.getAbsolutePath()
                + " , info.getRequestUri() = " + info.getRequestUri() + "\n\n");
        log.info("====== info.getBaseUri()=" + info.getBaseUri() + " info.getPath()=" + info.getPath()
                + " info.toString()=" + info.toString());
        log.info("====== request.getContextPath()=" + request.getContextPath() + " request.getRequestURI()="
                + request.getRequestURI() + " request.toString() " + request.toString());
        log.info("======" + context.getMethod() + " " + info.getPath() + " FROM IP " + request.getRemoteAddr());
        log.info("======PERFORMING AUTHORIZATION=========================================");
        String authorizationHeader = context.getHeaderString(HttpHeaders.AUTHORIZATION);
        String issuer = context.getHeaderString(ApiConstants.ISSUER);
        boolean configOauthEnabled = authorizationService.isConfigOauthEnabled();
        log.info("\n\n\n AuthorizationFilter::filter() - authorizationHeader = " + authorizationHeader + " , issuer = "
                + issuer + " , configOauthEnabled = " + configOauthEnabled + "\n\n\n");

        if (!configOauthEnabled) {
            log.info("====== Authorization Granted...====== ");
            return;
        }

        log.info("\n\n\n AuthorizationFilter::filter() - Config Api OAuth Valdation Enabled");
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(context);
            log.info("======ONLY TOKEN BASED AUTHORIZATION IS SUPPORTED======================");
            return;
        }
        try {
            authorizationHeader = this.authorizationService.processAuthorization(authorizationHeader, issuer,
                    resourceInfo, context.getMethod(), request.getRequestURI());

            if (authorizationHeader != null && authorizationHeader.trim().length() > 0) {
                context.getHeaders().remove(HttpHeaders.AUTHORIZATION);
                context.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
            
            //encode data
            log.error("======AuthorizationFilter - resourceInfo:{}, resourceInfo.getResourceMethod():{} ",resourceInfo, resourceInfo.getResourceMethod());
            log.error("======AuthorizationFilter - resourceInfo.getResourceMethod().getParameterCount():{}, resourceInfo.getResourceMethod().getParameters():{},  resourceInfo.getResourceMethod().getParameterTypes():{}", resourceInfo.getResourceMethod().getParameterCount(), resourceInfo.getResourceMethod().getParameters(), resourceInfo.getResourceMethod().getParameterTypes());
            processData();
            
            
            log.info("======AUTHORIZATION  GRANTED===========================================");
        } catch (Exception ex) {
            log.error("======AUTHORIZATION  FAILED ===========================================", ex);
            abortWithUnauthorized(context);
        }

    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null
                && authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME).build());
    }

    private void processData() {
        log.error("AuthorizationFilter Processing  Data -  request.getAttributeNames():{} , request.getParameterNames():{} ", request.getAttributeNames(), request.getParameterNames());
        for (Enumeration en =  request.getAttributeNames(); en.hasMoreElements(); ) {
            String name = (String)en.nextElement();
            log.error(" name:{} ",name);
            String values[] = request.getParameterValues(name);
            log.error(" values:{} ",values);
            if(values!=null && values.length>0) {
            int n = values.length;
                for(int i=0; i < n; i++) {
                 //values[i] = values[i].replaceAll("[^\\dA-Za-z ]","").replaceAll("\\s+","+").trim();   
                    dataProcessingUtil.encodeObjDataType(null);
                }
            }
        }
    }
}
