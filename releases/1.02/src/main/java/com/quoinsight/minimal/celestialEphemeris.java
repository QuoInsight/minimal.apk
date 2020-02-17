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
    // !! JD0.0 is 01-JAN-4713BC at Noon !!
    // !! astronomical day starting at 12:00 noon vs civil day starting at 00:00 midnight !!
    double d = java.time.Instant.now().toEpochMilli()
      - java.time.Instant.parse("2000-01-01T12:00:00.00Z").toEpochMilli();
          d = d / (24*60*60000);
    this.gDaysSince2000 = d;

    this.gObsrvLoc = obsrvLoc;

    this.gEcl = Math.toRadians(23.439281); // stardroid/units/HeliocentricCoordinates.java | OBLIQUITY
    // this.gEcl = Math.toRadians(23.4397); // florianmski/suncalc/utils/Constants.java | EARTH_OBLIQUITY
    // this.gEcl = Math.toRadians(23.4393 - (3.563E-7)*d); // stjarnhimlen.se

    this.gLocalSiderealTime = Math.toRadians( (280.461 + 360.98564737*d) + gObsrvLoc[1] ); // stardroid/util/TimeUtil.java
    // this.gLocalSiderealTime = Math.toRadians( (280.16 + 360.9856235*d) + obsrvLoc[1] ); // florianmski/suncalc/utils/PositionUtils.java

    this.gEarthXYZ = getEarthHeliocentricXYZ(d);
  }

  //////////////////////////////////////////////////////////////////////

  public static double mod2PI(double r) {
    double f = r / (2.0*Math.PI);
    double r1 = (2.0*Math.PI) * ( (f >= 0.0) ? f-Math.floor(f) : f-Math.ceil(f) );
    return (r1 < 0.0) ? (2.0*Math.PI)+r1 : r1;
  }

  public static double mod360(double d) {
    return (d+360.0) % 360.0;
  }

  //////////////////////////////////////////////////////////////////////

  // stardroid/units/HeliocentricCoordinates.java | CalculateEquatorialCoordinates
  public static double[] helioToGeocentricXYZ(double ecl, double[] hCoords) {
    double xh=hCoords[0], yh=hCoords[1], zh=hCoords[2];
    double y = yh*Math.cos(ecl) - zh*Math.sin(ecl);
    double z = zh*Math.cos(ecl) + yh*Math.sin(ecl);
    // Geocentric-Rectangular Equatorial Coordinates
    return new double[] {xh, y, z};
  }

  // stardroid/units/RaDec.java | calculateRaDecDist
  double[] xyzToRaDec(double[] xyz) {
    // input == [Rectangular Coordinates] == X,Y,Z
    // output == [Spherical Coordinates] == RA,DEC
    double rightAscension = Math.atan2(xyz[1], xyz[0]);
    double declination = Math.atan2(
      xyz[2], Math.sqrt(xyz[0]*xyz[0] + xyz[1]*xyz[1])
    );  // if (b>0) atan2(a,b)===atan(a/b)
    // declination ==?? Math.asin(xyz[2]);
    /*
      print("RA: " + rightAscension + ", DEC: " + declination);
      print("RightAscension: " + mod360(Math.toDegrees(rightAscension))/15.0 + " hrs");
      print("Declination: " + Math.toDegrees(declination) + "°");
    */
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
    // double distance, double meanLongitude, double eccentricity, double perihelion, double ascendingNode, double inclination
    return getHeliocentricXYZ(a, l, e, w, o, i);
  }

  double[] getMoonHeliocentricLLD(double daysSince2000) {
    // stardroid/provider/ephemeris/Planet.java | calculateLunarGeocentricLocation
    // https://books.google.com.my/books?id=HWVNAQAAMAAJ&pg=SL4-PA22
    /**
     * Calculate the geocentric right ascension and declination of the moon using
     * an approximation as described on page D22 of the 2008 Astronomical Almanac
     * All of the variables in this method use the same names as those described
     * in the text: lambda = Ecliptic longitude (degrees) beta = Ecliptic latitude
     * (degrees) pi = horizontal parallax (degrees) r = distance (Earth radii)
     *
     * NOTE: The text does not give a specific time period where the approximation
     * is valid, but it should be valid through at least 2009.
     */
    double jc = daysSince2000 / 36525.0;  // TimeUtil.julianCenturies(date)
    double lambda = 218.32 + 481267.881*jc + 6.29
                  * Math.sin(Math.toRadians(135.0 + 477198.87*jc)) - 1.27
                  * Math.sin(Math.toRadians(259.3 - 413335.36*jc)) + 0.66
                  * Math.sin(Math.toRadians(235.7 + 890534.22*jc)) + 0.21
                  * Math.sin(Math.toRadians(269.9 + 954397.74*jc)) - 0.19
                  * Math.sin(Math.toRadians(357.5 + 35999.05*jc)) - 0.11
                  * Math.sin(Math.toRadians(186.5 + 966404.03*jc));
    double beta = 5.13 * Math.sin(Math.toRadians(93.3 + 483202.02*jc)) + 0.28
                * Math.sin(Math.toRadians(228.2 + 960400.89*jc)) - 0.28
                * Math.sin(Math.toRadians(318.3 + 6003.15*jc)) - 0.17
                * Math.sin(Math.toRadians(217.6 - 407332.21*jc));
    double pi = 0.9508 + 0.0518 * Math.cos(Math.toRadians(135.0 + 477198.87*jc))
              + 0.0095 * Math.cos(Math.toRadians(259.3 - 413335.36*jc))
              + 0.0078 * Math.cos(Math.toRadians(235.7 + 890534.22*jc))
              + 0.0028 * Math.cos(Math.toRadians(269.9 + 954397.74*jc));
    double r = 1.0 / Math.sin(Math.toRadians(pi));
    return new double[] {Math.toRadians(lambda), Math.toRadians(beta), r};
  }

  public static double[] getVenusHeliocentricXYZ(double daysSince2000) {
    // stardroid/provider/ephemeris/Planet.java
    double jc = daysSince2000 / 36525.0;                      // TimeUtil.julianCenturies(date)
    double a = 0.72333566 + 0.00000390*jc;                    // distance
    double e = 0.00677672 - 0.00004107*jc;                    // eccentricity
    double i = Math.toRadians(3.39467605 - 0.00078890*jc);    // inclination
    double w = Math.toRadians(131.60246718 + 0.00268329*jc);  // perihelion
    double o = Math.toRadians(76.67984255 - 0.27769418*jc);   // ascendingNode
    double l = mod2PI(Math.toRadians(181.97909950 + 58517.81538729*jc));  // meanLongitude
    // double distance, double meanLongitude, double eccentricity, double perihelion, double ascendingNode, double inclination
    return getHeliocentricXYZ(a, l, e, w, o, i);
  }

  //////////////////////////////////////////////////////////////////////

  public float[] getCurrentSolarPosition() {
    // invert hEarthCoords to get Sun in Earth coordinates, not the Earth in Sun coordinates
    double[] hCoords = new double[] {-this.gEarthXYZ[0], -this.gEarthXYZ[1], -this.gEarthXYZ[2]};
    double[] eCoords = helioToGeocentricXYZ(this.gEcl, hCoords);
    
    return getAzimuthalCoordinates(  // "#sun@0°Az✳/0°Alt△"
      xyzToRaDec(eCoords), this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  public float[] getCurrentLunarPosition() {
    double[] mCoords = getMoonHeliocentricLLD(this.gDaysSince2000);
    double lambda=mCoords[0], beta=mCoords[1], r=mCoords[2];

    // get Geocentric XYZ | stardroid/provider/ephemeris/Planet.java | calculateLunarGeocentricLocation
    double l = Math.cos(beta) * Math.cos(lambda);
    double m = 0.9175*Math.cos(beta)*Math.sin(lambda) - 0.3978*Math.sin(beta);
    double n = 0.3978*Math.cos(beta)*Math.sin(lambda) + 0.9175*Math.sin(beta);

    return getAzimuthalCoordinates(  // "#moon@0°Az✳/0°Alt△"
      xyzToRaDec(new double[] {l, m, n}), this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  public float[] getCurrentVenusPosition() {
    double[] hCoords = getVenusHeliocentricXYZ(this.gDaysSince2000);
    hCoords[0]-=this.gEarthXYZ[0]; hCoords[1]-=this.gEarthXYZ[1]; hCoords[2]-=this.gEarthXYZ[2];
    double[] eCoords = helioToGeocentricXYZ(this.gEcl, hCoords);

    return getAzimuthalCoordinates(  // "#venus@0°Az✳/0°Alt△"
      xyzToRaDec(eCoords), this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  float[] getCurrentSiriusPosition() {
    return getAzimuthalCoordinates(  // "#sirius@0°Az✳/0°Alt△"
      new double[] {Math.toRadians(101.287), Math.toRadians(-16.716)},
      this.gLocalSiderealTime, Math.toRadians(this.gObsrvLoc[0])
    );
  }

  //////////////////////////////////////////////////////////////////////

}
