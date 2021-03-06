package com.sandbox.runtime.js.models;

import com.sandbox.runtime.models.ServiceScriptException;
import com.sandbox.runtime.models.EngineRequest;
import com.sandbox.runtime.models.Route;
import com.sandbox.runtime.models.ScriptSource;
import com.sandbox.runtime.models.http.HTTPRoute;
import com.sandbox.runtime.models.jms.JMSRoute;
import jdk.nashorn.internal.objects.NativeError;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by nickhoughton on 25/09/2014.
 */
public class SandboxScriptObject implements ISandboxScriptObject{
    private HashMap<Route, ISandboxDefineCallback> routes = new HashMap<>();
    ScriptObject config;
    Route currentRoute;

    public void define(String transport, String defineType, String path, String method, ScriptObject properties, ScriptFunction callback, ISandboxDefineCallback func, NativeError error) throws ServiceScriptException {
        Map<String, String> propertiesMap = new HashMap<>();
        properties.propertyIterator().forEachRemaining(k -> {
            Object value = properties.get(k);
            if(value instanceof String) propertiesMap.put(k, (String) value);
            return;
        });

        Route routeDetails = null;
        if(transport.equals("http")){
            routeDetails = new HTTPRoute(method, path, propertiesMap);
        }else if(transport.equals("jms")){
            routeDetails = new JMSRoute(path, propertiesMap);
        }

        routeDetails.setTransport(transport);
        routeDetails.setFunctionSource(new ScriptSource(callback));
        routeDetails.setDefineSource(new ScriptSource(error, "<sandbox-internal>"));
        routeDetails.setDefineType(defineType);

        //set property for extension classes
        currentRoute = routeDetails;

        routes.put(routeDetails, func);
    }

    public List<Route> getRoutes() { return new ArrayList<>(routes.keySet()); }

    public void setConfig(ScriptObject config) {
        this.config = config;
    }

    public ScriptObject getConfig() {
        return config;
    }

    public ISandboxDefineCallback getMatchedFunction(EngineRequest req) {
        Optional<Route> matchedRoute = routes.keySet().stream().filter(r -> r.isUncompiledMatch(req)).findFirst();
        return matchedRoute.isPresent() ? routes.get(matchedRoute.get()) : null;
    }

    public void seedState(String func) { }
}
