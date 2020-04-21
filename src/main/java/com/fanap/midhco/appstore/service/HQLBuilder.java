package com.fanap.midhco.appstore.service;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.entities.helperClasses.DayDate;
import com.fanap.midhco.appstore.entities.helperClasses.DayTime;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

public class HQLBuilder implements Serializable {
    private final static Logger logger = Logger.getLogger(HQLBuilder.class);

    private ArrayList<String> joinClauses = new ArrayList<String>();
    private StringBuilder whereClauses = new StringBuilder();
    private ArrayList<String> orderClauses = new ArrayList<String>();

    private Map<String, Object> params = new HashMap<String, Object>();
    private Session session;
    private String selectClause;
    private String fromClause;
    private String groupBy;


    public String getFromClause() {
        return fromClause;
    }


    public HQLBuilder() {
    }

    public HQLBuilder(Session session) {
        this.session = session;
    }

    public HQLBuilder(Session session, Class fromEntity) {
        this(session, fromEntity, "select  ent");
    }

    public HQLBuilder(Session session, Class fromEntity, String selectClause) {
        this(session, selectClause, String.format("%s ent", fromEntity.getName()));
    }

    // Main Constructor
    public HQLBuilder(Session session, String selectClause, String fromClause) {
        this.session = session;
        this.selectClause = selectClause.toLowerCase().contains("select") ? selectClause : String.format("select %s", selectClause);
        this.fromClause = fromClause.contains("from") ? fromClause : String.format("from %s", fromClause);
    }

