package com.fanap.midhco.appstore.service.state;

import com.fanap.midhco.appstore.entities.State;
import com.fanap.midhco.appstore.service.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by admin123 on 6/25/2016.
 */
public class StateService {
    public static StateService Instance = new StateService();

    private StateService() {
    }

    public List<State> listAllStates() {
        Session session = HibernateUtil.getCurrentSession();
        try {
            String queryString = "select state from State state";
            Query query = session.createQuery(queryString);
            return query.list();
        } finally {
            session.close();
        }
    }

}
