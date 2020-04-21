package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.restControllers.vos.OrganizationVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 4/9/2018.
 */
@RestController
@RequestMapping("/service/organization")
public class OrganizationRestController {

    @RequestMapping(value = "listOrganization", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO listOrganization(@RequestParam(required = false) OrganizationVO organizationVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();

            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.ORGANIZATION_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(requesterUser);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            List<Organization> organizationListList = new ArrayList<>();
            OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
            session = HibernateUtil.getCurrentSession();
            Object resultObject = null;

            if (organizationVO != null) {
                boolean hasValue = false;

                if (organizationVO.getId() != null) {
                    orgCriteria.setId(organizationVO.getId());
                    hasValue = true;
                }
                if (organizationVO.getNickName() != null && !organizationVO.getNickName().trim().equals("")) {
                    orgCriteria.setNickName(organizationVO.getNickName().trim());
                    hasValue = true;
                }
                if (organizationVO.getFullName() != null && !organizationVO.getFullName().trim().equals("")) {
                    orgCriteria.setFullName(organizationVO.getFullName().trim());
                    hasValue = true;
                }
                if (organizationVO.getEnglishFullName() != null && !organizationVO.getEnglishFullName().trim().equals("")) {
                    orgCriteria.setEnglishFullName(organizationVO.getEnglishFullName().trim());
                    hasValue = true;
                }
                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return responseVO;
                }
            }
            String sortBy = PrincipalUtil.getSortBy();
            String sortProperties = null;
            if (sortBy != null) {
                try {
                    List<String> declaredField = new Organization().getDeclaredField();
                    if (declaredField.contains(sortBy)) {
                        sortProperties = sortBy;
                    }
                } catch (Exception e) {
                    sortProperties = null;
                }
            }
            if(getResultCount){
                Long resultCont = OrgService.Instance.count(orgCriteria , session);
                resultObject = resultCont;
            }else {
                organizationListList = OrgService.Instance.list(orgCriteria, fromIndex, countIndex, sortProperties, isAscending, session);

                if (organizationListList != null && !organizationListList.isEmpty()) {
                    Stream<OrganizationVO> organizationVOStream = organizationListList.stream().map(appCategory -> OrganizationVO.buildOrganizationVO(appCategory));
                    resultObject = organizationVOStream.collect(Collectors.<OrganizationVO>toList());
                } else {
                    resultObject = new ArrayList<OrganizationVO>();
                }
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(resultObject));
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "addOrganization", headers = "accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO addOrganization(@RequestParam(required = true) OrganizationVO organizationVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (organizationVO != null && organizationVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.ORGANIZATION_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveOrganizationToDataBase(organizationVO);
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    @RequestMapping(value = "editOrganization", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO editOrganization(@RequestParam(required = true) OrganizationVO organizationVO, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (organizationVO == null || organizationVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.NULL_DATA);
                responseVO.setResult(ResultStatus.NULL_DATA.toString());
            } else {
                Long orgId = organizationVO.getId();

                try {
                    PrincipalUtil.setCurrentUser(requesterUser);
                    Session session = HibernateUtil.getCurrentSession();
                    Organization loadedOrganization = (Organization) session.get(Organization.class, orgId);
                    if (loadedOrganization == null) {
                        responseVO.setResultStatus(ResultStatus.ORGANIZATION_NOT_FOUND);
                        responseVO.setResult(ResultStatus.ORGANIZATION_NOT_FOUND.toString());
                        return responseVO;
                    }

                    UserService.Instance.checkAccessibility(requesterUser, Access.ORGANIZATION_EDIT, responseVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    responseVO = saveOrganizationToDataBase(organizationVO);
                } catch (Exception e) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(e.getMessage());
                }
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }
        return responseVO;
    }


    private ResponseVO saveOrganizationToDataBase(OrganizationVO organizationVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        if (organizationVO != null) {
            try {
                session = HibernateUtil.getNewSession();

                Organization organization = null;
                if (organizationVO.getId() != null) {
                    organization = (Organization) session.load(Organization.class, organizationVO.getId());
                } else {
                    responseVO = OrgService.Instance.checkInputOrganizationVo(organizationVO);
                    if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                        return responseVO;
                    }
                    organization = new Organization();
                }

                if (organizationVO.getNickName() != null && !organizationVO.getNickName().trim().equals("")) {
                    OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
                    orgCriteria.setNickName(organizationVO.getNickName().trim());

                    List<Organization> organizationList = OrgService.Instance.list(orgCriteria, 0, -1, null, false, session);
                    if (organizationVO.getId() == null) {
                        if (organizationList != null && organizationList.size() > 0) {
                            responseVO.setResultStatus(ResultStatus.ORGANIZATION_EXIST);
                            responseVO.setResult(ResultStatus.ORGANIZATION_EXIST.toString());
                            return responseVO;
                        }
                    } else {
                        for (Organization organizationInList : organizationList) {
                            if (!organizationInList.getId().equals(organization.getId())) {
                                responseVO.setResultStatus(ResultStatus.ORGANIZATION_EXIST);
                                responseVO.setResult(ResultStatus.ORGANIZATION_EXIST.toString());
                                return responseVO;
                            }
                        }
                    }
                    organization.setNickName(organizationVO.getNickName().trim());
                }

                if (organizationVO.getFullName() != null && !organizationVO.getFullName().trim().equals("")) {
                    organization.setFullName(organizationVO.getFullName());
                }

                if (organizationVO.getEnglishFullName() != null && !organizationVO.getEnglishFullName().trim().equals("")) {
                    organization.setEnglishFullName(organizationVO.getEnglishFullName());
                }

                if (organizationVO.getIconFilePath() != null && !organizationVO.getIconFilePath().trim().equals("")) {
                    File iconFile = new File();
                    iconFile.setStereoType(StereoType.THUMB_FILE);
                    iconFile.setFilePath(organizationVO.getIconFilePath());
                    iconFile.setFileName(FileServerService.Instance.getFileNameFromFilePath(organizationVO.getIconFilePath()));
                    BaseEntityService.Instance.saveOrUpdate(iconFile, session);
                    organization.setIconFile(iconFile);
                }

                Transaction tx = session.beginTransaction();
                BaseEntityService.Instance.saveOrUpdate(organization, session);
                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                OrganizationVO convertedOrganizationVO = OrganizationVO.buildOrganizationVO(organization);
                String organizationString = JsonUtil.getJson(convertedOrganizationVO);
                organizationString.replaceAll("\\\\", "");
                String replaceStr = organizationString.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
                responseVO.setResult(replaceStr);

            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(e.getMessage());
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
        return responseVO;
    }
}