    public String getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }

    public void addClause(String clause) {
        whereClauses.append(" ").append(clause).append(" ");
    }

    public void addClause2(String clause, Object... params) {
        whereClauses.append(" ").append(String.format(clause, params)).append(" ");
    }

    public void addParam(String param, Object clause) {
        if (params.containsKey(param))
            throw new RuntimeException("Duplicate param in HQLBuilder: " + param);
        params.put(param, clause);
    }

    public void addClause(String clause, String param, Object obj) {
        addClause(clause);
        addParam(param, obj);
    }

    public void addClause(String clause, String param, Object... obj) {
        addClause(clause);
        ArrayList<Object> list = new ArrayList<Object>();
        if (obj != null)
            for (Object o : obj)
                if (o != null)
                    list.add(o);
        addParam(param, list);
    }

    public void addJoinClause(String joinClause) {
        joinClauses.add(String.format(" %s ", joinClause));
    }

    public void addOrder(String clause, boolean isAsc) {
        String[] strings = clause.split(",");
        for (String str : strings)
            orderClauses.add(String.format(" %s %s ", str, isAsc ? "asc" : "desc"));
    }

    public void addDateTimeLongRange(String propName, String param, DateTime[] range) {
        addDateTimeLongRange("ent", propName, param, range);
    }

    public void addDateTimeLongRange(String alias, String propName, String param, DateTime[] range) {
        if (!DateTime.isNullOrUnknown(range[0]) && !DateTime.isNullOrUnknown(range[1])) {
            addClause(String.format("and %1$s.%2$s.dayDate*1000000L+%1$s.%2$s.dayTime between :%3$s_l and :%3$s_u",
                    alias, propName, param));
            addParam(param + "_l", range[0].getDateTimeLong());
            addParam(param + "_u", range[1].getDateTimeLong());
        } else {
            if (!DateTime.isNullOrUnknown(range[0]))
                addClause(String.format("and %1$s.%2$s.dayDate*1000000L+%1$s.%2$s.dayTime>=:%3$s_l", alias, propName, param),
                        param + "_l", range[0].getDateTimeLong());
            if (!DateTime.isNullOrUnknown(range[1]))
                addClause(String.format("and %1$s.%2$s.dayDate*1000000L+%1$s.%2$s.dayTime<:%3$s_u", alias, propName, param),
                        param + "_u", range[1].getDateTimeLong());
        }
    }

    public void addDateTimeRange(String propName, String fromParam, String toParam, DateTime[] range) {
        addDateTimeRange("ent", propName, fromParam, toParam, range);
    }

    public void addDateTimeRange(String alias, String propName, String fromParam, String toParam, DateTime[] range) {
        if (!DateTime.isNullOrUnknown(range[0]) && !DateTime.isNullOrUnknown(range[1]) &&
                (range[0].getDayDate().equals(range[1].getDayDate()) ||
                        (range[0].getDayDate().equals(range[1].getDayDate().clone().previousDay()) && DayTime.isNullOrUnknown(range[1].getDayTime()))
                )
                ) {
            String str = String.format("and %s.%s.dayDate=:%s_dd", alias, propName, fromParam);
            if (!DayTime.isNullOrUnknown(range[0].getDayTime()))
                str += String.format(" and %s.%s.dayTime>=:%s_dt", alias, propName, fromParam);
            if (!DayTime.isNullOrUnknown(range[1].getDayTime()))
                str += String.format(" and %s.%s.dayTime<:%s_dt", alias, propName, toParam);
            addClause(str);
            addParam(fromParam + "_dd", range[0].getDayDate());
            if (!DayTime.isNullOrUnknown(range[0].getDayTime()))
                addParam(fromParam + "_dt", range[0].getDayTime());
            if (!DayTime.isNullOrUnknown(range[1].getDayTime()))
                addParam(toParam + "_dt", range[1].getDayTime());
        } else {
            if (!DateTime.isNullOrUnknown(range[0]))
                addDateTimeGt(alias, propName, fromParam, range[0]);
            if (!DateTime.isNullOrUnknown(range[1]))
                addDateTimeLt(alias, propName, toParam, range[1]);
        }
    }

    public void addDateTimeRange2(String alias, String propName, DateTime[] range) {
        if (!DateTime.isNullOrUnknown(range[0]) && !DateTime.isNullOrUnknown(range[1]) &&
                (range[0].getDayDate().equals(range[1].getDayDate()) ||
                        (range[0].getDayDate().equals(range[1].getDayDate().clone().previousDay()) && DayTime.isNullOrUnknown(range[1].getDayTime()))
                )
                ) {
            String str = String.format("and %s.%s.dayDate=%s", alias, propName, range[0].getDayDate().getDate());
            if (!DayTime.isNullOrUnknown(range[0].getDayTime()))
                str += String.format(" and %s.%s.dayTime>=%s", alias, propName, range[0].getDayTime().getDayTime());
            if (!DayTime.isNullOrUnknown(range[1].getDayTime()))
                str += String.format(" and %s.%s.dayTime<%s", alias, propName, range[1].getDayTime().getDayTime());
            addClause(str);
        } else {
            if (!DateTime.isNullOrUnknown(range[0]))
                addDateTimeGt2(alias, propName, range[0]);
            if (!DateTime.isNullOrUnknown(range[1]))
                addDateTimeLt2(alias, propName, range[1]);
        }
    }

    public void addDayDateRange(String alias, String propName, String fromParam, String toParam, DayDate[] range) {
        if (!DayDate.isNullOrUnknown(range[0]) && !DayDate.isNullOrUnknown(range[1]) &&
                (range[0].equals(range[1]) || (range[0].equals(range[1].clone().previousDay()))
                )) {
            String str = String.format("and %s.%s=:%s_dd", alias, propName, fromParam);
            addClause(str);
            addParam(fromParam + "_dd", range[0]);
        } else {
            if (!DayDate.isNullOrUnknown(range[0]))
                addDayDateGt(alias, propName, fromParam, range[0]);
            if (!DayDate.isNullOrUnknown(range[1]))
                addDayDateLt(alias, propName, toParam, range[1]);
        }
    }


    public void addDateTimeGt(String propName, String param, DateTime dt) {
        addDateTimeGt("ent", propName, param, dt);
    }

    public void addDateTimeGt(String alias, String propName, String param, DateTime dt) {
        String str;
        if (DayTime.isNullOrUnknown(dt.getDayTime()))
            str = String.format("and %s.%s.dayDate>=:%s_dd", alias, propName, param);
        else
            str = String.format("and (%1$s.%2$s.dayDate>:%3$s_dd or (%1$s.%2$s.dayDate=:%3$s_dd and %1$s.%2$s.dayTime>=:%3$s_dt))",
                    alias, propName, param);
        addClause(str);
        addParam(param + "_dd", dt.getDayDate());
        if (!DayTime.isNullOrUnknown(dt.getDayTime()))
            addParam(param + "_dt", dt.getDayTime());
    }

    public void addDateTimeGt2(String alias, String propName, DateTime dt) {
        String str;
        if (DayTime.isNullOrUnknown(dt.getDayTime()))
            str = String.format("and %s.%s.dayDate>=%s", alias, propName, dt.getDayDate().getDate());
        else
            str = String.format("and (%1$s.%2$s.dayDate>%3$s or (%1$s.%2$s.dayDate=%3$s and %1$s.%2$s.dayTime>=%4$s))",
                    alias, propName, dt.getDayDate().getDate(), dt.getDayTime().getDayTime());
        addClause(str);
    }

    public void addDayDateGt(String alias, String propName, String param, DayDate dt) {
        String str = String.format("and %s.%s>=:%s_dd", alias, propName, param);
        addClause(str);
        addParam(param + "_dd", dt);
    }

    public void addDayDateLt(String alias, String propName, String param, DayDate dt) {
        String str = String.format("and %s.%s<=:%s_dd", alias, propName, param);
        addClause(str);
        addParam(param + "_dd", dt);
    }

    public void addDateTimeLt(String propName, String param, DateTime dt) {
        addDateTimeLt("ent", propName, param, dt);
    }

    public void addDateTimeLt(String alias, String propName, String param, DateTime dt) {
        String str;
        if (DayTime.isNullOrUnknown(dt.getDayTime()))
            str = String.format("and %s.%s.dayDate<:%s_dd", alias, propName, param);
        else
            str = String.format("and (%1$s.%2$s.dayDate<:%3$s_dd or (%1$s.%2$s.dayDate=:%3$s_dd and %1$s.%2$s.dayTime<=:%3$s_dt))",
                    alias, propName, param);
        addClause(str);
        addParam(param + "_dd", dt.getDayDate());
        if (!DayTime.isNullOrUnknown(dt.getDayTime()))
            addParam(param + "_dt", dt.getDayTime());
    }

    public void addDateTimeLt2(String alias, String propName, DateTime dt) {
        String str;
        if (DayTime.isNullOrUnknown(dt.getDayTime()))
            str = String.format("and %s.%s.dayDate<%s", alias, propName, dt.getDayDate().getDate());
        else
            str = String.format("and (%1$s.%2$s.dayDate<%3$s or (%1$s.%2$s.dayDate=%3$s and %1$s.%2$s.dayTime<=%4$s))",
                    alias, propName, dt.getDayDate().getDate(), dt.getDayTime().getDayTime());
        addClause(str);
    }

    public void addRange(String propName, String param, Long[] range) {
        addRange("ent", propName, param, range);
    }

    public void addRange(String alias, String propName, String param, Long[] range) {
        if (range[0] != null && range[0].equals(range[1]))
            addClause(String.format("and %s.%s=:%s", alias, propName, param), param, range[0]);
        else {
            if (range[0] != null)
                addClause(String.format("and %s.%s >= :%s_l", alias, propName, param), param + "_l", range[0]);
            if (range[1] != null)
                addClause(String.format("and %s.%s < :%s_u", alias, propName, param), param + "_u", range[1]);
        }
    }

    public void addRange2(String alias, String propName, Long[] range) {
        if (range[0] != null && range[0].equals(range[1]))
            addClause2("and %s.%s=%s", alias, propName, range[0]);
        else {
            if (range[0] != null)
                addClause2("and %s.%s >= %s", alias, propName, range[0]);
            if (range[1] != null)
                addClause2("and %s.%s < %s", alias, propName, range[1]);
        }
    }

    public void addSubSelect(String clause, String param, HQLBuilder builder) {
        String subSelect = clause.replace(":" + param, builder.getQueryString());
        addClause(subSelect);
        params.putAll(builder.params);
    }

    public Query createQuery() {
        String queryString = getQueryString();
        logger.debug(String.format("#> HQLBuilder: %s", queryString));
        Query query = session.createQuery(queryString);
        addParams(query, params);
        return query;
    }

    private String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("%s %s ", selectClause, fromClause));
        for (String joinClause : joinClauses)
            queryBuilder.append(joinClause);

        if (whereClauses.length() > 0)
            queryBuilder.append(" where 1=1 ").append(whereClauses.toString());

        if (groupBy != null)
            queryBuilder.append(" group by ").append(groupBy);

        if (orderClauses.size() > 0) {
            queryBuilder.append(" order by ").append(orderClauses.get(0));
            for (int i = 1; i < orderClauses.size(); i++)
                queryBuilder.append(", ").append(orderClauses.get(i));
        }
        return queryBuilder.toString();
    }

    public String getWhereClause() {
        return whereClauses.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static String like(String property) {
        return "%" + property + "%";
    }

    public static String inClause(List list) {
        return inClause(list, null);
    }

    public static String inClause(List list, String prop) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPropSafe(list.get(0), prop));
        for (int i = 1; i < list.size(); i++)
            builder.append(",").append(getPropSafe(list.get(i), prop));
        return builder.toString();
    }

    private static Object getPropSafe(Object obj, String prop) {
        try {
            Object o;
            if (prop != null)
                o = PropertyUtils.getProperty(obj, prop);
            else
                o = obj;
            if (o instanceof String)
                return String.format("'%s'", o);
            else
                return o;
        } catch (Exception e) {
            logger.error("getPropSafe: ", e);
        }
        return null;
    }

    public static void addParams(Query query, Map<String, Object> params) {
        if (params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof Collection)
                    query.setParameterList(entry.getKey(), (Collection) entry.getValue());
                else
                    query.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    public static List extractIds(Collection objs, String idProp) {
        List list = new ArrayList();
        for (Object obj : objs) {
            try {
                list.add(PropertyUtils.getProperty(obj, idProp));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    public void replace(String src, String dst) {
        String str = whereClauses.toString().replace(src, dst);
        whereClauses = new StringBuilder();
        whereClauses.append(str);
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public void addInnerBuilder(String childAlias, String dest, HQLBuilder child, boolean isDestAlias) {
        String regularReplacement = " " + childAlias + ".";
        String modifiedWhere = child.getWhereClause().replaceAll(regularReplacement, isDestAlias ? " ".concat(dest).concat(".") : " ".concat(childAlias).concat(".").concat(dest.concat(".")));
        Map<String, Object> parMap = new HashMap<String, Object>();
        for (Object key : child.getParams().keySet()) {
            String rep = dest.concat("_");
            rep = rep.concat((String) key);
            parMap.put(rep, child.getParams().get(key));
            String matchKey = ":".concat((String) key);
            modifiedWhere = modifiedWhere.replaceAll(matchKey, ":".concat(rep));
        }
        addClause2(modifiedWhere, parMap);
        getParams().putAll(parMap);
    }
}
