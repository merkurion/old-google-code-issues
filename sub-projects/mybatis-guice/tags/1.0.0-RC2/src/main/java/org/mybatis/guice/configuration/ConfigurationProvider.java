/*
 *    Copyright 2010 The myBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.guice.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Provides the myBatis Configuration.
 *
 * @version $Id$
 */
@Singleton
public final class ConfigurationProvider implements Provider<Configuration> {

    /**
     * The myBatis Configuration reference.
     */
    private final Configuration configuration;

    private Map<String, Class<?>> typeAliases = Collections.emptyMap();

    private Map<Class<?>, TypeHandler> typeHandlers = Collections.emptyMap();

    private Set<Class<?>> mapperClasses = Collections.emptySet();

    private Set<Interceptor> plugins = Collections.emptySet();

    /**
     * Creates a new myBatis Configuration from the Environment.
     *
     * @param environment the needed myBatis Environment.
     */
    @Inject
    public ConfigurationProvider(final Environment environment) {
        this.configuration = new Configuration(environment);
    }

    /**
     * Called after injection is done. Listener is set up in {@link org.mybatis.guice.MyBatisModule#configure()}.
     * Initialization needs to be done in a defined order plus ErrorContext instance must be reset at the end
     * to prevent memory leaks.
     */
    protected void postCreate() {
        try {
            for (Entry<String, Class<?>> entry : this.typeAliases.entrySet()) {
                this.configuration.getTypeAliasRegistry().registerAlias(entry.getKey(), entry.getValue());
            }

            for (Entry<Class<?>, TypeHandler> entry : this.typeHandlers.entrySet()) {
                this.configuration.getTypeHandlerRegistry().register(entry.getKey(), entry.getValue());
            }

            for (Class<?> mapperClass : this.mapperClasses) {
                this.configuration.addMapper(mapperClass);
            }

            for (Interceptor interceptor : this.plugins) {
                this.configuration.addInterceptor(interceptor);
            }
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Inject(optional = true)
    public void setLazyLoadingEnabled(@Named("mybatis.configuration.lazyLoadingEnabled") boolean lazyLoadingEnabled) {
        this.configuration.setLazyLoadingEnabled(lazyLoadingEnabled);
    }

    /**
     * Adds the user defined type aliases to the myBatis Configuration.
     *
     * @param typeAliases the user defined type aliases.
     */
    @Inject(optional = true)
    public void registerAlias(@TypeAliases final Map<String,Class<?>> typeAliases) {
        this.typeAliases = typeAliases;
    }

    /**
     * Adds the user defined type handlers to the myBatis Configuration.
     *
     * @param typeHandlers the user defined type handlers.
     */
    @Inject(optional = true)
    public void registerTypeHandlers(final Map<Class<?>, TypeHandler> typeHandlers) {
        this.typeHandlers = typeHandlers;
    }

    /**
     * Adds the user defined Mapper classes to the myBatis Configuration.
     *
     * @param mapperClasses the user defined Mapper classes.
     */
    @Inject(optional = true)
    public void registerMappers(@Mappers final Set<Class<?>> mapperClasses) {
        this.mapperClasses = mapperClasses;
    }

    /**
     * Adds the user defined ObjectFactory to the myBatis Configuration.
     *
     * @param objectFactory
     */
    @Inject(optional = true)
    public void setObjectFactory(final ObjectFactory objectFactory) {
        this.configuration.setObjectFactory(objectFactory);
    }

    /**
     * Registers the user defined plugins interceptors to the
     * myBatis Configuration.
     *
     * @param plugins the user defined plugins interceptors.
     */
    @Inject(optional = true)
    public void registerPlugins(final Set<Interceptor> plugins) {
        this.plugins = plugins;
    }

    /**
     * {@inheritDoc}
     */
    public Configuration get() {
        return this.configuration;
    }

}