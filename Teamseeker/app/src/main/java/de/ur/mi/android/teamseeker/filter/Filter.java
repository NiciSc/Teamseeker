package de.ur.mi.android.teamseeker.filter;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

public class Filter {

    //https://developer.android.com/reference/android/support/annotation/StringDef
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            FILTER_NAME,
            FILTER_ISFULL,
            FILTER_MINAGE,
            FILTER_TYPE,
            FILTER_MAXRADIUS,
            FILTER_ID,
            FILTER_GETALL,
            FILTER_DATE})
    public @interface FilterDef {
    }


    /*** The strings are used to identify filters in database queries
     * They are easier to maintain here than in strings.xml
     */
    //region static strings
    public static final String FILTER_NAME = "filter:name";
    public static final String FILTER_ISFULL = "filter:isfull";
    public static final String FILTER_MINAGE = "filter:minage";
    public static final String FILTER_TYPE = "filter:type";
    public static final String FILTER_MAXRADIUS = "filter:maxradius";
    public static final String FILTER_ID = "filter:id";
    public static final String FILTER_GETALL = "filter:all";
    public static final String FILTER_DATE = "filter:date";
    //endregion


    private HashMap<String, Object> filters = new HashMap<>();

    public static Filter empty() {
        return new Filter();
    }

    public static Filter all() {
        Filter filter = Filter.empty();
        filter.addFilter(FILTER_GETALL, null);
        return filter;
    }

    public void addFilter(@FilterDef String filterType, Object value) {
        filters.put(filterType, value);
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public HashMap<String, Object> getFilters() {
        return filters;
    }

    public boolean containsFilterType(@FilterDef String filterType) {
        return filters.containsKey(filterType);
    }

    public Object getValue(@FilterDef String filterType) {
        return filters.get(filterType);
    }
}
