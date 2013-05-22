/**
 * 
 */
package individual.adam.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

/**
 * @author Joyband
 * 
 */
public class WSUtil {
	public static String NAMESPACE = "http://WebXml.com.cn/";
	public static String URL = "http://fy.webxml.com.cn/webservices/EnglishChinese.asmx?WSDL";
	public static int VERSION = -1;
	public static String RESULT_SUFFIX = "Result";

	public static String callWebService(String methodName,
			Map<String, Object> params) throws Exception {
		try {
			SoapObject obj = executeMethod(methodName, params);			
			if (obj != null) {
				return obj.getProperty(methodName + RESULT_SUFFIX).toString();
			}
			return null;
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public static byte[] callWebServiceByte(String methodName,
			Map<String, Object> params) throws Exception {
		try {
			SoapObject obj = executeMethod(methodName, params);			
			if (obj != null) {
				return obj.getProperty(methodName + RESULT_SUFFIX).toString().getBytes();
			}
			return null;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static SoapObject executeMethod(String methodName,
			Map<String, Object> params) throws Exception {
		try {
			SoapObject request = new SoapObject(WSUtil.NAMESPACE, methodName);
			if (params != null && !params.isEmpty()) {
				Iterator<String> it = params.keySet().iterator();
				String key = null;
				while (it.hasNext()) {
					key = it.next();
					request.addProperty(key, params.get(key).toString());
				}
				it = null;
				key = null;
			}

			if (WSUtil.VERSION == -1) {
				WSUtil.VERSION = SoapEnvelope.VER11;
			}
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					WSUtil.VERSION);
			envelope.bodyOut = request;
			envelope.dotNet = true;

			AndroidHttpTransport transport = new AndroidHttpTransport(
					WSUtil.URL);
			SoapObject retObj;

			transport.call(NAMESPACE + methodName, envelope);
			retObj = (SoapObject) envelope.bodyIn;

			return retObj;

		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public static void writeToFile(String xmlString, String filename){
		try{
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			if (file.canWrite()) {
				FileWriter fileOut = new FileWriter(file);
				fileOut.write(xmlString);
				fileOut.flush();
				fileOut.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
