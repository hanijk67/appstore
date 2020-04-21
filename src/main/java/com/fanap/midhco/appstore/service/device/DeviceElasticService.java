package com.fanap.midhco.appstore.service.device;

import com.fanap.midhco.appstore.service.jest.JestService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin123 on 8/7/2017.
 */
public class DeviceElasticService {
    static Logger logger = Logger.getLogger(DeviceElasticService.class);

    public static DeviceElasticService Instance = new DeviceElasticService();

    private DeviceElasticService() {
    }

    public static class DeviceMetaData {
        String serialNumber;
        String productName;
        String manufacturer;
        String deviceClass;
        String screenWidth;
        String screenHeight;
        String cpuModel;
        String RAM;
        String screenDensityDpi;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getDeviceClass() {
            return deviceClass;
        }

        public void setDeviceClass(String deviceClass) {
            this.deviceClass = deviceClass;
        }

        public String getScreenWidth() {
            return screenWidth;
        }

        public void setScreenWidth(String screenWidth) {
            this.screenWidth = screenWidth;
        }

        public String getScreenHeight() {
            return screenHeight;
        }

        public void setScreenHeight(String screenHeight) {
            this.screenHeight = screenHeight;
        }

        public String getCpuModel() {
            return cpuModel;
        }

        public void setCpuModel(String cpuModel) {
            this.cpuModel = cpuModel;
        }

        public String getRAM() {
            return RAM;
        }

        public void setRAM(String RAM) {
            this.RAM = RAM;
        }

        public String getScreenDensityDpi() {
            return screenDensityDpi;
        }

        public void setScreenDensityDpi(String screenDensityDpi) {
            this.screenDensityDpi = screenDensityDpi;
        }
    }

    public static class DeviceMetaDataCriteria {
        String serialNumber;
    }

    public void applyDeviceMetaDataCriteria(DeviceMetaDataCriteria deviceMetaDataCriteria, BoolQueryBuilder boolQueryBuilder) {
        if (deviceMetaDataCriteria.serialNumber != null && !deviceMetaDataCriteria.serialNumber.isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("serialNumber", deviceMetaDataCriteria.serialNumber));
        }
    }

    public Long getDeviceCount(DeviceMetaDataCriteria deviceMetaDataCriteria) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyDeviceMetaDataCriteria(deviceMetaDataCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex("device")
                    .addType("deviceMetaData")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                return result.getTotal();
            } else {
                throw new IOException(result.getErrorMessage());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public List<DeviceMetaData> list(DeviceMetaDataCriteria deviceMetaDataCriteria, int from, int size) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();
        try {
            List<DeviceMetaData> retList = new ArrayList<>();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(from).size(size);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            applyDeviceMetaDataCriteria(deviceMetaDataCriteria, boolQueryBuilder);

            searchSourceBuilder.query(boolQueryBuilder);
            Search search = new Search.Builder(searchSourceBuilder.toString())
                    .addIndex("device")
                    .addType("deviceMetaData")
                    .build();

            SearchResult result = jestClient.execute(search);

            if (result.isSucceeded()) {
                if (result.getTotal() > 0) {
                    List<SearchResult.Hit<DeviceMetaData, Void>> tempResultList = result.getHits(DeviceMetaData.class);

                    for (SearchResult.Hit<DeviceMetaData, Void> tempResult : tempResultList) {
                        retList.add(tempResult.source);
                    }
                }
            } else {
                throw new IOException(result.getErrorMessage());
            }

            return retList;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public JestResult insertDeviceMetaData(DeviceElasticService.DeviceMetaData deviceMetaData) throws Exception {
        JestClient jestClient = JestService.Instance.getJestClient();

        try {
            if (deviceMetaData.getSerialNumber() == null) {
                throw new Exception("device has no serialNumber!");
            }
            Index index = new Index.Builder(deviceMetaData).index("device").type("deviceMetaData").id(deviceMetaData.serialNumber).build();
            JestResult jestResult = jestClient.execute(index);
            return jestResult;
        } catch (Exception ex) {
            throw ex;
        } finally {
            jestClient.close();
        }
    }

    public boolean isDeviceMetaDataPresent(String serialNumber) {
        DeviceElasticService.DeviceMetaDataCriteria criteria = new DeviceElasticService.DeviceMetaDataCriteria();
        criteria.serialNumber = serialNumber;

        try {
            Long deviceCount = Instance.getDeviceCount(criteria);
            if (deviceCount > 0)
                return true;
        } catch (Exception ex) {
            logger.error("error occured in getting device count ", ex);
        }
        return false;
    }
}
