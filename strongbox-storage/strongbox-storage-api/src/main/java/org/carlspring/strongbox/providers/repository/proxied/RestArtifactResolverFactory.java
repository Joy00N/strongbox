package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Objects;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RestArtifactResolverFactory
{

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;
    
    @Inject
    private ConfigurationManager configurationManager;
    
    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    public RestArtifactResolver newInstance(RemoteRepository repository)
    {
        Objects.requireNonNull(repository);
        
        RemoteRepositoryRetryArtifactDownloadConfiguration configuration = configurationManager.getConfiguration()
                                                                                               .getRemoteRepositoriesConfiguration()
                                                                                               .getRemoteRepositoryRetryArtifactDownloadConfiguration();
        
        String username = repository.getUsername();
        String password = repository.getPassword();
        String url = repository.getUrl();
        
        final HttpAuthenticationFeature authenticationFeature = (username != null && password != null) ? HttpAuthenticationFeature.basic(username, password) : null;
                
        return new RestArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient(), url,
                                        configuration,
                                        authenticationFeature)
                                {
                        
                                    @Override
                                    public boolean isAlive()
                                    {
                                        return remoteRepositoryAlivenessCacheManager.isAlive(repository);
                                    }
                        
                                };
    }

}
