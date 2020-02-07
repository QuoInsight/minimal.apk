package com.quoinsight.minimal;

/*
   https://stjarnhimlen.se/comp/ppcomp.html
   https://github.com/sky-map-team/stardroid
   https://github.com/florianmski/SunCalc-Java
   https://github.com/LocusEnergy/solar-calculations
*/

public class celestialEphemeris {

  public double gDaysSince2000 = 0;
  public float[] gObsrvLoc = {5.2960f, 100.2752f, 3f};  // Penang International Airport

  public double gEcl = Math.toRadians(23.439281);  // Obliquity of the Ecliptic
  public double gLocalSiderealTime = 0;
  public double[] gEarthXYZ = {0, 0, 0};

  //////////////////////////////////////////////////////////////////////

  public celestialEphemeris(float[] obsrvLoc) {
    double d = java.time.Instant.now().toEpochMilli()
      - java.time.Instant.parse("2000-01-01T00:00:00.00Z").toEpochMilli();
          d = d / (24*60*60000);
    this.gDaysSince2000 = d;
    this.gObsrvLoc = obsrvLoc;

    this.gLocalSiderealTime = Math.toRadians( (280.16 + 360.9856235*d) + obsrvLoc[1] );
    this.gEarthXYZ = getEarthHeliocentricXYZ(d);
    this.gEcl = Math.toRadians(23.4393 - (3.563E-7)*d);
  }

  //////////////////////////////////////////////////////////////////////

  public static double mod2PI(double r) {
    double f = r / (2.0*Math.PI);
    double r1 = (2.0*Math.PI) * ( (f >= 0.0) ? f-Math.floor(f) : f-Math.ceil(f) );
    return (r1 < 0.0) ? (2.0*Math.PI)+r1 : r1;
  }

  public static double mod360(double d) {
    return (d+360)%360;
  }

  //////////////////////////////////////////////////////////////////////

  // stardroid/units/HeliocentricCoordinates.java | CalculateEquatorialCoordinates
  public static double[] helioToGeocentricXYZ(double ecl, double[] hCoords) {
    double xh=hCoords[0], yh=hCoords[0], zh=hCoords[0];
    double y = yh*Math.cos(ecl) - zh*Math.sin(ecl);
    double z = zh*Math.cos(ecl) + yh*Math.sin(ecl);
    // Geocentric-Rectangular Equatorial Coordinates
    return new double[] {xh, y, z};
  }

  // stardroid/units/RaDec.java | calculateRaDecDist
  public static double[] xyzToRaDec(double x, double y, double z) {
    // Rectangular Coordinates: X,Y,Z
    double rightAscension = mod2PI(Math.atan2(y, x));
    double declination = Math.atan(z / Math.sqrt(x*x + y*y));
    /*
      print("RA: " + rightAscension + ", DEC: " + declination);
      print("RightAscension: " + mod360(Math.toDegrees(rightAscension))/15.0 + " hrs");
      print("Declination: " + Math.toDegrees(declination) + "°");
    */
    // Spherical Coordinates: RA,DEC
    return new double[] {rightAscension, declination};
  }

  //////////////////////////////////////////////////////////////////////

  public static float[] getAzimuthalCoordinates(double[] celestialCoordinates, double localSiderealTime, double obsrvLatitude) {
    double RA=celestialCoordinates[0], Decl=celestialCoordinates[1];

    // get azimuthal coordinates (azimuth and altitude) from HA (Hour Angle): 0==south; 24hr==360°
    double hourAngle = localSiderealTime - RA; // [OK]
    //System.out.println("hourAngle: " + mod360(Math.toDegrees(hourAngle)) + "°");

    // florianmski/suncalc/utils/PositionUtils.java | getAzimuth/getAltitude(hourAngle, obsrvLatitude, c.getDeclination())
    double azimuth = Math.PI + Math.atan2(Math.sin(hourAngle),
      Math.cos(hourAngle)*Math.sin(obsrvLatitude) - Math.tan(Decl)*Math.cos(obsrvLatitude)
    ); azimuth = mod360(Math.toDegrees(azimuth)); 

    double altitude = Math.asin(Math.sin(obsrvLatitude)*Math.sin(Decl)
      + Math.cos(obsrvLatitude)*Math.cos(Decl)*Math.cos(hourAngle)
    ); altitude = Math.toDegrees(altitude);

    return new float[] {(float)azimuth, (float)altitude};
  }

  //////////////////////////////////////////////////////////////////////

