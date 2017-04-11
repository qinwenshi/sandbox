package com.sandbox.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.runtime.models.config.RuntimeConfig;
import com.sandbox.runtime.services.InMemoryActivityStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataServer {

    private Server server;

    @Autowired
    RuntimeConfig config;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    InMemoryActivityStore activityStore;

    public void start() {
        int port = config.getMetadataPort();

        server = new Server(new QueuedThreadPool());
        ServerConnector connector=new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(new ContextHandler("/api/1/activity/") {
            @Override
            public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                mapper.writeValue(response.getWriter(), activityStore.getAll());
            }
        });

        try {
            server.start();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting jetty server");
        }

    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        if (server == null) return false;
        return server.isRunning();
    }

}
