package com.fanap.midhco.appstore.service.anouncement;

import java.util.Map;

/**
 * Created by admin123 on 8/28/2017.
 */
public interface IAnouncementActionDescriptor<T> {
    public ITaskResult<T> doAction(String actionDescriptor, Map<String, String> parametersMap) throws Exception;
}