  // stardroid/provider/ephemeris/OrbitalElements.java | calculateTrueAnomaly
  public static double calculateTrueAnomaly(double m, double e) {
    // initial approximation of eccentric anomaly
    double EPSILON = 1.0e-6;
    double e0 = m + e*Math.sin(m)*(1.0 + e*Math.cos(m));
    double e1 = e0;
    
    // iterate to improve accuracy
    int counter = 0;  do {
      e1 = e0;  e0 = e1 - (e1 - e*Math.sin(e1) - m) / (1.0 - e*Math.cos(e1));
      if (counter++ > 100) {
        //Log.d(TAG, "Failed to converge! Exiting.");
        //Log.d(TAG, "e1 = " + e1 + ", e0 = " + e0);
        //Log.d(TAG, "diff = " + Math.abs(e0 - e1));
        break;
      }
    } while (Math.abs(e0-e1) > EPSILON);

    // convert eccentric anomaly to true anomaly
    double v = 2.0 * Math.atan(Math.sqrt((1.0+e)/(1.0-e)) * Math.tan(0.5*e0));
    return mod2PI(v);
  }

  public static double getEccentriAnomaly(double meanAnomaly, double eccentricity) {
    // If eccentricity < 0.06rad, the below approximation is sufficiently accurate
    double E0 = meanAnomaly + eccentricity*Math.sin(meanAnomaly) * ( 1.0 + eccentricity * Math.cos(meanAnomaly) );
    if ( eccentricity < 0.06 ) {
      return E0;
    } else {
      // need to go for iteration until Math.abs(E1-E0) <= 0.001
      double E1 = E0;  int counter = 0;  do {
        E0 = E1;  // For each new iteration, replace E0 with E1
        E1 = E0 - ( E0 - eccentricity*Math.sin(E0) - meanAnomaly ) / ( 1.0 - eccentricity*Math.cos(E0) );
        if (counter++ > 100) break;
      } while (Math.abs(E1-E0) > 0.001);
      return E1;
    }
  }

  // stardroid/units/HeliocentricCoordinates.java | HeliocentricCoordinates getInstance
  public static double [] getHeliocentricXYZ(
    double distance, double meanLongitude, double eccentricity, double perihelion, double ascendingNode, double inclination
  ) {
    double anomaly = getEccentriAnomaly(meanLongitude-perihelion, eccentricity);

    double rh = distance * (1 - eccentricity*eccentricity) / (1 + eccentricity*Math.cos(anomaly));
        // rh = = √(x^2 + y^2 + z^2);
    double xh = rh * (
      Math.cos(ascendingNode) * Math.cos(anomaly+perihelion-ascendingNode)
      - Math.sin(ascendingNode) * Math.sin(anomaly+perihelion-ascendingNode) * Math.cos(inclination)
    );
    double yh = rh * (
      Math.sin(ascendingNode) * Math.cos(anomaly+perihelion-ascendingNode)
      + Math.cos(ascendingNode) * Math.sin(anomaly+perihelion-ascendingNode) * Math.cos(inclination)
    );
    double zh = rh * Math.sin(anomaly+perihelion-ascendingNode) * Math.sin(inclination);

    // Heliocentric Rectangular Coordinates: in AU (astronomical unit)
    return new double[] {xh, yh, zh, rh};  // actually only {x,y,z} are needed, r is include in the output for convenience use later
  }

  //////////////////////////////////////////////////////////////////////

  public static double[] getEarthHeliocentricXYZ(double daysSince2000) {
    // stardroid/provider/ephemeris/Planet.java
    // Note that this is the orbital data for Earth, this will determine the Sun position as observed from earth.
    double jc = daysSince2000 / 36525.0;                      // TimeUtil.julianCenturies(date)
    double a = 1.00000261 + 0.00000562*jc;                    // distance
    double e = 0.01671123 - 0.00004392*jc;                    // eccentricity
    double i = Math.toRadians(-0.00001531 - 0.01294668*jc);   // inclination
    double w = Math.toRadians(102.93768193 + 0.32327364*jc);  // perihelion
    double o = 0.0;                                           // ascendingNode
    double l = mod2PI(Math.toRadians(100.46457166 + 35999.37244981*jc));  // meanLongitude
    // stardroid/util/Geometry.java | mod2pi
    return getHeliocentricXYZ(a, l, e, w, o, i);
    // double distance, double meanLongitude, double eccentricity, double perihelion, double ascendingNode, double inclination
  }

