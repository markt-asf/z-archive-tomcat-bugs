package org.apache.tomcat.user014;

import javax.security.auth.callback.CallbackHandler;

import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.config.AuthConfigProvider;
import jakarta.security.auth.message.config.ClientAuthConfig;
import jakarta.security.auth.message.config.ServerAuthConfig;

public class MyAuthConfigProvider implements AuthConfigProvider {

    @Override
    public ClientAuthConfig getClientAuthConfig(String layer, String appContext,
            CallbackHandler handler) throws AuthException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerAuthConfig getServerAuthConfig(String layer, String appContext,
            CallbackHandler handler) throws AuthException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException();
    }
}
