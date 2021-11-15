package data;

public class AirlineCoverage {

    long nbRoutes;
    String country;
    String airlineName;

    /**
     * @param nbRoutes
     * @param country
     * @param airlineName
     */
    public AirlineCoverage(long nbRoutes, String country, String airlineName) {
        super();
        this.nbRoutes = nbRoutes;
        this.country = country;
        this.airlineName = airlineName;
    }

    /**
     * @return the nbRoutes
     */
    public long getNbRoutes() {
        return nbRoutes;
    }

    /**
     * @param nbRoutes
     *            the nbRoutes to set
     */
    public void setNbRoutes(long nbRoutes) {
        this.nbRoutes = nbRoutes;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country
     *            the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the airlineName
     */
    public String getAirlineName() {
        return airlineName;
    }

    /**
     * @param airlineName
     *            the airlineName to set
     */
    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

}