  public static double[] getLunarXYZ(double d) {
    // [ephemeris - 天文曆表] Orbital elements of Moon 
    double N = Math.toRadians(125.1228 - 0.0529538083 * d);  // longitude of the ascending node
    double i = Math.toRadians(5.1454);                       // inclination to the ecliptic (plane of the Earth's orbit)
    double w = Math.toRadians(318.0634 + 0.1643573223 * d);  // argument of perihelion
    double M = Math.toRadians(115.3654 + 13.0649929509 * d); // mean anomaly
    double e = 0.054900;                                     // eccentricity (0=circle, 0-1=ellipse, 1=parabola)

    double E = getEccentriAnomaly(M, e);                     // eccentric anomaly
    double xv = Math.cos(E) - e;                             // == r * Math.cos(v)
    double yv = Math.sqrt(1.0 - e*e) * Math.sin(E);          // == r * Math.sin(v) 

    double v = Math.atan2(yv, xv);       // true anomaly (angle between position and perihelion)
    double r = Math.sqrt(xv*xv + yv*yv); // current distance/range from earth in AU/AstronomicalUnits; e.g. 0.98
    double objLongitude = v + w;         // true longitude

    // position in 3-dimensional space; moon==>geocentric (Earth-centered); planets==>heliocentric (Sun-centered)
    double xh = r * ( Math.cos(N) * Math.cos(objLongitude) - Math.sin(N) * Math.sin(objLongitude) * Math.cos(i) );
    double yh = r * ( Math.sin(N) * Math.cos(objLongitude) + Math.cos(N) * Math.sin(objLongitude) * Math.cos(i) );
    double zh = r * ( Math.sin(objLongitude) * Math.sin(i) );

    // ecliptic spherical geocentric coordinates
    // double lonecl = Math.atan2( yh, xh );
    // double latecl = Math.atan2( zh, Math.sqrt(xh*xh+yh*yh) );

    return new double[] {xh,yh,zh};
  }

  public static double[] getVenusHeliocentricXYZ(double d) {
    // [ephemeris - 天文曆表] Orbital elements of Venus 
    double a = 0.723330;                                    // semi-major axis, or mean distance from Sun (unit in AU)
    double e = 0.006773 - (1.302E-9) * d;                   // eccentricity (0=circle, 0-1=ellipse, 1=parabola)
    double i = Math.toRadians(3.3946 + (2.75E-8) * d);      // inclination to the ecliptic (plane of the Earth's orbit)
    double w = Math.toRadians(54.8910 + (1.38374E-5) * d);  // argument of perihelion
    double N = Math.toRadians(76.6799 + (2.46590E-5) * d);  // longitude of the ascending node
    double M = Math.toRadians(48.0052 + 1.6021302244 * d);  // mean anomaly (0 at perihelion; increases uniformly with time)

    double E = getEccentriAnomaly(M, e);                    // eccentric anomaly
    double xv = Math.cos(E) - e;                            // == r * Math.cos(v)
    double yv = Math.sqrt(1.0 - e*e) * Math.sin(E);         // == r * Math.sin(v) 

    double v = Math.atan2(yv, xv);       // true anomaly (angle between position and perihelion)
    double r = Math.sqrt(xv*xv + yv*yv); // current distance/range from earth in AU/AstronomicalUnits; e.g. 0.98
    double objLongitude = v + w;         // true longitude

    // position in 3-dimensional space; moon==>geocentric (Earth-centered); planets==>heliocentric (Sun-centered)
    double xh = r * ( Math.cos(N) * Math.cos(objLongitude) - Math.sin(N) * Math.sin(objLongitude) * Math.cos(i) );
    double yh = r * ( Math.sin(N) * Math.cos(objLongitude) + Math.cos(N) * Math.sin(objLongitude) * Math.cos(i) );
    double zh = r * ( Math.sin(objLongitude) * Math.sin(i) );

    return new double[] {xh, yh, zh};
  }

  //////////////////////////////////////////////////////////////////////

  public float[] getCurrentSolarPosition() {
    // invert hEarthCoords to get Sun in Earth coordinates, not the Earth in Sun coordinates
    double[] hCoords = new double[] {-this.gEarthXYZ[0], -this.gEarthXYZ[1], -this.gEarthXYZ[2]};
    double[] eCoords = helioToGeocentricXYZ(this.gEcl, hCoords);
    
    return getAzimuthalCoordinates(  // "#sun@0°Az✳/0°Alt△"
      xyzToRaDec(eCoords[0], eCoords[1], eCoords[2]),
        this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  public float[] getCurrentLunarPosition() {
    double[] eCoords = getLunarXYZ(this.gDaysSince2000);

    return getAzimuthalCoordinates(  // "#moon@0°Az✳/0°Alt△"
      xyzToRaDec(eCoords[0], eCoords[1], eCoords[2]),
        this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  public float[] getCurrentVenusPosition() {
    double[] hCoords = getVenusHeliocentricXYZ(this.gDaysSince2000);
    hCoords[0]-=this.gEarthXYZ[0]; hCoords[1]-=this.gEarthXYZ[1]; hCoords[2]-=this.gEarthXYZ[2];
    double[] eCoords = helioToGeocentricXYZ(this.gEcl, hCoords);

    return getAzimuthalCoordinates(  // "#venus@0°Az✳/0°Alt△"
      xyzToRaDec(eCoords[0], eCoords[1], eCoords[2]),
        this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  //////////////////////////////////////////////////////////////////////

}
