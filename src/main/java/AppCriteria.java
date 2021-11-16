import data.AirlineCoverage;
import flights.Airline;
import flights.Airport;
import flights.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

public class AppCriteria {
    private static Logger log = LoggerFactory.getLogger(AppCriteria.class);

    public static void main(String argv[]) {
        System.out.println("Running App ...");

        log.debug("Create persistence manager");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("myApp");
        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        {
            log.debug("Search for French Airlines");
            CriteriaQuery<Airline> cq = cb.createQuery(Airline.class);
            ParameterExpression<String> valeur = cb.parameter(String.class);
            Root<Airline> airline = cq.from(Airline.class);
            cq.select(airline).where(cb.equal(airline.get("country"), valeur));
            TypedQuery<Airline> tq = em.createQuery(cq);
            tq.setParameter(valeur, "France");
            List<Airline> airlines = tq.getResultList();

            //showAirlines(airlines);
        }
        {
            log.debug("Search for French Airlines going to Miami");

            // airlines = em
            //.createQuery("select distinct rt.airline from Route as rt join rt.airline as al "
            //+ "join rt.destination as dst "
                        /*+ "where al.country = :country and dst.city = :city", Airline.class)
                .setParameter("country", "France").setParameter("city", "Miami").getResultList();*/

            //showAirlines(airlines);

            //notre requête

            CriteriaQuery<Airline> cq = cb.createQuery(Airline.class);
            ParameterExpression<String> valeurPays = cb.parameter(String.class);
            ParameterExpression<String> valeurCity = cb.parameter(String.class);
            Root<Route> route = cq.from(Route.class);

            Join<Route, Airline> airlineJoin = route.join("airline");

            Join<Route, Airport> destinationJoin = route.join("destination");

            cq.select(route.get("airline")).distinct(true).where(cb.and(cb.equal(airlineJoin.get("country"), valeurPays), cb.equal(destinationJoin.get("city"), valeurCity)));
            TypedQuery<Airline> tq2 = em.createQuery(cq);
            tq2.setParameter(valeurPays, "France");
            tq2.setParameter(valeurCity, "Miami");
            List<Airline> airlinesResult = tq2.getResultList();

            showAirlines(airlinesResult);
        }
        {
            log.debug("Search Number of airlines from CDG");

            /*long count = em
                    .createQuery("select count(distinct rt.airline) from Route as rt "
                            + "join rt.source as src where src.iata = :code", Long.class)
                    .setParameter("code", "CDG").getSingleResult();*/
            //requete console h2
            //select count(*) from airline left join route on route.airline_id= airline.id left join airport on route.source_id = airport.id where airport.iata = 'CDG'
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            ParameterExpression<String> searchCDG = cb.parameter(String.class);
            Root<Route> route = cq.from(Route.class);

            Join<Route, Airport> airportIATA = route.join("source");

            cq.select(cb.countDistinct(route.get("airline"))).where(cb.equal(airportIATA.get("iata"), searchCDG));
            TypedQuery<Long> tq = em.createQuery(cq);
            tq.setParameter(searchCDG, "CDG");
            Long count = tq.getSingleResult();
            System.out.println(count + " airlines stars at CDG");
        }


        {
            log.debug(
                    "Search Best airlines with number of dest airports to USA from CDG, ordered by 'coverage'");
            /*
            List<AirlineCoverage> res = em.createQuery(
                            "select new data.AirlineCoverage(count(rt.destination), apS.country, al.name) from Route as rt "
                                    + "join rt.destination as apD " + "join rt.source as apS "
                                    + "join rt.airline as al " + "where apD.country = :country "
                                    + " and apS.iata = :code " + "group by apS.country, al.name "
                                    + "order by count(rt.destination) desc",
                            AirlineCoverage.class).setParameter("country", "United States")
                    .setParameter("code", "CDG").getResultList();*/

            //notre requête

            CriteriaQuery<AirlineCoverage> cq = cb.createQuery(AirlineCoverage.class);
            CriteriaQuery<Long> cqLong = cb.createQuery(Long.class);
            //cqLong.select(cb.count(cqLong.from(MyEntity.class)));
            ParameterExpression<String> valeurCountry = cb.parameter(String.class);
            ParameterExpression<String> valeurIATA = cb.parameter(String.class);
            Root<Route> route = cq.from(Route.class);

            Join<Route, Airport> destinationJoin = route.join("destination");
            Join<Route, Airport> airportJoin = route.join("source");
            Join<Route, Airline> airlineJoin = route.join("airline");

            Expression<Long> count =  cb.count(route.get("destination"));

            cq.select(cb.construct(AirlineCoverage.class, count, airportJoin.get("country"), airlineJoin.get("name")))
                    .where(cb.and(cb.equal(destinationJoin.get("country"), valeurCountry), cb.equal(airportJoin.get("iata"), valeurIATA)))
                    .groupBy(destinationJoin.get("country"), airlineJoin.get("name"))
                    .orderBy(cb.desc(count));

            TypedQuery<AirlineCoverage> tq = em.createQuery(cq);
            tq.setParameter(valeurCountry, "United States");
            tq.setParameter(valeurIATA, "CDG");
            List<AirlineCoverage> res = tq.getResultList();

            showAirlineCoverage(res);
        }
        log.debug("Close Entity Manager");
        em.close();
        emf.close();

    }

    private static void showAirlineCoverage(List<AirlineCoverage> res) {
        for (AirlineCoverage airlineCoverage : res) {
            System.out.println(String.format("%3s | %-20s | %-30s", airlineCoverage.getNbRoutes(),
                    airlineCoverage.getCountry(), airlineCoverage.getAirlineName()));
        }
    }

    private static void showAirlines(List<Airline> airlines) {
        System.out.println(String.format("%5s | %3s | %-30s", "Id", "ICAO", "Name"));
        for (Airline airline : airlines) {
            System.out.println(String.format("%5s | %3s | %-30s", airline.getId(),
                    airline.getIcao(), airline.getName()));
        }
    }


}