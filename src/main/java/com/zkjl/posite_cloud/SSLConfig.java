package com.zkjl.posite_cloud;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yindawei
 * @date 2018/10/11 13:46
 **/
/*@Configuration*/
public class SSLConfig {

    @Value(value = "${http.port}")
    Integer httpPort;
    @Value(value = "${server.port}")
    Integer serverPort;
    @Bean
    public TomcatServletWebServerFactory servletContainer() { //springboot2 新变化

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(Context context) {

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/api/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
        return tomcat;
    }

    private Connector initiateHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(serverPort);
        return connector;
    }

    /**
     * 创建wss协议接口
     * @return
     */
    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        System.out.println("TOMCATCONTEXTCUSTOMIZER INITILIZED");
        return new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                context.addServletContainerInitializer(new WsSci(), null);
            }

        };
    }
}