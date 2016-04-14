package focusandroidserverutils.multidbsutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;


public class JerseyJettyServer {
	
	private static final Logger logger = LogManager.getLogger(JerseyJettyServer.class.getName());
	
	private final int port;
	private final String providerPackages;
	private final String contextPath;
	
	private Server server;
	
	public static final String DEFAULT_CONTEXT_PATH = "/*"; //"/rest/*";
	
	public JerseyJettyServer(final int port, final String providerPackages) {
		this.port = port;
		this.providerPackages = providerPackages;
		this.contextPath = DEFAULT_CONTEXT_PATH;
	}
	
	public JerseyJettyServer(final int port, final String providerPackages, final File propertiesFile)
			throws FileNotFoundException, IOException {
		this.port = port;
		this.providerPackages = providerPackages;
		this.contextPath = DEFAULT_CONTEXT_PATH;
		PropertiesManager.getInstance().loadProperties(propertiesFile);
	}
	
	public JerseyJettyServer(final int port, final String providerPackages, final String contextPath) {
		this.port = port;
		this.providerPackages = providerPackages;
		this.contextPath = contextPath;
	}
	
	public JerseyJettyServer(final int port, final String providerPackages, final String contextPath, final File propertiesFile) 
			throws FileNotFoundException, IOException {
		this.port = port;
		this.providerPackages = providerPackages;
		this.contextPath = contextPath;
		PropertiesManager.getInstance().loadProperties(propertiesFile);
	}
	
	public void start() throws Exception {
		logger.info("Starting Jetty Server bound to port " + port);
		
//		ResourceConfig rc = new ResourceConfig();
//		rc.register(MultiPartFeature.class);
		
//		ServletContainer sc = new ServletContainer(rc);
		
		ServletHolder sh = new ServletHolder(ServletContainer.class);    
	
		sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES, providerPackages);
		

		// For the file upload
		sh.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, "org.glassfish.jersey.media.multipart.MultiPartFeature");

		server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet(sh, contextPath);
        
        context.addFilter(ApiOriginFilter.class, contextPath, EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
        
        server.setStopAtShutdown(true);
        
        try {
       	 server.start();
       	 
       	 Runtime.getRuntime().addShutdownHook(
                    new Thread(new ShutdownSignalHandler(server))      
            );
       }
       catch (Exception e) {
       		logger.error("Failed to start  server", e);
       	
       		throw e;
       }
        
       server.join();
		
       logger.info("Jetty Server is shutdown");
	}
	
	public void stop() throws Exception {
		server.stop();
	}
}

class ShutdownSignalHandler implements Runnable {
    
    private final Server _server;

    public ShutdownSignalHandler(final Server server) {
        this._server = server;
    }

    @Override
	public void run() {
       
        try {
            _server.stop();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
