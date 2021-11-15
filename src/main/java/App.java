import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.AirlineCoverage;
import flights.Airline;

public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String argv[]) {
        System.out.println("Running App ...");

        log.debug("Create persistence manager");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("myApp");
        EntityManager em = emf.createEntityManager();

        log.debug("Search for French Airlines");

        List<Airline> airlines = em
                .createQuery("from Airline as al where al.country = :country", Airline.class)
                .setParameter("country", "France").getResultList();

        showAirlines(airlines);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Airline> cq = cb.createQuery(Airline.class);
        ParameterExpression<String> valeur = cb.parameter(String.class);
        Root<Airline> airline = cq.from(Airline.class);
        cq.select(airline).where(cb.equal(airline.get("country"), valeur));
        TypedQuery<Airline> tq = em.createQuery(cq);
        tq.setParameter(valeur, "France");
        airlines = tq.getResultList();

        showAirlines(airlines);

        log.debug("Search for French Airlines going to Miami");

        airlines = em
                .createQuery("select distinct rt.airline from Route as rt join rt.airline as al "
                        + "join rt.destination as dst "
                        + "where al.country = :country and dst.city = :city", Airline.class)
                .setParameter("country", "France").setParameter("city", "Miami").getResultList();

        showAirlines(airlines);

        log.debug("Search Number of airlines from CDG");

        long count = em
                .createQuery("select count(distinct rt.airline) from Route as rt "
                        + "join rt.source as src where src.iata = :code", Long.class)
                .setParameter("code", "CDG").getSingleResult();

        System.out.println(count + " airlines stars at CDG");

        log.debug(
                "Search Best airlines with number of dest airports to USA from CDG, ordered by 'coverage'");

        List<AirlineCoverage> res = em.createQuery(
                "select new data.AirlineCoverage(count(rt.destination), apS.country, al.name) from Route as rt "
                        + "join rt.destination as apD " + "join rt.source as apS "
                        + "join rt.airline as al " + "where apD.country = :country "
                        + " and apS.iata = :code " + "group by apS.country, al.name "
                        + "order by count(rt.destination) desc",
                AirlineCoverage.class).setParameter("country", "United States")
                .setParameter("code", "CDG").getResultList();

        for (AirlineCoverage airlineCoverage : res) {
            System.out.println(String.format("%3s | %-20s | %-30s", airlineCoverage.getNbRoutes(),
                    airlineCoverage.getCountry(), airlineCoverage.getAirlineName()));
        }

        log.debug("Close Entity Manager");
        em.close();
        emf.close();

    }

    private static void showAirlines(List<Airline> airlines) {
        System.out.println(String.format("%5s | %3s | %-30s", "Id", "ICAO", "Name"));
        for (Airline airline : airlines) {
            System.out.println(String.format("%5s | %3s | %-30s", airline.getId(),
                    airline.getIcao(), airline.getName()));
        }
    }

}