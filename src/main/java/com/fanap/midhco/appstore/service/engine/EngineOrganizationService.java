package com.fanap.midhco.appstore.service.engine;

import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by A.Moshiri on 10/29/2018.
 */
public class EngineOrganizationService {
    final static org.apache.logging.log4j.Logger logger = LogManager.getLogger();
    public static EngineOrganizationService Instance = new EngineOrganizationService();

    private EngineOrganizationService() {
    }

    public List<OrganizationVO> getAllOrganization() throws Exception {
        CompletableFuture<List<OrganizationVO>> organizationVOCompletableFuture = EngineOrganizationProviderService.INSTANCE.getOrganizations();
        organizationVOCompletableFuture.join();
        return organizationVOCompletableFuture.get();
    }

    public List<OrganizationVO> getAllOrganization(Map<Long, OrganizationVO> organizationVOMap, Map<String, OrganizationVO> organizationNameVOMap) throws Exception {

        List<OrganizationVO> organizationVOList = getAllOrganization();

        organizationVOList = buildOrganizationVoByJsonArray(organizationVOList, organizationVOMap, organizationNameVOMap);

        return organizationVOList;
    }

    private List<OrganizationVO> buildOrganizationVoByJsonArray(JSONArray jsonArray) {
        List<OrganizationVO> organizationVOList = new ArrayList<>();
        List<OrganizationVO> needToAddParent = new ArrayList<>();
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                int i = 0;
                Map<Long, OrganizationVO> organizationVOMap = new HashMap<>();
                Map<String, OrganizationVO> organizationVoNameMap = new HashMap<>();
                buildOrgVoListAndMap(jsonArray, organizationVOList, needToAddParent, organizationVOMap, organizationVoNameMap);
            }
            return organizationVOList;
        } catch (Exception e) {
            return null;
        }


    }

    private void buildOrgVoListAndMap(Iterable organizationIterable, List<OrganizationVO> organizationVOList, List<OrganizationVO> needToAddParent,
                                      Map<Long, OrganizationVO> organizationVOMap, Map<String, OrganizationVO> organizationNameMap) {
        OrganizationVO rootOrganizationVO = new OrganizationVO();
        rootOrganizationVO.setTitleFa("root");
        rootOrganizationVO.setParent(null);
        rootOrganizationVO.setId(Long.valueOf(-1));
        organizationVOMap.put(Long.valueOf(-1), rootOrganizationVO);
        organizationNameMap.put("root", rootOrganizationVO);
        organizationVOList.add(rootOrganizationVO);

        Iterator iterator = organizationIterable.iterator();

        while (iterator.hasNext()) {
            OrganizationVO organizationVO = (OrganizationVO) iterator.next();
            organizationVO.setNickName(organizationVO.getTitleFa());
            organizationVOMap.put(organizationVO.getId(), organizationVO);
            organizationNameMap.put(organizationVO.getTitleFa(), organizationVO);
            if (organizationVO.getParentId() != null) {
                if (organizationVOMap.get(organizationVO.getParentId()) != null) {
                    try {
                        organizationVO.setParent(organizationVOMap.get(organizationVO.getParentId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    organizationVOList.add(organizationVO);
                } else {
                    needToAddParent.add(organizationVO);
                }
            } else {
                organizationVO.setParent(rootOrganizationVO);
                organizationVOList.add(organizationVO);
            }
        }

        for (OrganizationVO organizationVO : needToAddParent) {
            organizationVO.setParent(organizationVOMap.get(organizationVO.getParentId()));
            organizationVOList.add(organizationVO);
        }
    }


    private List<OrganizationVO> buildOrganizationVoByJsonArray(Iterable organizationIterable, Map<Long, OrganizationVO> organizationVOMap, Map<String, OrganizationVO> organizationNameMap) {
        List<OrganizationVO> organizationVOList = new ArrayList<>();
        List<OrganizationVO> needToAddParent = new ArrayList<>();
        try {
            buildOrgVoListAndMap(organizationIterable, organizationVOList, needToAddParent, organizationVOMap, organizationNameMap);
            return organizationVOList;
        } catch (Exception e) {
            return null;
        }


    }


    private static JSONArray parseEngineResponseStream(HttpURLConnection httpURLConnection) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader rd;
        rd = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line = null;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();

        return new JSONArray(result.toString());
    }


}
