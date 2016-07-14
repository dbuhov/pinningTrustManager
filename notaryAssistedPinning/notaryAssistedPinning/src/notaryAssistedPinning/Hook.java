package notaryAssistedPinning;


import java.lang.reflect.Method;
import java.security.SecureRandom;


import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


import android.content.Context;
import android.util.Log;
import com.saurik.substrate.*;


public class Hook {
	public static String Log_Tag = "PinningTrustManager";
	public static Context context;
	
	//hook for the Context of the application
	protected static void hookGetSystemContext(Class<?> _clazz) {
        Method method;
        try {
                Class<?>[] params = new Class[1];
                params[0] = String.class;

                method = _clazz.getMethod("getSystemService", params);
        } catch (NoSuchMethodException e) {
                method = null;
                Log.d("ERROR", Log.getStackTraceString(e));
        }

        if (method != null) {

                MS.hookMethod(_clazz, method,
                                new MS.MethodAlteration<Context, Object>() {

                                        public Object invoked(final Context hooked,
                                                        final Object... args) throws Throwable {
                                                context = hooked.getApplicationContext();
                                                return invoke(hooked, args);
                                        }
                                });
        }
	}
	
	
	public static void initialize() {
		
		 Log.i(Log_Tag, "Substrate Initialized.");
		
		 MS.hookClassLoad("android.app.ContextImpl", new MS.ClassLoadHook() {
             public void classLoaded(Class<?> _clazz) {
                     hookGetSystemContext(_clazz);
             }
     });
		
	 	//hooks accessed URLs
		 MS.hookClassLoad("java.net.URLConnection", new MS.ClassLoadHook() {
     	  @SuppressWarnings({ "unchecked", "rawtypes" })
           public void classLoaded(Class<?> _class) {
           	Method method;
           	final String methodName = "getURL";
           	Log.i(Log_Tag, "Class Loaded: " + _class.getName());
               try{
               	method = _class.getMethod(methodName);
               }catch(NoSuchMethodException e){ 
               	method = null;
               	Log.i(Log_Tag, "No such method: " + methodName);
               }
               
                            
           	if (method != null) {
           		Log.i(Log_Tag, "Method Hooked." + methodName);
           		MS.hookMethod(_class, method, new MS.MethodAlteration() {
             		public Object invoked(Object _class, Object... args) throws Throwable
	               		{
	               			Object originalRetValue =  invoke(_class, args);
	               			Log.i(Log_Tag, "Original Return Value: " + originalRetValue);
	              				return originalRetValue;
	               		}
					});// End MS.hookMethod
           	}
             
           }
       }); // End MS.hookClassLoad
		 
	   //This hook lists all installed applications		 
		 MS.hookClassLoad("android.content.pm.PackageManager", new MS.ClassLoadHook() {
	     	  @SuppressWarnings({ "unchecked", "rawtypes" })
	           public void classLoaded(Class<?> _class) {
	           	Method method;
	           	final String methodName = "getInstalledPackages";
	           	Log.i(Log_Tag, "Class Loaded: " + _class.getName());
	               try{
	               	method = _class.getMethod(methodName, Integer.class);
	               }catch(NoSuchMethodException e){ 
	               	method = null;
	               	Log.i(Log_Tag, "No such method: " + methodName);
	               }
	               
	                            
	           	if (method != null) {
	           		Log.i(Log_Tag, "Method Hooked." + methodName);
	           		MS.hookMethod(_class, method, new MS.MethodAlteration() {
	             		public Object invoked(Object _class, Object... args) throws Throwable
		               		{
		               			Object originalRetValue =  invoke(_class, args);
		               			Log.i(Log_Tag, "Packages: " + originalRetValue);
		              				return originalRetValue;
		               		}
						});// End MS.hookMethod
	           	}
	             
	           }
	       }); // End MS.hookClassLoad
		 

		// This hook overrides the getTrustManagers method from javax.net.ssl.TrustManagerFactory
		 MS.hookClassLoad("javax.net.ssl.TrustManagerFactory", new MS.ClassLoadHook() {
	     	  @SuppressWarnings({ "unchecked", "rawtypes" })
	           public void classLoaded(Class<?> _class) {
	           	Method method;
	           	final String methodName = "getTrustManagers";
	           	Log.i(Log_Tag, "Class Loaded: " + _class.getName());
	               try{
	               	method = _class.getMethod(methodName);
	               }catch(NoSuchMethodException e){ 
	               	method = null;
	               	Log.i(Log_Tag, "No such method: " + methodName);
	               }
	               
	                            
	           	if (method != null) {
	           		Log.i(Log_Tag, "Method Hooked." + methodName);
	           		MS.hookMethod(_class, method, new MS.MethodAlteration() {
	             		public Object invoked(Object _class, Object... args) throws Throwable
		               		{
	             			Log.i(Log_Tag, methodName + "() override");
							return PinTrustManager.getPinTrustManager();
		               		}
						});// End MS.hookMethod
	           	}
	             
	           }
	       }); // End MS.hookClassLoad
		 
		 
		// This hook overrides the setSSLSocketFactory method from javax.net.ssl.HttpsURLConnection
		 MS.hookClassLoad("javax.net.ssl.HttpsURLConnection", new MS.ClassLoadHook() {
	     	  @SuppressWarnings({ "unchecked", "rawtypes" })
	           public void classLoaded(Class<?> _class) {
	           	Method method;
	           	final String methodName = "setSSLSocketFactory";
	           	Log.i(Log_Tag, "Class Loaded: " + _class.getName());
	               try{
	               	method = _class.getMethod(methodName);
	               }catch(NoSuchMethodException e){ 
	               	method = null;
	               	Log.i(Log_Tag, "No such method: " + methodName);
	               }
	               
	                            
	           	if (method != null) {
	           		Log.i(Log_Tag, "Method Hooked." + methodName);
	           		MS.hookMethod(_class, method, new MS.MethodAlteration() {
	             		public Object invoked(Object _class, Object... args) throws Throwable
		               		{
	             			Log.i(Log_Tag, methodName + "() override");
							SSLContext context = SSLContext.getInstance("TLS");
							context.init(null, PinTrustManager.getPinTrustManager(), null);
							return invoke(_class, context.getSocketFactory());
		               		}
						});// End MS.hookMethod
	           	}
	             
	           }
	       }); // End MS.hookClassLoad
		 
	
		
		// This hook overrides the "init" method from javax.net.ssl.SSLContext
		 MS.hookClassLoad("javax.net.ssl.SSLContext", new MS.ClassLoadHook() {
	     	  @SuppressWarnings({ "unchecked", "rawtypes" })
	           public void classLoaded(Class<?> _class) {
	           	Method method;
	           	final String methodName = "init";
	           	Log.i(Log_Tag, "Class Loaded: " + _class.getName());
	           	try {
					method = _class.getMethod(methodName, 
							KeyManager[].class, TrustManager[].class, SecureRandom.class);
				} catch (NoSuchMethodException e) {
					Log.w(Log_Tag, "No such method: " + methodName);
					method = null;
				}
	               
	                            
	           	if (method != null) {
	           		Log.i(Log_Tag, "Method Hooked." + methodName);
	           		MS.hookMethod(_class, method, new MS.MethodAlteration() {
	             		public Object invoked(Object _class, Object... args) throws Throwable
		               		{
	             			Log.i(Log_Tag, methodName + "() override in " +	"javax.net.ssl.SSLContext");
							return invoke(_class, null, PinTrustManager.getPinTrustManager(), null);
		               		}
						});// End MS.hookMethod
	           	}
	             
	           }
	       }); // End MS.hookClassLoad
		 

	}
}