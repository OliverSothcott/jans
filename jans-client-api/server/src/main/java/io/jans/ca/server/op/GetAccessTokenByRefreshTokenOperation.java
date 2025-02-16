package io.jans.ca.server.op;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.jans.as.client.TokenClient;
import io.jans.as.client.TokenResponse;
import io.jans.as.model.util.Util;
import io.jans.ca.common.Command;
import io.jans.ca.common.ErrorResponseCode;
import io.jans.ca.common.params.GetAccessTokenByRefreshTokenParams;
import io.jans.ca.common.response.GetClientTokenResponse;
import io.jans.ca.common.response.IOpResponse;
import io.jans.ca.server.HttpException;
import io.jans.ca.server.Utils;
import io.jans.ca.server.configuration.model.Rp;
import io.jans.ca.server.service.DiscoveryService;
import io.jans.ca.server.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * @author yuriyz
 */
public class GetAccessTokenByRefreshTokenOperation extends BaseOperation<GetAccessTokenByRefreshTokenParams> {

    private static final Logger LOG = LoggerFactory.getLogger(GetAccessTokenByRefreshTokenOperation.class);
    private DiscoveryService discoveryService;

    public GetAccessTokenByRefreshTokenOperation(Command command, ServiceProvider serviceProvider) {
        super(command, serviceProvider, GetAccessTokenByRefreshTokenParams.class);
        this.discoveryService = serviceProvider.getDiscoveryService();
    }

    @Override
    public IOpResponse execute(GetAccessTokenByRefreshTokenParams params) {
        try {
            validate(params);
            final Rp rp = getRp();
            final TokenClient tokenClient = new TokenClient(discoveryService.getConnectDiscoveryResponse(rp).getTokenEndpoint());
            tokenClient.setExecutor(discoveryService.getHttpService().getClientEngine());
            final TokenResponse tokenResponse = tokenClient.execRefreshToken(scopeAsString(params), params.getRefreshToken(), rp.getClientId(), rp.getClientSecret());
            if (tokenResponse != null) {
                if (Util.allNotBlank(tokenResponse.getAccessToken())) {
                    GetClientTokenResponse response = new GetClientTokenResponse();
                    response.setAccessToken(tokenResponse.getAccessToken());
                    response.setExpiresIn(tokenResponse.getExpiresIn());
                    response.setRefreshToken(tokenResponse.getRefreshToken());
                    response.setScope(Utils.stringToList(tokenResponse.getScope()));

                    return response;
                } else {
                    LOG.error("access_token is blank in response, params: " + params + ", response: " + tokenResponse);
                    LOG.error("Please check AS logs for more details (oxauth.log for CE).");
                }
            } else {
                LOG.error("No response from TokenClient");
            }
        } catch (HttpException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        throw HttpException.internalError();
    }

    private String scopeAsString(GetAccessTokenByRefreshTokenParams params) throws UnsupportedEncodingException {
        Set<String> scope = Sets.newHashSet();

        scope.add("openid");
        if (params.getScope() != null) {
            scope.addAll(params.getScope());
        }
        return Utils.joinAndUrlEncode(scope);
    }

    private void validate(GetAccessTokenByRefreshTokenParams params) {
        if (Strings.isNullOrEmpty(params.getRefreshToken())) {
            throw new HttpException(ErrorResponseCode.BAD_REQUEST_NO_REFRESH_TOKEN);
        }
    }
}
