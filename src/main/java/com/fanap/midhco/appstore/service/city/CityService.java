package com.fanap.midhco.appstore.service.city;

import com.fanap.midhco.appstore.entities.City;
import com.fanap.midhco.appstore.entities.State;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by admin123 on 6/25/2016.
 */
public class CityService {
    public static CityService Instance = new CityService();

    private CityService() {}

    public List<City> listStateCities(State state) {
        Session session = HibernateUtil.getCurrentSession();
        String queryString = "select city from City city where city.state = :state";
        Query query = session.createQuery(queryString);
        query.setParameter("state", state);
        return query.list();
    }
}
