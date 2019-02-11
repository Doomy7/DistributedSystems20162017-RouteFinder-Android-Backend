
public class Directions {

	
	// Euclidean distance between two points
		static double distanceTo(double cLat, double cLon, double sLat, double sLon) {
			double dx = cLat - sLat;
			double dy = cLon - sLon;
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		
}
