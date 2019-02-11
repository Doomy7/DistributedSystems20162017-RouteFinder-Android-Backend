import java.net.*;
import java.util.*;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class will get the lat long values.
 */
public class RoutesGoogleAPI{
	
	/**
	 * pros to paron main
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws Exception
	 */
	static List<List<HashMap<String, String>>> getRoutes(double[] coor) throws IOException, ParseException{
	  
	  String jsonDataString = downloadUrl(getDirectionsUrl(coor));
	  
	  List<List<HashMap<String, String>>> finalRoute = parseJsonData(jsonDataString);
	  
	  return finalRoute;
  }

	/**
	 * find downloadurl
	 * @param strUrl
	 * @return
	 * @throws IOException
	 */
	private static String downloadUrl(String strUrl) throws IOException {
	    String data = "";
	    InputStream iStream = null;
	    HttpURLConnection urlConnection = null;
	    try {
	        URL url = new URL(strUrl);
	
	        // Creating an http connection to communicate with url
	        urlConnection = (HttpURLConnection) url.openConnection();
	
	        // Connecting to url
	        urlConnection.connect();
	
	        // Reading data from url
	        iStream = urlConnection.getInputStream();
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
	
	        StringBuffer sb = new StringBuffer();
	
	        String line = "";
	        while ((line = br.readLine()) != null) {
	            sb.append(line);
	        }
	
	        data = sb.toString();
	
	        br.close();
	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	        iStream.close();
	        urlConnection.disconnect();
	    }
	    return data;
	}
  
	/**
	 * get direction url
	 * @param originlat
	 * @param originlon
	 * @param destlat
	 * @param destlon
	 * @return
	 */
	private static String getDirectionsUrl(double[] coor) {
	  
	    // Origin of route
	    String str_origin = "origin=" + coor[0] + "," + coor[1];

	    // Destination of route
	    String str_dest = "destination=" + coor[2] + "," + coor[3];

	    // Sensor enabled
	    String sensor = "sensor=false";

	    // Building the parameters to the web service
	    String parameters = str_origin + "&" + str_dest + "&" + sensor;

	    // Output format
	    String output = "json";

	    // Building the url to the web service
	    String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
	    return url;
	}
  
	/**
	 * build list of route nodes
	 * @param jStringObject
	 * @return
	 * @throws ParseException
	 */
	public static List<List<HashMap<String,String>>> parseJsonData(String jStringObject) throws ParseException{
	  
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;
		JSONParser parser = new JSONParser();
		JSONObject jsonDataclear = (JSONObject) parser.parse(jStringObject);
		try {	
			jRoutes = (JSONArray) jsonDataclear.get("routes");
			// Traversing all routes 
			for(int i=0;i<jRoutes.size();i++){
				jLegs = (JSONArray) ( (JSONObject)jRoutes.get(i)).get("legs");
				List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
				
				// Traversing all legs 
				for(int j=0;j<jLegs.size();j++){
					jSteps = (JSONArray) ((JSONObject)jLegs.get(j)).get("steps");
					
					// Traversing all steps 
					for(int k=0;k<jSteps.size();k++){
						String polyline = "";
						polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
						List<LatLng> list = decodePoly(polyline);
						
						// Traversing all points 
						for(int l=0;l<list.size();l++){
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
							hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
							path.add(hm);
						}
					}
					routes.add(path);
	            }
	        }

	    } catch (Exception e){
	    	
	    }
		
		return routes;
	}	
	    
	/**
	 * json parser for poly
	 * @param encoded
	 * @return
	 */
	private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(((double)lat / 1E5), ((double)lng / 1E5));
            poly.add(p);
        }
        return poly;
    }
}

