package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.*;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.restControllers.exceptions.PreProcessorException;
import com.fanap.midhco.appstore.restControllers.vos.*;
import com.fanap.midhco.appstore.service.AppInstallReportQueue.AppInstallReportQueueService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.anouncement.AnouncementService;
import com.fanap.midhco.appstore.service.anouncement.ITaskResult;
import com.fanap.midhco.appstore.service.app.*;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.comment.CommentService;
import com.fanap.midhco.appstore.service.engine.EngineOrganizationService;
import com.fanap.midhco.appstore.service.environment.EnvironmentService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.handlerApp.HandlerAppService;
import com.fanap.midhco.appstore.service.login.JWTService;
import com.fanap.midhco.appstore.service.login.SSOUserService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.appstore.service.org.OrgService;
import com.fanap.midhco.appstore.service.os.OSService;
import com.fanap.midhco.appstore.service.osType.OSTypeService;
import com.fanap.midhco.appstore.service.security.AccessService;
import com.fanap.midhco.appstore.service.timeLine.TimeLineElasticService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.google.gson.JsonObject;
import io.searchbox.client.JestResult;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by admin123 on 7/4/2016.
 */
@RestController
@RequestMapping("/service")
public class AppStoreRestController {
    static private final Logger logger = Logger.getLogger(AppStoreRestController.class);
    Long envId = null;

    private static OSType preProcess_OSTYPE() throws Exception {
        if (PrincipalUtil.getCurrentOSTYPE() == null) {
            throw new PreProcessorException(ErrorPhrases.NO_OSTYPE_RECIEVED_OR_UNKNOWN_OSTYPE.getMessage());
        }

        return PrincipalUtil.getCurrentOSTYPE();
    }

    @RequestMapping(value = "/getNumberOfProducts", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Integer getNumberOfProducts(@RequestParam(required = false) String jwtToken, HttpServletRequest request) {
        Long userId = null;
        if (jwtToken != null)
            userId = JWTService.validateAndGetUser(jwtToken);

        AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        criteria.publishStates = new ArrayList<>();
        criteria.publishStates.add(PublishState.PUBLISHED);
        Session session = HibernateUtil.getNewSession();

        try {
            if (userId != null) {
                criteria.developers = new ArrayList<>();
                User user = UserService.Instance.getUserBySSOId(userId, session);
                criteria.developers.add(user);
            }

            OSType osType = preProcess_OSTYPE();
            criteria.osType = new ArrayList<>();
            criteria.osType.add(osType);

            criteria.setDeleted(false);

            return AppService.Instance.count(criteria, session).intValue();
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getAllProducts", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAllProductsByQueryParameter(@RequestParam int from, @RequestParam int count, @RequestParam(required = false) String jwtToken, HttpServletRequest request, HttpServletResponse response) {
        return getAllProducts(from, count, jwtToken, request, response);
    }

    @RequestMapping(value = "/getAllProducts/{from}/{count}", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAllProducts(@PathVariable int from, @PathVariable int count, @RequestParam(required = false) String jwtToken, HttpServletRequest request, HttpServletResponse response) {
        Long userId = null;


        if (jwtToken != null)
            userId = JWTService.validateAndGetUser(jwtToken);

        AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        criteria.publishStates = new ArrayList<>();
        criteria.publishStates.add(PublishState.PUBLISHED);

        Session session = HibernateUtil.getNewSession();
        try {
            if (userId != null) {
                criteria.developers = new ArrayList<>();
                User user = UserService.Instance.getUserBySSOId(userId, session);
                criteria.developers.add(user);
            }

            OSType osType = preProcess_OSTYPE();
            criteria.osType = new ArrayList<>();
            criteria.osType.add(osType);

            criteria.setDeleted(false);

            List<AppService.AppSearchResultModel> resultObjects = AppService.Instance.list(criteria, from, count, "app.id", false, session);

            if (resultObjects != null && !resultObjects.isEmpty()) {
                Stream<ProductVO> productVOStream =
                        resultObjects.stream().map(appSearchResultModel -> {
                            try {
                                return ProductVO.buildProductVO(appSearchResultModel);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                return productVOStream.collect(Collectors.<ProductVO>toList());
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getOutputStream().write(ErrorPhrases.NO_APP_FOUND.getMessage().getBytes("UTF-8"));
            }
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }

    @RequestMapping(value = "/getVersionList", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<AppPackageService.VersionInfo> getVersionList(String packageName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = HibernateUtil.getNewSession();
        try {
            OSType osType = preProcess_OSTYPE();

            List<AppPackageService.VersionInfo> versionInfos = new ArrayList<>();
            if (packageName != null && !packageName.trim().equals("")) {
                versionInfos = AppPackageService.Instance.getVersionsForApp(packageName.trim(), osType, session);
            }
            return versionInfos;

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getProduct", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ProductVO getProduct(@RequestParam(required = false) String versionCode, @RequestParam(required = true) String packageName,
                                HttpServletRequest request, HttpServletResponse response) throws IOException {
        AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        criteria.publishStates = new ArrayList<>();
        if (versionCode == null || versionCode.trim().equals("")) {
            criteria.publishStates.add(PublishState.PUBLISHED);
        } else {
            criteria.versionCode = versionCode.trim();
        }
        Session session = HibernateUtil.getNewSession();
        try {
            OSType osType = preProcess_OSTYPE();

            if (packageName != null && !packageName.trim().equals("")) {
                criteria.packageName = packageName.trim();
            }
            criteria.osType = new ArrayList<>();
            criteria.osType.add(osType);

            List<AppService.AppSearchResultModel> resultObjects = null;

            criteria.setDeleted(false);
            if (versionCode == null)
                resultObjects = AppService.Instance.list(criteria, 0, -1, "app.id", false, session);
            else
                resultObjects = AppService.Instance.searchByPackages(criteria, session);

            if (resultObjects != null && !resultObjects.isEmpty()) {
                AppService.AppSearchResultModel appSearchResultModel = resultObjects.get(0);

                return ProductVO.buildProductVO(appSearchResultModel);
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getOutputStream().write(ErrorPhrases.NO_APP_WITH_GIVEN_PACKAGENAME_EXISTS.getMessage().getBytes("UTF-8"));
            }
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }

    @RequestMapping(value = "/getChangeLog", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ChangeLogVO getChangeLog(@RequestParam(required = true) String packageName, @RequestParam(required = true) String versionCode, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = HibernateUtil.getNewSession();
        if (versionCode == null || versionCode.trim().isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getOutputStream().write(ErrorPhrases.VERSIONCODE_IS_REQUIRED.getMessage().getBytes("UTF-8"));
        } else if (packageName == null || packageName.trim().isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getOutputStream().write(ErrorPhrases.PACKAGENAME_IS_REQUIRED.getMessage().getBytes("UTF-8"));
        } else {
            try {
                OSType osType = preProcess_OSTYPE();
                AppPackage loadedAppPackage = AppPackageService.Instance.getPackage(osType, packageName.trim(), versionCode.trim(), session);
                if (loadedAppPackage == null) {
                    String message = AppStorePropertyReader.getString("app.package.not.found.with.this.name.and.code");
                    message = message.replace("${packageName}", packageName);
                    message = message.replace("${versionCode}", versionCode);
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.getOutputStream().write(message.getBytes("UTF-8"));
                } else {
                    ChangeLogVO changeLogVO = new ChangeLogVO();
                    changeLogVO.setAppPackageChangeLog(loadedAppPackage.getChangeLog());
                    return changeLogVO;
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), e);
            } finally {
                session.close();
            }
        }
        return null;
    }


    @RequestMapping(value = "/getImageGalleries", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ImageGalleryVO> getImageGalleries(@RequestParam(required = false) String versionCode, @RequestParam String packageName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        criteria.publishStates = new ArrayList<>();
        String versionCodeStr = null;
        String packageNameStr = null;

        if (versionCode == null || versionCode.trim().endsWith("")) {
            criteria.publishStates.add(PublishState.PUBLISHED);
        } else {
            versionCodeStr = versionCode.trim();
        }
        if (packageName != null && !packageName.trim().equals("")) {
            packageNameStr = packageName.trim();
        }
        Session session = HibernateUtil.getNewSession();
        List<ImageGalleryVO> galleryVOs = new ArrayList<>();
        try {
            OSType osType = preProcess_OSTYPE();

            criteria.packageName = packageNameStr;


            AppPackage appPackage = null;

            if (versionCodeStr == null)
                appPackage = AppPackageService.Instance.getMainPackage(packageNameStr, session);
            else {
                appPackage = AppPackageService.Instance.getPackage(packageNameStr, versionCodeStr, session);
            }


            if (appPackage != null) {
                AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
                appSearchCriteria.packageName = packageNameStr;
                appSearchCriteria.osType = new ArrayList<>();
                appSearchCriteria.osType.add(osType);


                List<com.fanap.midhco.appstore.entities.File> thumbFiles = appPackage.getThumbImages();
                if (thumbFiles != null && !thumbFiles.isEmpty()) {
                    Stream<ImageGalleryVO> galleryVOStream =
                            thumbFiles.stream().map(thumbFile -> {
                                ImageGalleryVO imageGalleryVO = new ImageGalleryVO();

                                String hostAddress = null;
                                try {
                                    hostAddress = AppUtils.getHostName();
                                } catch (UnknownHostException e) {
                                    throw new AppStoreRuntimeException(e);
                                }
                                int port = request.getServerPort();
                                String context = ConfigUtil.getProperty(ConfigUtil.APP_WEB_CONTEXT);
                                String webServiceBasePath = ConfigUtil.getProperty(ConfigUtil.APP_RESTAPI_BASEPATH);

                                String path = ConfigUtil.getProperty(ConfigUtil.FILE_SERVER_OUTGOING_DOWNLOAD_PATH);
                                path = path.replace("${key}", thumbFile.getFilePath());
                                imageGalleryVO.setImageUrl(path);

                                try {
                                    hostAddress = AppUtils.getHostName();
                                } catch (UnknownHostException e) {
                                    throw new AppStoreRuntimeException(e);
                                }

                                String baseServiceURL = ConfigUtil.getProperty(ConfigUtil.APPLICATION_PATH) + webServiceBasePath;

                                String basePath = baseServiceURL + "getFile?path=";

                                try {
                                    String thumbUrl = AppUtils.getImageThumbNail(thumbFile.getFilePath(), thumbFile.getFileName());
                                    thumbUrl = basePath + "File:" + thumbUrl;//.replace("\\", "/");
                                    String thumbPath = StringEscapeUtils.escapeHtml4(thumbUrl);
                                    imageGalleryVO.setThumbnailURL(thumbPath);
                                } catch (Exception ex) {
                                    logger.error("Error generating thumb file URL ", ex);
                                    imageGalleryVO.setThumbnailURL("");
                                }

                                imageGalleryVO.setProduct_packageName(criteria.packageName);
                                return imageGalleryVO;
                            });

                    return galleryVOStream.collect(Collectors.<ImageGalleryVO>toList());
                }
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getOutputStream().write(ErrorPhrases.NO_APP_WITH_GIVEN_PACKAGENAME_EXISTS.getMessage().getBytes());
            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }

        return galleryVOs;
    }

    @RequestMapping(value = "/getProductDescription", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ProductDetailInfoVO getProductDescription(@RequestParam(required = false) String versionCode, @RequestParam(required = true) String packageName,
                                                     HttpServletResponse response) {
        Session session = HibernateUtil.getNewSession();
        try {
            OSType osType = preProcess_OSTYPE();
            String versionCodeStr = null;
            String packageNameStr = null;
            AppService.AppSearchCriteria searchCriteria = new AppService.AppSearchCriteria();
            searchCriteria.publishStates = new ArrayList<>();

            if (versionCode == null || versionCode.trim().endsWith("")) {
                searchCriteria.publishStates.add(PublishState.PUBLISHED);
            } else {
                versionCodeStr = versionCode.trim();
            }
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            searchCriteria.packageName = packageNameStr;

            searchCriteria.osType = new ArrayList<>();
            searchCriteria.osType.add(osType);

            List<AppService.AppSearchResultModel> searchResultModels = null;

            searchCriteria.setDeleted(false);
            if (versionCodeStr == null)
                searchResultModels =
                        AppService.Instance.list(searchCriteria, 0, -1, null, true, session);
            else
                searchResultModels =
                        AppService.Instance.searchByPackages(searchCriteria, session);

            if (searchResultModels != null && !searchResultModels.isEmpty()) {
                AppService.AppSearchResultModel searchResultModel = searchResultModels.get(0);
                ProductDetailInfoVO productDetailInfoVO = new ProductDetailInfoVO();
                String appDescription = AppService.Instance.getAppDescription(searchResultModel.getPackageName(), osType, session);
                productDetailInfoVO.setDescription(appDescription);
                productDetailInfoVO.setId_product(packageName);
                return productDetailInfoVO;
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                if (ErrorPhrases.NO_APP_WITH_GIVEN_PACKAGENAME_EXISTS.getMessage() != null) {
                    response.getOutputStream().write(ErrorPhrases.NO_APP_WITH_GIVEN_PACKAGENAME_EXISTS.getMessage().getBytes());
                }
            }
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }

    @RequestMapping(value = "/getCommentCountByAppPackageName", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Integer getCommentCountByAppPackageName(
            @RequestParam(required = true) String packageName,
            HttpServletResponse response,
            HttpServletRequest request) {

        Session session = null;
        Integer commentCount = 0;
        try {
            OSType osType = preProcess_OSTYPE();
            session = HibernateUtil.getCurrentSession();
            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);

            CommentService.CommentVOCriteria commentVOCriteria = null;
            if (app != null) {
                commentVOCriteria = new CommentService.CommentVOCriteria();
                commentVOCriteria.appId = app.getId();
                commentVOCriteria.approved = true;
                commentCount = CommentService.Instance.getCommentsCountForApp(commentVOCriteria).intValue();
            }


            return commentCount;
        } catch (Exception ex) {
            logger.error("error occured in getCommentCountByAppPackageName ", ex);
            throw new RuntimeException(ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/getCommentsByPackageName", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getCommentsByPackageName(
            @RequestParam(required = true) String packageName,
            @RequestParam(required = false) Integer from,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String jwtToken,
            @RequestParam(required = false) Boolean isApproved,
            HttpServletResponse response,
            HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            JWTService.JWTUserClass jwtUser = null;
            if (jwtToken != null) {
                jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }

            }

            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();
            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);
            responseVO = checkApp(app);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL))
                return responseVO;
            if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                int fromInt = (from == null) ? 0 : from;
                int toInt = (count == null) ? 10 : count;

                toInt = toInt - fromInt;

                CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                commentVOCriteria.appId = app.getId();
                if (isApproved == null) {
                    commentVOCriteria.approved = true;
                } else {
                    commentVOCriteria.approved = isApproved;
                }

                List<CommentService.ElasticCommentVO> elasticCommentVOList = CommentService.Instance.getCommentsForApp(commentVOCriteria, fromInt, toInt, "lastModifyDate", false);

                List<String> parentCommentIdList = new ArrayList<>();
                Map<String, CommentService.ElasticCommentVO> elasticCommentLikeVOMap = new HashMap<>();

                for (CommentService.ElasticCommentVO elasticCommentVO : elasticCommentVOList) {
                    if (elasticCommentVO.getId() != null) {
                        parentCommentIdList.add(elasticCommentVO.getId());
                    }

                    elasticCommentLikeVOMap.put(elasticCommentVO.getId(), elasticCommentVO);
                }

                if (!parentCommentIdList.isEmpty()) {
                    Map<String, CommentService.LikeResultClass> comment2LikeCountMap = CommentService.Instance.getLikeCountForCommentList(parentCommentIdList,
                            jwtUser != null ? jwtUser.getUserId() : null);
                    Map<String, CommentService.DisLikeResultClass> comment2DisLikeCountMap = CommentService.Instance.getDisLikeCategoryCountForCommentList(parentCommentIdList,
                            jwtUser != null ? jwtUser.getUserId() : null);

                    for (String key : comment2LikeCountMap.keySet()) {
                        CommentService.ElasticCommentVO tempElasticCommentVO = elasticCommentLikeVOMap.get(key);
                        tempElasticCommentVO.setLikeCount(comment2LikeCountMap.get(key).count);
                        tempElasticCommentVO.setHasUserLiked(comment2LikeCountMap.get(key).likedByUser);
                    }

                    for (String key : comment2DisLikeCountMap.keySet()) {
                        CommentService.ElasticCommentVO tempElasticCommentVO = elasticCommentLikeVOMap.get(key);
                        tempElasticCommentVO.setDislikeCat2Count(comment2DisLikeCountMap.get(key).disLikeCategory2NumberMap);
                        tempElasticCommentVO.setHasUserDisLiked(comment2DisLikeCountMap.get(key).disLikedByUser);
                    }
                }

                String resultString = JsonUtil.getJson(elasticCommentVOList);

                responseVO.setResult(resultString);

                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            } else {
                return responseVO;
            }
        } catch (Exception e) {
            logger.error("error occured in getCommentsByPackageName ", e);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/getCommentsForApproved", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getCommentsForApproved(
            @RequestParam(required = true) String packageName,
            @RequestParam(required = false) Integer from,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String jwtToken,
            HttpServletResponse response,
            HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            responseVO = getCommentsByPackageName(packageName, from, count, jwtToken, false, response, request);
        } catch (Exception e) {
            logger.error("error occured in getCommentsForApproved ", e);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/insertCommentForAppMainPackage", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO insertCommentForAppMainPackage
            (
                    @RequestParam(required = true, value = "commentRateInsertVo") CommentRateInsertVo commentRateInsertVo
                    , @RequestParam(required = true) String jwtToken,
                    HttpServletResponse response,
                    HttpServletRequest request) throws Exception {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }

            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();
            String packageNameStr = null;
            if (commentRateInsertVo.getPackageName() != null && !commentRateInsertVo.getPackageName().trim().equals("")) {
                packageNameStr = commentRateInsertVo.getPackageName().trim();
            }
            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);
            responseVO = checkApp(app);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL))
                return responseVO;
            if (commentRateInsertVo != null) {
                if (commentRateInsertVo.deviceId == null || commentRateInsertVo.deviceId.trim().equals("")) {
                    responseVO.setResultStatus(ResultStatus.DEVICE_ID_IS_NULL);
                    responseVO.setResult(ResultStatus.DEVICE_ID_IS_NULL.toString());
                } else if (commentRateInsertVo.ratingIndex == null) {
                    responseVO.setResultStatus(ResultStatus.RATING_INDEX_IS_NULL);
                    responseVO.setResult(ResultStatus.RATING_INDEX_IS_NULL.toString());
                } else if (commentRateInsertVo.getRatingIndex() > 5 || commentRateInsertVo.getRatingIndex() < 0) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                } else if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    RatingIndex ratingIndex = new RatingIndex(Integer.valueOf(commentRateInsertVo.ratingIndex));

                    CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                    String idStr = null;
                    if (commentRateInsertVo.commentRateId != null && !commentRateInsertVo.commentRateId.trim().equals("")) {
                        idStr = commentRateInsertVo.commentRateId.trim();
                    }
                    commentVOCriteria.id = idStr;
                    List<CommentService.ElasticCommentVO> commentVOList = CommentService.Instance.getCommentsForApp(commentVOCriteria, 0, 1, null, true);
                    if (commentVOList != null && !commentVOList.isEmpty()) {
                        CommentService.ElasticCommentVO elasticCommentVO = commentVOList.get(0);
                        Long userId = elasticCommentVO.getUserId();
                        if (!userId.equals(jwtUser.getUserId())) {
                            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                            responseVO.setResult("userId not equals to original userId!");
                        }
                    }
//todo use this part insteed that part
//                    CommentService.ElasticCommentVO elasticCommentVO = CommentService.Instance.buildCommentVOForElastic(
//                            commentRateInsertVo.commentRateId, app, commentRateInsertVo.deviceId, commentRateInsertVo.commentText,
//                            commentRateInsertVo.language, new Date().getTime(), Long.valueOf(jwtUser.getUserId()), ratingIndex, false);


                    CommentService.ElasticCommentVO elasticCommentVO = CommentService.Instance.buildCommentVOForElastic(
                            commentRateInsertVo.commentRateId, app, commentRateInsertVo.deviceId, commentRateInsertVo.commentText,
                            commentRateInsertVo.language, new Date().getTime(), Long.valueOf(jwtUser.getUserId()), ratingIndex, false);

                    if (elasticCommentVO == null) {
                        responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                        responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                        return responseVO;
                    }

                    if (commentRateInsertVo.getCommentRateId() != null && !commentRateInsertVo.getCommentRateId().trim().equals("")) {
                        elasticCommentVO.setId(commentRateInsertVo.getCommentRateId());
                    }
                    JestResult jestResult = CommentService.Instance.insertCommentForAppMainPackage(elasticCommentVO);
                    if (jestResult.isSucceeded()) {
                        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                        JsonObject jsonObject = jestResult.getJsonObject();
                        if (jsonObject.has("_id")) {
                            elasticCommentVO.setId(jsonObject.get("_id").toString());
                        }

                        responseVO.setResult(JsonUtil.getJson(elasticCommentVO));

                    } else {
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                        responseVO.setResult(jestResult.getErrorMessage());
                    }
                    return responseVO;
                } else {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(responseVO.getResult());
                }
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            logger.error("Error occured in insert comment ", e);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }

    @RequestMapping(value = "/getLikeUnLikeCountByCommentId", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getLikeUnLikeCountByCommentId(@RequestParam(required = true) String parentCommentId
            , @RequestParam(required = true) boolean getLike
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        try {
            if (parentCommentId != null && !parentCommentId.trim().equals("")) {
                CommentService.LikeVOCriteria likeVOCriteria = new CommentService.LikeVOCriteria();
                likeVOCriteria.like = getLike;
                likeVOCriteria.parentCommentId = parentCommentId;

                Integer countLik = CommentService.Instance.getLikesCountForComment(likeVOCriteria).intValue();

                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                responseVO.setResult(countLik.toString());

                return responseVO;
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        }

        return responseVO;
    }


    @RequestMapping(value = "/getLikeUnLikeByCommentId", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getLikeUnLikeByCommentId(@RequestParam(required = true) String parentCommentId
            , @RequestParam(required = true) boolean getLike
            , @RequestParam(required = false) Integer from
            , @RequestParam(required = false) Integer count
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        try {
            if (parentCommentId != null && !parentCommentId.trim().equals("")) {

                int fromInt = (from == null ? 0 : Integer.valueOf(from));
                int countInt = (count == null ? 10 : Integer.valueOf(count));

                countInt = countInt - fromInt;

                CommentService.LikeVOCriteria likeVOCriteria = new CommentService.LikeVOCriteria();
                likeVOCriteria.like = getLike;
                likeVOCriteria.parentCommentId = parentCommentId;

                List<CommentService.ElasticCommentLikeVO> elasticCommentLikeVOs =
                        CommentService.Instance.getLikesForComment(likeVOCriteria, fromInt, countInt, false);

                String resultString = JsonUtil.getJson(elasticCommentLikeVOs);

                responseVO.setResult(resultString);

                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                return responseVO;
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        }

        return responseVO;
    }

    @RequestMapping(value = "/unLikeComment", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO unLikeComment(
            @RequestParam(required = true) String parentCommentId,
            @RequestParam(required = true) String jwtToken
    ) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {

            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            if (parentCommentId != null && !parentCommentId.trim().equals("")) {


                JestResult jestResult = CommentService.Instance.deleteCommentLike(parentCommentId, Long.parseLong(jwtUser.getUserId()));
                if (jestResult.isSucceeded()) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    JsonObject jsonObject = jestResult.getJsonObject();
                    responseVO.setResult(jsonObject.toString());

                } else {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
                }

                return responseVO;
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.clear();
            }
        }

        return responseVO;
    }

    @RequestMapping(value = "/setDisLikeForCommentId", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO setDisLikeForCommentId(
            @RequestParam(required = true) String parentCommentId,
            @RequestParam(required = true) Integer disLikeCategory,
            @RequestParam(required = true) String jwtToken
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {

            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            if (parentCommentId != null && !parentCommentId.trim().equals("")) {

                CommentService.ElasticCommentLikeVO elasticCommentLikeVO =
                        CommentService.Instance.buildLikeVOForElastic(false, Long.valueOf(jwtUser.getUserId()), parentCommentId);
                if (elasticCommentLikeVO == null) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }

                if (!(disLikeCategory.equals(0) || disLikeCategory.equals(1) || disLikeCategory.equals(2))) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult("invalid dislike category value!");
                }
                elasticCommentLikeVO.setDislikeCategory(disLikeCategory);

                CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                commentVOCriteria.id = parentCommentId;
                Integer commentCount = CommentService.Instance.getCommentsCountForApp(commentVOCriteria).intValue();
                if (commentCount == 0) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult("No comment exists with id " + parentCommentId);
                    return responseVO;
                }

                elasticCommentLikeVO.setModifyDateTime(new Date().getTime());
                JestResult jestResult = CommentService.Instance.insertLikeForComment(elasticCommentLikeVO);
                if (jestResult.isSucceeded()) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    JsonObject jsonObject = jestResult.getJsonObject();
                    responseVO.setResult(jsonObject.toString());

                } else {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
                }
                return responseVO;
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    @RequestMapping(value = "/setLikeForCommentId", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO setLikeForCommentId(@RequestParam(required = true) String parentCommentId
            , @RequestParam(required = true) String jwtToken
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {

            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            if (parentCommentId != null && !parentCommentId.trim().equals("")) {

                CommentService.ElasticCommentLikeVO elasticCommentLikeVO =
                        CommentService.Instance.buildLikeVOForElastic(true, Long.valueOf(jwtUser.getUserId()), parentCommentId);

                if (elasticCommentLikeVO == null) {
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    return responseVO;
                }
                CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
                commentVOCriteria.id = parentCommentId;
                Integer commentCount = CommentService.Instance.getCommentsCountForApp(commentVOCriteria).intValue();
                if (commentCount == 0) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult("No comment exists with id " + parentCommentId);
                    return responseVO;
                }

                elasticCommentLikeVO.setModifyDateTime(new Date().getTime());

                JestResult jestResult = CommentService.Instance.insertLikeForComment(elasticCommentLikeVO);
                if (jestResult.isSucceeded()) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    JsonObject jsonObject = jestResult.getJsonObject();
                    responseVO.setResult(jsonObject.toString());

                } else {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
                }
                return responseVO;
            } else {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(AppStorePropertyReader.getString("label.operation.unsuccessfully"));
            }
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    @RequestMapping(value = "/getLikeUnLikeComment", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getLikeUnLikeComment(@RequestParam(required = true) Boolean getLike
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();


        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            User requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            CommentService.LikeVOCriteria likeVOCriteria = new CommentService.LikeVOCriteria();
            likeVOCriteria.like = getLike;
            likeVOCriteria.userId = requesterUser.getUserId();

            List<CommentService.ElasticCommentLikeVO> elasticCommentLikeVOs =
                    CommentService.Instance.getLikesForComment(likeVOCriteria, fromIndex, countIndex, false);

            String resultString = JsonUtil.getJson(elasticCommentLikeVOs);

            responseVO.setResult(resultString);

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return responseVO;

        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        }
    }

    private ResponseVO checkApp(App app) {
        ResponseVO responseVO = new ResponseVO();
        boolean invalidApp = false;
        if (app == null) {
            invalidApp = true;
            responseVO.setResult(AppStorePropertyReader.getString("error.app.notFound"));
        }

        if (invalidApp) {
            responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
            return responseVO;
        }
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        return responseVO;
    }

    @RequestMapping(value = "/getFile", method = RequestMethod.GET)
    public void getFile(@RequestParam(required = true) String path, HttpServletResponse response) {
        InputStream is = null;
        try {
            if (!path.contains("temp")) {
                return;
            }
            InputStream in = new URL(path).openStream();
            logger.debug("after URL(path).... ");
            org.apache.commons.io.IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            logger.error("Error writing file to output stream. Filename was '" + path + "'", ex);
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Error closing File!");
                }
        }
    }

    @RequestMapping(value = "/getUpdates", headers = "Accept=*/*", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getProductUpdates(@RequestParam(value = "requestVOs") ProductRequestVOs productRequestVOs, HttpServletRequest request) {
        Session session = HibernateUtil.getNewSession();
        List<ProductVO> productVOList = new ArrayList<>();
        try {
            OSType osType = preProcess_OSTYPE();

            Map<String, String> packageName2VersionCodeMap = new HashMap<>();

            List<ProductRequestVO> requestVOs = productRequestVOs.getProductRequestVOList();

            for (ProductRequestVO requestVO : requestVOs) {
                String client_packageName = requestVO.getPackageName();
                String client_versionCode = requestVO.getVersionCode();
                packageName2VersionCodeMap.put(client_packageName, client_versionCode);
            }

            List<AppService.AppSearchResultModel> searchResultModels =
                    AppService.Instance.getUpdatableProducts(packageName2VersionCodeMap, osType, session);

            if (searchResultModels != null && !searchResultModels.isEmpty()) {
                Stream<ProductVO> productVOStream =
                        searchResultModels.stream().map(appSearchResultModel -> {
                            try {
                                return ProductVO.buildProductVO(appSearchResultModel);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                return productVOStream.collect(Collectors.<ProductVO>toList());
            }
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }

        return productVOList;
    }


    @RequestMapping(value = "/checkFileExistenceInPackage", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO checkFileExistenceInPackage(
            @RequestParam(required = true) String packageName,
            @RequestParam(required = true) String versionCode,
            @RequestParam(required = true) String fileName,
            HttpServletRequest request) throws Exception {
        ResponseVO responseVO = new ResponseVO();

        try {
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
            } catch (Exception e) {
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                return responseVO;
            }

            String versionCodeStr = null;
            String packageNameStr = null;
            String fileNameStr = null;

            if (versionCode != null && !versionCode.trim().endsWith("")) {
                versionCodeStr = versionCode.trim();
            }
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            if (fileName != null && !fileName.trim().equals("")) {
                fileNameStr = fileName.trim();
            }
            responseVO = AppPackageService.Instance.checkFileExistenceInPackage(osType, packageNameStr, versionCodeStr, fileNameStr);

        } catch (Exception ex) {
            responseVO.setResult(ex.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }
        return responseVO;

    }


    @RequestMapping(value = "/getDeltaUpdatePackage", headers = "Accept=*/*", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseBody
    public DeltaPackageVO getProductUpdates(@RequestParam(value = "packageName") String packageName,
                                            @RequestParam(value = "previousVersion") String previousVersion,
                                            @RequestParam(value = "forwardVersion") String forwardVersion,
                                            HttpServletRequest request) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            String previousVersionStr = null;
            String forwardVersionStr = null;
            String packageNameStr = null;

            if (previousVersion != null && !previousVersion.trim().equals("")) {
                previousVersionStr = previousVersion.trim();
            }

            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }

            if (forwardVersion != null && !forwardVersion.trim().equals("")) {
                forwardVersionStr = forwardVersion.trim();
            }

            String deltaPackageURL =
                    AppPackageService.Instance.getDeltaPackageDownloadURL(osType, packageNameStr, previousVersionStr, forwardVersionStr, session);

            DeltaPackageVO deltaPackageVO = new DeltaPackageVO();
            deltaPackageVO.setDeltaPackDownloadURL(deltaPackageURL);
            return deltaPackageVO;

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getCategories", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<CategoryVO> getCategories(@RequestParam(required = false) String from, @RequestParam(required = false) String count,
                                          @RequestParam(required = false) String categoryName, @RequestParam(required = false) Long categoryId, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            AppCategoryService.AppCategoryCriteria criteria = new AppCategoryService.AppCategoryCriteria();
            if (categoryName != null && !categoryName.trim().equals("")) {
                criteria.setCategoryName(categoryName.trim());
            }
            if (categoryId != null) {
                criteria.setId(categoryId);
            }
            criteria.setAssignable(true);
            criteria.setEnabled(true);
            int fromInt = (from == null || from.trim().equals("") ? 0 : Integer.valueOf(from));
            int countInt = (count == null || count.trim().equals("") ? 10 : Integer.valueOf(count));
            List<AppCategory> appCategoryList = AppCategoryService.Instance.list(criteria, fromInt, countInt, "ent.id", false, session);


            if (appCategoryList != null && !appCategoryList.isEmpty()) {
                Stream<CategoryVO> categoryVOStream = appCategoryList.stream().map(appCategory -> CategoryVO.buildCategoryVO(appCategory));
                return categoryVOStream.collect(Collectors.<CategoryVO>toList());

            } else {
                return new ArrayList<CategoryVO>();
            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getCountCategories", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountCategories(@RequestParam(required = false) String categoryName, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            AppCategoryService.AppCategoryCriteria criteria = new AppCategoryService.AppCategoryCriteria();
            if (categoryName != null && !categoryName.trim().equals("")) {
                criteria.setCategoryName(categoryName.trim());
            }
            criteria.setAssignable(true);
            criteria.setEnabled(true);
            Long catCount = AppCategoryService.Instance.count(criteria, session);
            return catCount;
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getAppByCategory", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppByCategory(@RequestParam(required = true) String appCatId, @RequestParam(required = false) String from, @RequestParam(required = false) String count,
                                            HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            AppCategory appCategory = null;
            if (appCatId != null && !appCatId.trim().equals("")) {
                appCategory = (AppCategory) session.get(AppCategory.class, Long.valueOf(appCatId));
            }
            if (appCategory != null) {
                criteria.osType = osTypeList;
                criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);
                List<AppCategory> appCategoryList = new ArrayList<>();
                appCategoryList.add(appCategory);
                criteria.appCategory = appCategoryList;

                criteria.setDeleted(false);
                return getProductVOS(from, count, request, session, criteria);

            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getCountAppByCategory", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountAppByCategory(@RequestParam(required = true) String appCatId, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            AppCategory appCategory = null;
            if (appCatId != null && !appCatId.trim().equals("")) {
                appCategory = (AppCategory) session.get(AppCategory.class, Long.valueOf(appCatId));
            }
            if (appCategory != null) {
                criteria.osType = osTypeList;
                criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);
                List<AppCategory> appCategoryList = new ArrayList<>();
                appCategoryList.add(appCategory);
                criteria.appCategory = appCategoryList;

                criteria.setDeleted(false);
                return AppService.Instance.count(criteria, session);

            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }

    @RequestMapping(value = "/getAppByCategoryNameInAllOsTypes", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppByCategoryNameInAllOsTypes(@RequestParam(required = true) String categoryName, @RequestParam(required = false) String from,
                                                            @RequestParam(required = false) String count, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {

            AppCategoryService.AppCategoryCriteria categoryCriteria = new AppCategoryService.AppCategoryCriteria();
            if (categoryName != null && !categoryName.trim().equals("")) {
                categoryCriteria.setCategoryName(categoryName.trim());
            }

            List<AppCategory> appCategoryList = AppCategoryService.Instance.list(categoryCriteria, 0, -1, "id", false, session);

            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();

            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);
            if (appCategoryList != null && !appCategoryList.isEmpty()) {
                List<AppCategory> tmpAppCategoryList = new ArrayList<>();
                tmpAppCategoryList.add(appCategoryList.get(0));
                criteria.appCategory = appCategoryList;

                criteria.setDeleted(false);
                return getProductVOS(from, count, request, session, criteria);

            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }

    @RequestMapping(value = "/getCountAppByCategoryNameInAllOsTypes", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountAppByCategoryNameInAllOsTypes(@RequestParam(required = true) String categoryName, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {

            AppCategoryService.AppCategoryCriteria categoryCriteria = new AppCategoryService.AppCategoryCriteria();
            if (categoryName != null && !categoryName.trim().equals("")) {
                categoryCriteria.setCategoryName(categoryName.trim());
            }

            List<AppCategory> appCategoryList = AppCategoryService.Instance.list(categoryCriteria, 0, -1, "id", false, session);

            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();

            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);
            if (appCategoryList != null && !appCategoryList.isEmpty()) {
                List<AppCategory> tmpAppCategoryList = new ArrayList<>();
                tmpAppCategoryList.add(appCategoryList.get(0));
                criteria.appCategory = appCategoryList;

                criteria.setDeleted(false);
                return AppService.Instance.count(criteria, session);

            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getAppByAppTitle", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppByAppTitle(@RequestParam(required = true) String appTitle, @RequestParam(required = false) String from, @RequestParam(required = false) String count,
                                            HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            if (appTitle != null && !appTitle.trim().equals("")) {
                criteria.title = appTitle.trim();
            }
            criteria.osType = osTypeList;
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            criteria.setDeleted(false);
            if (criteria != null) {
                return getProductVOS(from, count, request, session, criteria);
            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getCountAppByAppTitle", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountAppByAppTitle(@RequestParam(required = true) String appTitle, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            if (appTitle != null && !appTitle.trim().equals("")) {
                criteria.title = appTitle.trim();
            }
            criteria.osType = osTypeList;
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            criteria.setDeleted(false);
            if (criteria != null) {
                return AppService.Instance.count(criteria, session);
            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getAppByAppTitleInAllOsTypes", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppByAppTitleInAllOsTypes(@RequestParam(required = true) String appTitle, @RequestParam(required = false) String from,
                                                        @RequestParam(required = false) String count, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {

            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            if (appTitle != null && !appTitle.trim().equals("")) {
                criteria.title = appTitle.trim();
            }
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            criteria.setDeleted(false);
            return getProductVOS(from, count, request, session, criteria);


        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getCountAppByAppTitleInAllOsTypes", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountAppByAppTitleInAllOsTypes(@RequestParam(required = true) String appTitle, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {

            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            if (appTitle != null && !appTitle.trim().equals("")) {
                criteria.title = appTitle.trim();
            }
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            criteria.setDeleted(false);
            return AppService.Instance.count(criteria, session);


        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    private List<ProductVO> getProductVOS(@RequestParam(required = false) String from, @RequestParam(required = false) String count, HttpServletRequest request, Session session, AppService.AppSearchCriteria criteria) throws Exception {
        int fromInt = (from == null || from.trim().equals("") ? 0 : Integer.valueOf(from));
        int countInt = (count == null || count.trim().equals("") ? 10 : Integer.valueOf(count));

        List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromInt, countInt, "app.id", false, session);
        return setAndReturnProductVOs(request, appSearchResultModelList);
    }


    @RequestMapping(value = "/getApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getApp(@RequestParam(required = false) AppSearchVO appSearchVO, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();

        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();

            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = null;
            if (appSearchVO != null) {
                criteria = AppSearchVO.createCriteriaFromJson(appSearchVO, session);
            } else {
                criteria = new AppService.AppSearchCriteria();
            }
            criteria.osType = osTypeList;
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromIndex, countIndex, "app.id", false, session);

            return setAndReturnProductVOs(request, appSearchResultModelList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getAppInAllOsType", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppInAllOsType(@RequestParam(required = false) AppSearchVO appSearchVO, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();

        try {
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();

            AppService.AppSearchCriteria criteria = null;
            if (appSearchVO != null) {
                criteria = AppSearchVO.createCriteriaFromJson(appSearchVO, session);
            } else {
                criteria = new AppService.AppSearchCriteria();
            }
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromIndex, countIndex, "app.id", false, session);

            return setAndReturnProductVOs(request, appSearchResultModelList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getCountApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountApp(@RequestParam(required = false) AppSearchVO appSearchVO, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = null;
            if (appSearchVO != null) {
                criteria = AppSearchVO.createCriteriaFromJson(appSearchVO, session);
            } else {
                criteria = new AppService.AppSearchCriteria();
            }
            criteria.osType = osTypeList;
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            Long appCount = AppService.Instance.count(criteria, session);
            return appCount;

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getTopApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getTopApp(@RequestParam(required = true) Integer from,
                                     @RequestParam(required = true) Integer size,
                                     HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();

        try {
            OSType osType = preProcess_OSTYPE();
            List<ProductVO> productVOList = new ArrayList<>();
            List<Long> publishedAppId = new ArrayList<>();
            int counter = 0;

            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            criteria.publishStates = new ArrayList<>();
            criteria.publishStates.add(PublishState.PUBLISHED);
            criteria.setDeleted(false);
            List<AppService.AppSearchResultModel> resultObjects = AppService.Instance.list(criteria, 0, -1, "app.id", false, session);

            int allPublishedApp = resultObjects != null ? resultObjects.size() : 0;
            recursiveGetAppRate(from, size, session, osType, productVOList, publishedAppId, allPublishedApp);

            return productVOList;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
        } finally {
            session.close();
        }
    }

    private void recursiveGetAppRate(@RequestParam(required = true) Integer from, @RequestParam(required = true) Integer size, Session session, OSType osType, List<ProductVO> productVOList, List<Long> publishedAppId, int allPublishedApp) throws Exception {
        List<CommentService.TopRatedAppsVO> topRatedAppsVOs = CommentService.Instance.getTopRatedApps(osType.getId(), from, size, false);
        Integer unPublishedAppCounter = 0;
        for (CommentService.TopRatedAppsVO topRatedAppsVO : topRatedAppsVOs) {
            try {
                App app = (App) session.get(App.class, Long.valueOf(topRatedAppsVO.getAppId()));
                if (app != null && app.getDeleted() != null && !app.getDeleted()) {
                    if (app.getMainPackage().getPublishState().equals(PublishState.PUBLISHED)) {
                        if (!publishedAppId.contains(app.getId())) {
                            ProductVO productVO = ProductVO.buildProductVO(app, topRatedAppsVO.getAverageRateIndex());
                            productVOList.add(productVO);
                            publishedAppId.add(app.getId());
                        }
                    } else {
                        unPublishedAppCounter++;
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        if (!unPublishedAppCounter.equals(Integer.valueOf(0))) {
            if (from < allPublishedApp) {
                recursiveGetAppRate(from + size + 1, unPublishedAppCounter, session, osType, productVOList, publishedAppId, allPublishedApp);
            }
        }
    }


    @RequestMapping(value = "/getNewApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getNewApp(@RequestParam(required = false) Integer from,
                                     @RequestParam(required = false) Integer count,
                                     HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            criteria.osType = osTypeList;
            criteria.getNewApp = true;
            List<PublishState> publishStateArrayList = new ArrayList<>();
            publishStateArrayList.add(PublishState.PUBLISHED);
            criteria.publishStates = publishStateArrayList;
            int fromInt = (from == null ? 0 : from);
            int countInt = (count == null ? 10 : count);
            criteria.setDeleted(false);
            List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromInt, countInt, "app.creationDate", false, session);

            return setAndReturnProductVOs(request, appSearchResultModelList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getMenuForUser", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getMenuForUser(HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            boolean rootDeveloper = UserService.Instance.isUserRoot(requesterUser);

            byte[] allPermission = null;
            List<Integer> access = new ArrayList<>();
            if (rootDeveloper) {
                access.add(0);
            } else {
                UserService.Instance.populateUserPermissions(requesterUser);
                allPermission = requesterUser.getAllAllowedPermissions();
                access = AccessService.decode(allPermission);
            }
            if (access.contains(2) || access.contains(24) || access.contains(33) || access.contains(37) || access.contains(58) || access.contains(62)) {
                if (!access.contains(1)) {
                    access.add(1);
                }
            }

            responseVO.setResult(JsonUtil.getJson(access));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());


        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }

    @RequestMapping(value = "/getAllAppLauncher", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO getAllAppLauncher(@RequestParam(required = false) String osId, @RequestParam(required = false) String from,
                                        @RequestParam(required = false) String environmentId, @RequestParam(required = false) String orgId,
                                        @RequestParam(required = false) Boolean oldVersion, @RequestParam(required = false) Boolean is64Bit, @RequestParam(required = false) String count,
                                        HttpServletRequest request, HttpServletResponse response) {

        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        String senderUrl = request.getRemoteHost();
        //environmentId ="1001";
        Boolean useOldVersion = null;
        Boolean hasDefaultLauncher = false;
        useOldVersion = oldVersion == null ? false : oldVersion;
        try {
            if (environmentId != null && !environmentId.trim().equals("")) {
                envId = Long.valueOf(environmentId);
            }
            int fromInt = (from == null || from.trim().equals("") ? 0 : Integer.valueOf(from));
            int countInt = (count == null || count.trim().equals("") ? 50 : Integer.valueOf(count));
            HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
            if (osId != null && !osId.trim().equals("")) {
                List<Long> osIdList = new ArrayList<>();
                osIdList.add(Long.valueOf(osId.trim()));
                handlerAppCriteria.setOsIds(osIdList);
            }

            session = HibernateUtil.getCurrentSession();
            Organization organization = null;

            if (orgId != null && !orgId.trim().equals("")) {
                organization = (Organization) session.get(Organization.class, Long.valueOf(orgId));
                if (organization == null) {
                    responseVO.setResultStatus(ResultStatus.ORGANIZATION_NOT_FOUND);
                    responseVO.setResult(ResultStatus.ORGANIZATION_NOT_FOUND.toString());
                    return responseVO;
                }
                handlerAppCriteria.setOrganization(organization);
            } else {
                Organization defaultOrganization = OrgService.Instance.getDefaultOrganization(session);
                if (defaultOrganization != null) {
                    handlerAppCriteria.setOrganization(defaultOrganization);
                } else {
                    responseVO.setResultStatus(ResultStatus.ORGANIZATION_NOT_FOUND);
                    responseVO.setResult(ResultStatus.ORGANIZATION_NOT_FOUND.toString());
                    return responseVO;
                }
            }
            OSEnvironment osEnvironment = null;
            if (environmentId != null && !environmentId.trim().equals("") && !environmentId.trim().equals("null") && !useOldVersion) {
                osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, Long.valueOf(environmentId));
            } else {
                if (!useOldVersion) {
                    osEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
                }
            }

            if (osEnvironment != null) {
                handlerAppCriteria.setOsEnvironment(osEnvironment);
            }
            handlerAppCriteria.setActive(true);

            HandlerAppService.HandlerAppSearchResultModel defaultHandlerApp = null;

            List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModelList = HandlerAppService.Instance.list(handlerAppCriteria, fromInt, countInt, "handlerApp.versionCode", false, session);
            if (useOldVersion != null) {
                handlerAppCriteria.setDefault(true);
                List<HandlerAppService.HandlerAppSearchResultModel> defaultHandlerAppSearchResultModels = HandlerAppService.Instance.list(handlerAppCriteria, fromInt, countInt, "handlerApp.versionCode", false, session);
                if (defaultHandlerAppSearchResultModels != null && !defaultHandlerAppSearchResultModels.isEmpty()) {
                    defaultHandlerApp = defaultHandlerAppSearchResultModels.get(0);
                    if (is64Bit != null) {
                        if ((is64Bit && defaultHandlerApp.getTestFileHandlerAppKey() == null && defaultHandlerApp.getFileHandlerAppKey() == null) ||
                                (!is64Bit && defaultHandlerApp.getFileHandlerAppKey32Bit() == null && defaultHandlerApp.getTestFileHandlerAppKey32Bit() == null)) {
                            int i = 0;
                            while (hasDefaultLauncher == false && i < handlerAppSearchResultModelList.size()) {
                                HandlerAppService.HandlerAppSearchResultModel tmpDefaultHandlerApp = handlerAppSearchResultModelList.get(i);
                                if ((is64Bit && (tmpDefaultHandlerApp.getTestFileHandlerAppKey() != null || tmpDefaultHandlerApp.getFileHandlerAppKey() != null)) ||
                                        (!is64Bit && (tmpDefaultHandlerApp.getFileHandlerAppKey32Bit() != null || tmpDefaultHandlerApp.getTestFileHandlerAppKey32Bit() != null))) {
                                    defaultHandlerApp = tmpDefaultHandlerApp;
                                    hasDefaultLauncher = true;
                                }
                                i++;
                            }
                        }
                    } else {
                        hasDefaultLauncher = true;
                    }
                }
                if (!useOldVersion) {
                    if (defaultHandlerApp == null) {
                        if (handlerAppSearchResultModelList != null && !handlerAppSearchResultModelList.isEmpty()) {
                            int defaultHandlerIndex = 0;
                            Boolean needToSetHandlerAppToDefault = true;
                            while (defaultHandlerIndex < handlerAppSearchResultModelList.size() && needToSetHandlerAppToDefault) {
                                HandlerAppService.HandlerAppSearchResultModel tmpDefaultHandlerApp = handlerAppSearchResultModelList.get(defaultHandlerIndex);
                                if (is64Bit != null) {
                                    if ((is64Bit && (tmpDefaultHandlerApp.getTestFileHandlerAppKey() != null || tmpDefaultHandlerApp.getFileHandlerAppKey() != null)) ||
                                            (!is64Bit && (tmpDefaultHandlerApp.getFileHandlerAppKey32Bit() != null || tmpDefaultHandlerApp.getTestFileHandlerAppKey32Bit() != null))) {
                                        defaultHandlerApp = tmpDefaultHandlerApp;
                                        needToSetHandlerAppToDefault = false;
                                    }
                                } else {
                                    if ((tmpDefaultHandlerApp.getFileHandlerAppKey() != null && !tmpDefaultHandlerApp.getFileHandlerAppKey().trim().equals("")) ||
                                            (tmpDefaultHandlerApp.getFileHandlerAppKey32Bit() != null && tmpDefaultHandlerApp.getFileHandlerAppKey32Bit().trim().equals(""))) {
                                        defaultHandlerApp = tmpDefaultHandlerApp;
                                        needToSetHandlerAppToDefault = false;
                                    }
                                }
                                defaultHandlerIndex++;
                            }
                        } else {
                            defaultHandlerApp = null;
                        }
                    }
                    handlerAppSearchResultModelList.clear();
                    if (defaultHandlerApp != null) {
                        handlerAppSearchResultModelList.add(defaultHandlerApp);
                    }
                }


                /*
                //this part remove defaultHandlerApp from selcted Version
                else {
                    if (defaultHandlerApp != null && defaultHandlerApp.getOsEnvironment() != null && defaultHandlerApp.getOsEnvironment().equals(osEnvironment)) {
                        handlerAppSearchResultModelList.remove(defaultHandlerApp);
                    } else {
                       *//*
                        // todo use this lines after mohsen's new version for send envId on useOldVersion is true
                       if (environmentId != null && !environmentId.trim().equals("") && !environmentId.trim().equals("null")) {
                            osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, Long.valueOf(environmentId));
                        }*//*
                        if (envId != null) {
                            osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, envId);

                        } else {
                            osEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
                        }
                        if (handlerAppSearchResultModelList != null && !handlerAppSearchResultModelList.isEmpty()) {
                            if (hasDefaultLauncher) {
                                handlerAppSearchResultModelList.remove(defaultHandlerApp);
                            }else {
                                int defaultHandlerIndex = 0;
                                while (defaultHandlerIndex < handlerAppSearchResultModelList.size() && osEnvironment != null &&
                                        (
                                                handlerAppSearchResultModelList.get(defaultHandlerIndex).getOsEnvironment() == null || !handlerAppSearchResultModelList.get(defaultHandlerIndex).getOsEnvironment().equals(osEnvironment) ||
                                                        handlerAppSearchResultModelList.get(defaultHandlerIndex).getFileHandlerAppKey() == null || handlerAppSearchResultModelList.get(defaultHandlerIndex).getFileHandlerAppKey().trim().equals("")
                                        )
                                        ) {
                                    defaultHandlerIndex++;
                                }
                                if (defaultHandlerIndex < handlerAppSearchResultModelList.size()) {
                                    handlerAppSearchResultModelList.remove(handlerAppSearchResultModelList.get(defaultHandlerIndex));
                                }
                            }
                        }
                        osEnvironment = null;
                    }
                }*/


            }

            List<HandlerAppVO> handlerAppVos = new ArrayList<>();
            if (handlerAppSearchResultModelList != null && !handlerAppSearchResultModelList.isEmpty()) {
                for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                    if (handlerAppSearchResultModel != null) {
                        HandlerAppVO handlerAppVO = HandlerAppVO.buildHandlerAppVO(handlerAppSearchResultModel);
                        handlerAppVos.add(handlerAppVO);
                    }
                }
                //use for instead it
//                Stream<HandlerAppVO> handlerAppVOStream = handlerAppSearchResultModelList.stream().map(handlerAppSearchResult ->  HandlerAppVO.buildHandlerAppVO(handlerAppSearchResult));
//                List<HandlerAppVO> handlerAppVos = handlerAppVOStream.collect(Collectors.<HandlerAppVO>toList());
            } else {
//                handlerAppCriteria.setDefault(null);
//                handlerAppCriteria.setOsEnvironment(null);
                handlerAppSearchResultModelList = HandlerAppService.Instance.list(handlerAppCriteria, fromInt, countInt, "handlerApp.versionCode", false, session);

                if (handlerAppSearchResultModelList != null && !handlerAppSearchResultModelList.isEmpty()) {
                    if (!hasDefaultLauncher) {
                        handlerAppSearchResultModelList.remove(0);
                    }
                    for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                        if (handlerAppSearchResultModel != null && !handlerAppSearchResultModel.equals(defaultHandlerApp)) {
                            HandlerAppVO handlerAppVO = HandlerAppVO.buildHandlerAppVO(handlerAppSearchResultModel);
                            handlerAppVos.add(handlerAppVO);
                        }
                    }
                } else if (oldVersion == null) {
                    handlerAppCriteria.setOrganization(null);
                    handlerAppSearchResultModelList = HandlerAppService.Instance.list(handlerAppCriteria, fromInt, countInt, "handlerApp.versionCode", false, session);
                    if (handlerAppSearchResultModelList != null && !handlerAppSearchResultModelList.isEmpty()) {
                        for (HandlerAppService.HandlerAppSearchResultModel handlerAppSearchResultModel : handlerAppSearchResultModelList) {
                            if (handlerAppSearchResultModel != null) {
                                HandlerAppVO handlerAppVO = HandlerAppVO.buildHandlerAppVO(handlerAppSearchResultModel);
                                handlerAppVos.add(handlerAppVO);
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            responseVO.setResult(JsonUtil.getJson(handlerAppVos));

            return responseVO;

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    @RequestMapping(value = "getPartOfOsByOsCode", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO getPartOfOsByOsCode(@RequestParam(required = true) String osCode, HttpServletRequest request, HttpServletResponse response) {

        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        try {
            session = HibernateUtil.getCurrentSession();

            List<OS> osList = new ArrayList<>();
            OSService.OSCriteria osCriteria = new OSService.OSCriteria();

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult("");

            if (osCode != null && !osCode.trim().equals("")) {
                osCriteria.setOsCode(osCode.trim());
                osList = OSService.Instance.list(osCriteria, 0, -1, "id", false, session);
                if (osList != null && !osList.isEmpty()) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    OS loadedOs = osList.get(0);
                    loadedOs.setHandlerApps(null);
                    loadedOs.setHandlerAppDownloadPath(null);
                    loadedOs.setOsType(null);

                    OSVO osVo = OSVO.buildOSVO(osList.get(0));
                    String convertedOS = JsonUtil.getJson(osVo);
                    convertedOS.replaceAll("\\\\", "");
                    String replaceStr = convertedOS.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
                    responseVO.setResult(replaceStr);
                    return responseVO;
                }
            }


        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;

    }


    @RequestMapping(value = "/approveComment", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO approveComment(
            @RequestParam(required = true) String jwtToken,
            @RequestParam(required = true) String commentId,
            @RequestParam(required = true) Boolean approveStatus) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        try {
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            CommentService.ElasticApproveVO elasticApproveVO = new CommentService.ElasticApproveVO();

            elasticApproveVO.setParentCommentId(commentId);
            elasticApproveVO.setUserId(Long.valueOf(jwtUser.getUserId()));
            elasticApproveVO.setApproveDate(new DateTime().getTime());
            elasticApproveVO.setUserName(jwtUser.getUserName());
            elasticApproveVO.setApproved(approveStatus);

            JestResult jestResult = CommentService.Instance.approveComment(commentId, elasticApproveVO);

            if (!jestResult.isSucceeded()) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult(jestResult.getErrorMessage());
            } else {
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            }

        } catch (Exception ex) {

        } finally {
            if (session != null && session.isOpen())
                session.close();
        }

        return responseVO;
    }


    @RequestMapping(value = "/activateDeactivateHandlerApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO activateDeactivateHandlerApp(
            @RequestParam(required = true) String jwtToken,
            @RequestParam(required = true) Long osId,
            @RequestParam(required = true) Long handlerAppVersionCode,
            @RequestParam(required = true) Boolean activate,
            HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        try {
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            session = HibernateUtil.getCurrentSession();

            // should use this line instead of osId and use it and OsName and osVersion to obtain Os
//
//            OSType osType = null;
//            try {
//                osType = preProcess_OSTYPE();
//            } catch (Exception e) {
//                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
//                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
//                return responseVO;
//            }

            UserService.Instance.checkAccessibility(jwtUser, Access.HANDLERAPP_EDIT, responseVO, session);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(Long.parseLong(jwtUser.getUserId()), session);

            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                responseVO.setResult("such user not exists in appStore!");
                return responseVO;
            }


            HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
            List<Long> osIdList = new ArrayList<>();
            osIdList.add(osId);
            handlerAppCriteria.setOsIds(osIdList);
            handlerAppCriteria.setVersionCode(handlerAppVersionCode);

            Transaction tx = null;
            try {
                PrincipalUtil.setCurrentUser(user);

                List<HandlerAppService.HandlerAppSearchResultModel> handlerAppList = HandlerAppService.Instance.list(handlerAppCriteria, 0, -1, null, true, session);

                if (!handlerAppList.isEmpty()) {
                    tx = session.beginTransaction();
                    HandlerApp handlerApp = (HandlerApp) session.get(HandlerApp.class, handlerAppList.get(0).getId());
                    if (handlerApp != null) {
                        handlerApp.setActive(activate);
                        HandlerAppService.Instance.saveOrUpdate(handlerApp, session);
                        tx.commit();
                    }

                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                } else {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    responseVO.setResult("specified handlerapp not found!");
                }

            } catch (Exception ex) {
                if (tx != null)
                    tx.rollback();
                logger.error("Error occured in activateDeactivateHandlerApp service ", ex);
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            }


        } catch (Exception ex) {
            logger.error("Error occured in activateDeactivateHandlerApp webService ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    @RequestMapping(value = "/setAppLauncherToOS", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO setAppLauncherToOS(@RequestParam(required = true) HandlerAppVO handlerAppVO,
                                         @RequestParam(required = true) String jwtToken, @RequestParam(required = true) Boolean oldVersion,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        Transaction tx = null;
        try {
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            UserService.Instance.checkAccessibility(jwtUser, Access.OS_EDIT, responseVO, session);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(Long.parseLong(jwtUser.getUserId()), session);
            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                return responseVO;
            }
            PrincipalUtil.setCurrentUser(user);

            OS os = null;
            if (handlerAppVO.getOsId() != null) {
                os = (OS) session.get(OS.class, handlerAppVO.getOsId());
            }
            if (os == null) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }

            OSEnvironment osEnvironment = null;
            if (handlerAppVO.getEnvironmentId() != null) {
                osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, Long.valueOf(handlerAppVO.getEnvironmentId()));
            } else {
                osEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
            }


            if (osEnvironment == null) {
                responseVO.setResultStatus(ResultStatus.ENVIRONMENT_NOT_FOUND);
                responseVO.setResult(ResultStatus.ENVIRONMENT_NOT_FOUND.toString());
                return responseVO;
            }
            Organization organization = null;
            if (handlerAppVO.getOrgId() != null) {
                organization = (Organization) session.get(Organization.class, handlerAppVO.getOrgId());

            } else {
                organization = OrgService.Instance.getDefaultOrganization(session);
            }
            if (organization == null) {
                responseVO.setResultStatus(ResultStatus.ORGANIZATION_NOT_FOUND);
                responseVO.setResult(ResultStatus.ORGANIZATION_NOT_FOUND.toString());
                return responseVO;
            }
            boolean hasTestFileHandler = false;
            if (handlerAppVO.getTestFileHandlerAppKey() != null && !handlerAppVO.getTestFileHandlerAppKey().trim().equals("")) {
                hasTestFileHandler = true;
            } else if (handlerAppVO.getFileHandlerAppKey() == null || handlerAppVO.getFileHandlerAppKey().trim().equals("")) {
                if (!hasTestFileHandler) {
                    responseVO.setResultStatus(ResultStatus.FILE_KEY_NULL);
                    responseVO.setResult(ResultStatus.FILE_KEY_NULL.toString());
                    return responseVO;
                }
            }
            HandlerAppService.HandlerAppCriteria criteria = new HandlerAppService.HandlerAppCriteria();
            criteria.setVersionCode(handlerAppVO.getVersionCode());
            List<Long> osIdList = new ArrayList<>();
            osIdList.add(handlerAppVO.getOsId());
            criteria.setOsIds(osIdList);
            criteria.setOsEnvironment(osEnvironment);
            criteria.setOrganization(organization);
            criteria.setActive(true);

            List ls = HandlerAppService.Instance.list(criteria, 0, 1, null, true, session);
            if (!ls.isEmpty()) {
                responseVO = new ResponseVO();
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult("handler app with this version code already defined!");
                return responseVO;
            }

            tx = session.beginTransaction();
            if (oldVersion != null) {
                handlerAppVO.setDefault(oldVersion);
            } else {
                handlerAppVO.setDefault(true);
            }
            HandlerApp handlerApp = HandlerAppService.Instance.saveHandlerAppToDataBase(session, handlerAppVO, os);
            if (handlerApp != null) {
                List<HandlerApp> handlerAppList = HandlerAppService.Instance.getAndSetHandlerAppList(session, handlerApp, os);
                os.setHandlerApps(handlerAppList);
                OSService.Instance.saveOrUpdate(os, session);

                tx.commit();
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                responseVO.setResult(ResultStatus.SUCCESSFUL.toString());
            } else {
                tx.rollback();
            }

            return responseVO;

        } catch (Exception ex) {
            tx.rollback();
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/getAppInstallationCount", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getAppInstallationCount(
            @RequestParam(required = true, value = "packageName") String packageName, HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;

        try {
            session = HibernateUtil.getCurrentSession();
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }
            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }

            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);
            responseVO = checkApp(app);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL))
                return responseVO;
            AppElasticService.AppInstallCriteria appInstallCriteria = new AppElasticService.AppInstallCriteria();
            appInstallCriteria.installAction = AppElasticService.InstallAction.INSTALL.toString();
            appInstallCriteria.appId = app.getId();
            Integer countLik = AppElasticService.Instance.getInstallationCountForApp(appInstallCriteria).intValue();
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(countLik.toString());
            return responseVO;

        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    @RequestMapping(value = "/getAppInstallationInfo", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getAppInstallationInfo(
            @RequestParam(required = true, value = "packageName") String packageName
            , @RequestParam(required = false) String jwtToken
            , @RequestParam(required = false) Integer from
            , @RequestParam(required = false) Integer count
            , HttpServletResponse response
            , HttpServletRequest request) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }
            JWTService.JWTUserClass jwtUser = null;
            if (jwtToken != null && !jwtToken.trim().equals("")) {
                jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
            }

            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }


            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);
            responseVO = checkApp(app);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL))
                return responseVO;
            AppElasticService.AppInstallCriteria appInstallCriteria = new AppElasticService.AppInstallCriteria();
            appInstallCriteria.installAction = AppElasticService.InstallAction.INSTALL.toString();
            if (jwtUser != null) {
                appInstallCriteria.userId = Long.valueOf(jwtUser.getUserId());
            }
            appInstallCriteria.appId = app.getId();
            int fromInt = (from == null ? 0 : from);
            int countInt = (count == null ? 10 : count);
            List<AppElasticService.AppInstallVO> appInstallVOList = AppElasticService.Instance.getInstallationInfoForApp(appInstallCriteria, fromInt, countInt);
            String resultString = JsonUtil.getJson(appInstallVOList);

            responseVO.setResult(resultString);

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            return responseVO;
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/setInstallInfoForApp", headers = "Accept=*/*", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseBody
    public ResponseVO setInstallInfoForApp
            (@RequestParam(required = true, value = "appInstallInfoVO") AppInstalledVO appInstalledVO
                    , @RequestParam(required = false) String jwtToken
                    , HttpServletResponse response
                    , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        Transaction tx = null;
        try {
            JWTService.JWTUserClass jwtUser = null;
            if (jwtToken != null) {
                jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
            }

            session = HibernateUtil.getCurrentSession();
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }

            if (appInstalledVO.getDeviceId() == null || appInstalledVO.getDeviceId().trim().equals("")) {
                responseVO.setResultStatus(ResultStatus.DEVICE_ID_IS_NULL);
                responseVO.setResult(ResultStatus.DEVICE_ID_IS_NULL.toString());
                return responseVO;
            }

            if (appInstalledVO == null || appInstalledVO.getPackageList() == null || appInstalledVO.getPackageList().isEmpty()) {
                responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
                responseVO.setResult(ResultStatus.APP_NOT_FOUND.toString());
                return responseVO;
            }

            Integer appInstallReportMaxSize = Integer.parseInt(
                    ConfigUtil.getProperty(ConfigUtil.APP_INSTALL_REPORT_MAX_SIZE));
            if (appInstalledVO.getPackageList().size() > appInstallReportMaxSize) {
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                responseVO.setResult("app report size in too large");
                return responseVO;
            }

            tx = session.beginTransaction();
            AppInstallReportQueueService.Instance.save(appInstalledVO, osType, jwtUser, session);
            tx.commit();

            responseVO.setResult("");
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            return responseVO;

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error("error ocuured in setInstallInfoForApp ", e);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/listTimeLines", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listTimeLines(@RequestParam(required = false) TimeLineVO timeLineVO) {

        Integer fromIndex = PrincipalUtil.getFromIndex();
        Integer countIndex = PrincipalUtil.getCountIndex();
        Boolean getResultCount = PrincipalUtil.getResultCount();
        String sortBy = PrincipalUtil.getSortBy();

        Boolean isAscending = PrincipalUtil.isAscending();
        ResponseVO responseVO = new ResponseVO();
        Object resultObject = null;

        try {
//            User requesterUser = null;
//            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
//            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
//                return responseVO;
//            }

            TimeLineElasticService.TimeLineCriteria timeLineCriteria = TimeLineElasticService.Instance.BuildCriteriaByVO(timeLineVO);

            if (getResultCount) {
                Long resultCount = TimeLineElasticService.Instance.count(timeLineCriteria, fromIndex, countIndex, sortBy, isAscending);
                resultObject = resultCount;
            } else {
                resultObject = TimeLineElasticService.Instance.searchTimeLine(timeLineCriteria, fromIndex, countIndex, sortBy, isAscending);
            }
            responseVO.setResult(JsonUtil.getJson(resultObject));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO;
    }

    @RequestMapping(value = "/listAnnouncement", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listAnnouncement(
            @RequestParam(required = false) Integer lastActiveAnouncements) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        try {
            OSType osType = preProcess_OSTYPE();

            session = HibernateUtil.getCurrentSession();
            if (lastActiveAnouncements == null) {
                lastActiveAnouncements = Integer.valueOf(ConfigUtil.getProperty(ConfigUtil.LAST_DATA_TO_SHOW));
            }
            AnouncementService.AnouncmentCriteria anouncmentCriteria = new AnouncementService.AnouncmentCriteria();
            anouncmentCriteria.setOsTypes(Arrays.asList(osType));
            anouncmentCriteria.setActive(true);
            List<AnouncementVO> anouncementVOList = getAnouncementVOS(lastActiveAnouncements, session, anouncmentCriteria);
            responseVO.setResult(JsonUtil.getJson(anouncementVOList));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception ex) {
            responseVO.setResult(ex.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            logger.error("Error getting list of announcements ", ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;

    }

    @RequestMapping(value = "/allOrganizationUnits", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO allOrganizationUnits() {
        ResponseVO responseVO = new ResponseVO();
        List<OrganizationVO> organizationVOList = new ArrayList<>();
        try {

//            User requesterUser = null;
//            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
//            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
//                return responseVO;
//            }
//            String jwtToken = PrincipalUtil.getJwtToken();
//
//            if (jwtToken == null || jwtToken.trim().equals("")) {
//                responseVO.setResultStatus(ResultStatus.NO_JWT_TOKEN_RECEIVED);
//                responseVO.setResult(ResultStatus.NO_JWT_TOKEN_RECEIVED.toString());
//                return responseVO;
//            }
            organizationVOList = EngineOrganizationService.Instance.getAllOrganization();

            responseVO.setResult(JsonUtil.getJson(organizationVOList));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception ex) {
            responseVO.setResult(ex.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            logger.error("Error getting list of announcements ", ex);
        }
        return responseVO;

    }

    @RequestMapping(value = "/allOrganizationUnitsInTree", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public static ResponseVO allOrganizationUnitsInTree() {
        ResponseVO responseVO = new ResponseVO();
        List<OrganizationVO> organizationVOList = new ArrayList<>();
        try {
//            User requesterUser = null;
//            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
//            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
//                return responseVO;
//            }
//
//            String jwtToken = PrincipalUtil.getJwtToken();
//
//
//            if (jwtToken == null || jwtToken.trim().equals("")) {
//                responseVO.setResultStatus(ResultStatus.NO_JWT_TOKEN_RECEIVED);
//                responseVO.setResult(ResultStatus.NO_JWT_TOKEN_RECEIVED.toString());
//                return responseVO;
//            }
            organizationVOList = EngineOrganizationService.Instance.getAllOrganization();


            OrganizationParentChildVO finalOrganizationParentChildVO = null;
            if (organizationVOList != null && !organizationVOList.isEmpty()) {
                ArrayList<ParentChildPairVO> pairs = new ArrayList<ParentChildPairVO>();
                Map<Long, Long> parentChildMap = new HashMap<>();
                for (OrganizationVO organizationVOInList : organizationVOList) {
                    pairs.add(new ParentChildPairVO(organizationVOInList.getId(), organizationVOInList.getParentId(), organizationVOInList.getTitleFa()));
                    parentChildMap.put(organizationVOInList.getId(), organizationVOInList.getParentId());
                }


                // Arrange
                Map<Long, OrganizationParentChildVO> hm = new HashMap<>();

                // populate a Map
                for (ParentChildPairVO p : pairs) {

                    if (p.getChildId() != null && p.getChildId() > 0) {
                        //  ----- Child -----
                        OrganizationParentChildVO mmdChild;
                        if (hm.containsKey(p.getChildId())) {
                            mmdChild = hm.get(p.getChildId());
                        } else {
                            mmdChild = new OrganizationParentChildVO();
                            hm.put(p.getChildId(), mmdChild);
                        }
                        mmdChild.setId(p.getChildId());
                        mmdChild.setTitle(p.getTitle());
                        mmdChild.setParentId(p.getParentId());
                        // no need to set ChildrenItems list because the constructor created a new empty list

                        // ------ Parent ----
                        OrganizationParentChildVO mmdParent;
                        if (hm.containsKey(p.getParentId())) {
                            mmdParent = hm.get(p.getParentId());
                        } else {
                            mmdParent = new OrganizationParentChildVO();
                            hm.put(p.getParentId(), mmdParent);
                        }
                        mmdParent.setId(p.getParentId());
                        mmdParent.setParentId(null);
                        mmdParent.addChildrenItem(mmdChild);
                    }


                }

                for (OrganizationParentChildVO mmd : hm.values()) {
                    mmd.setParentId(parentChildMap.get(mmd.getId()));
                }

                // Get the root
                List<OrganizationParentChildVO> DX = new ArrayList<OrganizationParentChildVO>();
                for (OrganizationParentChildVO mmd : hm.values()) {
                    if (mmd.getParentId() == null)
                        DX.add(mmd);
                }

                finalOrganizationParentChildVO = new OrganizationParentChildVO();
                for (OrganizationParentChildVO organizationParentChildVO : DX) {
                    if (organizationParentChildVO.getId() == null) {
                        organizationParentChildVO.setTitle("root");
                        finalOrganizationParentChildVO = organizationParentChildVO;
                        break;
                    }
                }
            }


            responseVO.setResult(JsonUtil.getJson(finalOrganizationParentChildVO));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception ex) {
            responseVO.setResult(ex.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            logger.error("Error getting list of announcements ", ex);
        }
        return responseVO;

    }

    @RequestMapping(value = "/listAnnouncementFirstPage", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listAnnouncementFirstPage(
            @RequestParam(required = false) Integer lastActiveAnouncements, @RequestParam(required = false) String announcementName) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        try {
            OSType osType = null;
            if (PrincipalUtil.getCurrentOSTYPE() != null) {
                osType = preProcess_OSTYPE();
            }

            session = HibernateUtil.getCurrentSession();
            if (lastActiveAnouncements == null) {
                lastActiveAnouncements = Integer.valueOf(ConfigUtil.getProperty(ConfigUtil.LAST_DATA_TO_SHOW));
            }
            AnouncementService.AnouncmentCriteria anouncmentCriteria = new AnouncementService.AnouncmentCriteria();
            if (osType != null) {
                anouncmentCriteria.setOsTypes(Arrays.asList(osType));
            }
            anouncmentCriteria.setActive(true);
            if (announcementName != null && !announcementName.trim().equals("")) {
                anouncmentCriteria.setAnouncementText(announcementName);
            }
            List<AnouncementVO> anouncementVOList = getAnouncementVOS(lastActiveAnouncements, session, anouncmentCriteria);
            responseVO.setResult(JsonUtil.getJson(anouncementVOList));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception ex) {
            logger.error("Error getting list of announcements ", ex);
            responseVO.setResult(ex.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
//            throw new RuntimeException(ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;

    }

    @RequestMapping(value = "/listAllOsTypesAnnouncement", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<AnouncementVO> listAllOsTypesAnnouncement(
            @RequestParam(required = false) Integer lastActiveAnouncements) {
        Session session = null;
        try {
            AnouncementService.AnouncmentCriteria anouncmentCriteria = new AnouncementService.AnouncmentCriteria();
            anouncmentCriteria.setActive(true);
            List<AnouncementVO> anouncementVOList = getAnouncementVOS(lastActiveAnouncements, session, anouncmentCriteria);

            return anouncementVOList;
        } catch (Exception ex) {
            logger.error("Error getting list of announcements ", ex);
            throw new RuntimeException(ex);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


    @RequestMapping(value = "/listAllOsEnvironment", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listAllOsEnvironment(
            @RequestParam(required = false) Integer lastActiveAnouncements, @RequestParam(required = false) String envName) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();

        try {
            session = HibernateUtil.getCurrentSession();
            EnvironmentService.EnvironmentCriteria environmentCriteria = new EnvironmentService.EnvironmentCriteria();
            if (envName != null && !envName.trim().equals("")) {
                environmentCriteria.setEnvName(envName);
            }
            List<OSEnvironment> osEnvironmentList = EnvironmentService.Instance.list(environmentCriteria, 0, -1, null, false, session);
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            Stream<OsEnvironmentVO> environmentVOStream = osEnvironmentList.stream().map(environment -> OsEnvironmentVO.buildEnvironmentVOByEnvironment(environment));
            List<OsEnvironmentVO> environmentVOList = environmentVOStream.collect(Collectors.<OsEnvironmentVO>toList());

            responseVO.setResult(JsonUtil.getJson(environmentVOList));
        } catch (Exception ex) {
            logger.error("Error getting list of announcements ", ex);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;

    }


    @RequestMapping(value = "/listAllCompanyParsers", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO listAllCompanyParsers() {

        ResponseVO responseVO = new ResponseVO();

        try {
            String companyParsers = ConfigUtil.getProperty(ConfigUtil.COMPANY_GET_PARSERS);
            companyParsers.replaceAll("\\\\", "");
            String replaceStr = companyParsers.replaceAll("\\\\", "").replaceAll("\\\"", "\"");

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

//            responseVO.setResult(JsonUtil.getJson(replaceStr));
            responseVO.setResult(companyParsers);
        } catch (Exception ex) {
            logger.error("Error getting list of announcements ", ex);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }
        return responseVO;

    }


    private List<AnouncementVO> getAnouncementVOS(@RequestParam(required = false) Integer
                                                          lastActiveAnouncements, Session session, AnouncementService.AnouncmentCriteria anouncmentCriteria) {
        List<Anouncement> latestAnouncements =
                AnouncementService.Instance.getLatestAnouncements(anouncmentCriteria, lastActiveAnouncements, session);

        List<AnouncementVO> anouncementVOList = new ArrayList<>();
        for (Anouncement anouncement : latestAnouncements) {
            anouncementVOList.add(AnouncementVO.buildVO(anouncement));
        }
        return anouncementVOList;
    }


    @RequestMapping(value = "/callAnouncement", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO callAnouncement(
            @RequestParam(required = true, value = "announcementId") Long announcementId
            , @RequestParam(required = false) String paramsJsonString
            , HttpServletResponse response
            , HttpServletRequest request) {

        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();

            AnouncementService.AnouncmentCriteria anouncmentCriteria = new AnouncementService.AnouncmentCriteria();
            anouncmentCriteria.setId(announcementId);

            List<Anouncement> anouncementList = AnouncementService.Instance.list(anouncmentCriteria, 0, -1, null, true, session);
            if (!anouncementList.isEmpty()) {
                Anouncement intendedAnouncement = anouncementList.get(0);
                responseVO = AnouncementService.Instance.checkAnouncement(intendedAnouncement);

                if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    Map<String, String> parameterMap = new HashMap<>();
                    if (paramsJsonString != null && !paramsJsonString.trim().equals("")) {
                        JSONObject jsonObject = new JSONObject(paramsJsonString.trim());
                        for (String key : (Set<String>) jsonObject.keySet()) {
                            parameterMap.put(key, jsonObject.getString(key));
                        }
                    }

                    ITaskResult taskResult = AnouncementService.Instance.launchAnouncement(intendedAnouncement, parameterMap);
                    Object result = taskResult.getResult();

                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

                    responseVO.setResult(result.toString());

                }
            } else {
                responseVO.setResult("No such announcement exists!");
            }

            return responseVO;
        } catch (Exception ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    @RequestMapping(value = "/getFileDownloadCount", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getFileDownloadCount(
            @RequestParam(required = true, value = "fileKey") String fileKey
            , HttpServletResponse response
            , HttpServletRequest request) {

        ResponseVO responseVO = new ResponseVO();
        try {
            if (fileKey == null || fileKey.trim().equals("")) {
                responseVO.setResult(ResultStatus.FILE_KEY_NULL.toString());
                responseVO.setResultStatus(ResultStatus.FILE_KEY_NULL);
            } else {

                try {
                    Integer fileDownloadCount = FileServerService.Instance.getFileDownloadCount(fileKey.trim());
                    responseVO.setResult(String.valueOf(fileDownloadCount));
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                } catch (Exception ex) {
                    responseVO.setResultStatus(ResultStatus.INTERNAL_SERVER_ERROR);
                    responseVO.setResult(ResultStatus.INTERNAL_SERVER_ERROR.toString());
                    return responseVO;
                }

            }
        } catch (Exception ex) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            return responseVO;
        }

    }

    private List<ProductVO> setAndReturnProductVOs(HttpServletRequest request, List<AppService.AppSearchResultModel> appSearchResultModelList) {
        if (appSearchResultModelList != null && appSearchResultModelList.size() > 0) {
            Stream<ProductVO> productVOStream =
                    appSearchResultModelList.stream().map(appSearchResultModel -> {
                        try {
                            return ProductVO.buildProductVO(appSearchResultModel);

                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            return productVOStream.collect(Collectors.<ProductVO>toList());

        } else {
            return new ArrayList<ProductVO>();
        }
    }

    private List<ProductVO> setAndReturnProductVOsFromAppList(HttpServletRequest request, List<App> appList) {
        if (appList != null && !appList.isEmpty()) {
            Stream<ProductVO> productVOStream =
                    appList.stream().map(app -> {
                        try {
                            return ProductVO.buildProductVO(app);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            return productVOStream.collect(Collectors.<ProductVO>toList());
        } else {
            return new ArrayList<ProductVO>();
        }
    }


    @RequestMapping(value = "/getCountNewApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountNewApp(HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            criteria.getNewApp = true;
            criteria.osType = osTypeList;
            List<PublishState> publishStateArrayList = new ArrayList<>();
            publishStateArrayList.add(PublishState.PUBLISHED);

            criteria.publishStates = publishStateArrayList;
            criteria.setDeleted(false);

            Long appCount = AppService.Instance.count(criteria, session);
            return appCount;

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }


    @RequestMapping(value = "/getPackagesForApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<PackageVO> getPackagesForApp(String packageName, int from, int count, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        List<PackageVO> packageVOList = new ArrayList<>();

        try {
            OSType osType = preProcess_OSTYPE();

            AppPackageService.Criteria criteria = new AppPackageService.Criteria();
            criteria.appPackageName = packageName;
            criteria.osType = osType;
            criteria.isDeleted = false;

            List<AppPackageService.AppPackageSearchResult> resultList =
                    AppPackageService.Instance.list(criteria, from, count, session);

            packageVOList = PackageVO.buildPackageVO(resultList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }

        return packageVOList;
    }

    @RequestMapping(value = "/publishUnpublisAppPackage", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO publishUnpublisAppPackage(@RequestParam(required = true) String packageName,
                                                @RequestParam(required = true) String versionCode,
                                                @RequestParam(required = true) String jwtToken,
                                                @RequestParam(required = true) boolean publishPackage,
                                                HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Transaction tx = null;
        try {
            OSType osType = preProcess_OSTYPE();
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            UserService.Instance.checkAccessibility(jwtUser, Access.APP_PUBLISH_UNPUBLISH, responseVO, session);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            User user = UserService.Instance.getUserBySSOId(Long.parseLong(jwtUser.getUserId()), session);
            if (user == null) {
                responseVO.setResultStatus(ResultStatus.USER_NOT_IN_SSO);
                return responseVO;
            }

            PrincipalUtil.setCurrentUser(user);
            String packageNameStr = null;
            String versionCodeStr = null;

            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }

            if (versionCode != null && !versionCode.trim().equals("")) {
                versionCodeStr = versionCode.trim();
            }

            AppPackageService.Criteria criteria = new AppPackageService.Criteria();
            criteria.appPackageName = packageNameStr;
            criteria.osType = osType;
            criteria.versionCode = versionCodeStr;
            criteria.isDeleted = false;

            List<AppPackageService.AppPackageSearchResult> resultList =
                    AppPackageService.Instance.list(criteria, 0, -1, session);
            tx = session.beginTransaction();
            if (resultList != null && !resultList.isEmpty() && resultList.get(0).getAppPackage() != null) {
                AppPackageService.AppPackageSearchResult searchResult = resultList.get(0);
                AppPackage appPackage = searchResult.getAppPackage();
                PublishState publishState = appPackage.getPublishState();

                appPackage = AppPackageHistoryService.Instance.setAppPackageHistoryForAppPackage(session, appPackage);

                AppPackageService.Instance.publishUnPublishPackage(session, searchResult, appPackage, publishState, publishPackage);

            }
            tx.commit();
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult("appPackageState changed successfully!");
            return responseVO;

        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();

            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

    }

    @RequestMapping(value = "/getUpdatedApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getUpdatedApp(@RequestParam(required = false) Integer from,
                                         @RequestParam(required = false) Integer count,
                                         HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            criteria.osType = osTypeList;
            criteria.getUpdatedApp = true;
            List<PublishState> publishStateArrayList = new ArrayList<>();
            publishStateArrayList.add(PublishState.PUBLISHED);
            criteria.publishStates = publishStateArrayList;
            int fromInt = (from == null ? 0 : from);
            int countInt = (count == null ? 10 : count);

            criteria.setDeleted(false);
            List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromInt, countInt, "app.mainPackageModificationDate", false, session);
            return setAndReturnProductVOs(request, appSearchResultModelList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getCountUpdatedApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Long getCountUpdatedApp(HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();

            criteria.getUpdatedApp = true;
            criteria.osType = osTypeList;
            List<PublishState> publishStateArrayList = new ArrayList<>();
            publishStateArrayList.add(PublishState.PUBLISHED);
            criteria.publishStates = publishStateArrayList;

            criteria.setDeleted(false);
            Long appCount = AppService.Instance.count(criteria, session);
            return appCount;
        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getOSTypeList", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<OSTypeVO> getOSTypeList(@RequestParam(required = false) String osTypeName, HttpServletRequest request, HttpServletResponse response) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();
            if (osTypeName != null && !osTypeName.trim().equals("")) {
                osTypeCriteria.name = osTypeName.trim();
            }
            osTypeCriteria.disabled = false;
            List<OSType> osTypeList = OSTypeService.Instance.list(osTypeCriteria, 0, -1, null, true, session);

            List<OSTypeVO> osTypeVOList = osTypeList.stream()
                    .map(osType -> OSTypeVO.buildOsTypeVO(osType)).collect(Collectors.toList());
            return osTypeVOList;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/getOSTypeListWithLauncher", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<OSTypeVO> getOSTypeListWithLauncher(@RequestParam(required = false) Long organizationId, @RequestParam(required = false) String environmentId,
                                                    HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        List<OSTypeVO> osTypeVOList = new ArrayList<>();
        try {
            session = HibernateUtil.getNewSession();
            OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();
            osTypeCriteria.disabled = false;
            List<OSType> osTypeListWithLauncher = OSTypeService.Instance.list(osTypeCriteria, 0, -1, null, true, session);
            Set<OSType> osTypeSet = new HashSet<>();
            Organization organization;


            OSService.OSCriteria osCriteria = new OSService.OSCriteria();
            osCriteria.osType = osTypeListWithLauncher;
            osCriteria.disabled = false;
            if (organizationId != null) {
                organization = (Organization) session.get(Organization.class, organizationId);
            } else {
                organization = OrgService.Instance.getDefaultOrganization(session);
            }
            if (organization == null) {
                return osTypeVOList;
            }

//                List<OS> osList = OSService.Instance.getOSForOSType(osType);
            List<OS> osList = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
            for (OS os : osList) {
                HandlerAppService.HandlerAppCriteria handlerAppCriteria = new HandlerAppService.HandlerAppCriteria();
                OSEnvironment osEnvironment;
                if (environmentId != null && !environmentId.trim().equals("") && !environmentId.trim().equals("null")) {
                    osEnvironment = (OSEnvironment) session.get(OSEnvironment.class, Long.valueOf(environmentId));
                } else {
                    osEnvironment = EnvironmentService.Instance.getDefaultEnvironment();
                }

                if (osEnvironment != null) {
                    handlerAppCriteria.setOsEnvironment(osEnvironment);
                }

                List<Long> osIdList = new ArrayList<>();
                osIdList.add(os.getId());
                handlerAppCriteria.setOsIds(osIdList);
                handlerAppCriteria.setActive(true);
                handlerAppCriteria.setOrganization(organization);
                List<HandlerAppService.HandlerAppSearchResultModel> handlerAppSearchResultModels = HandlerAppService.Instance.list(handlerAppCriteria, 0, -1, null, true, session);
                if (handlerAppSearchResultModels != null && !handlerAppSearchResultModels.isEmpty()) {
                    osTypeSet.add(os.getOsType());
                }
            }


            osTypeVOList = osTypeSet.stream()
                    .map(osType -> OSTypeVO.buildOsTypeVO(osType)).collect(Collectors.toList());

            return osTypeVOList;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @RequestMapping(value = "/getOSlistForOSType", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<OSVO> getOSlistForOSType(@RequestParam(value = "osTypeVO") OSTypeVO osTypeVO, HttpServletRequest request) {
        Session session = HibernateUtil.getNewSession();
        try {
            List<OSVO> osVOList = new ArrayList<>();
            if (osTypeVO != null && (osTypeVO.getOsName() != null || osTypeVO.getOsTypeID() != null)) {
                OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();

                if (osTypeVO.getOsTypeID() != null) {
                    osTypeCriteria.id = osTypeVO.getOsTypeID();
                }
                if (osTypeVO.getOsName() != null && !osTypeVO.getOsName().trim().isEmpty()) {
                    osTypeCriteria.name = osTypeVO.getOsName();
                }
                osTypeCriteria.disabled = false;

                List<OSType> osTypes = OSTypeService.Instance.list(osTypeCriteria, 0, -1, null, true, session);
                List<OS> allOs = new ArrayList<>();
                for (OSType osType : osTypes) {
                    List<OSType> osTypeList = new ArrayList<>();
                    osTypeList.add(osType);
                    OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                    osCriteria.osType = osTypeList;
                    osCriteria.disabled = false;
                    List<OS> tmpAllOs = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
                    if (tmpAllOs != null && !tmpAllOs.isEmpty()) {
                        allOs.addAll(tmpAllOs);
                    }
                }
                osVOList = null;
                if (!allOs.isEmpty()) {
                    osVOList = allOs.stream()
                            .map(os -> OSVO.buildOSVO(os)).collect(Collectors.toList());
                }
            }
            return osVOList;
        } catch (Exception ex) {
            logger.error("error getting osList for osType", ex);
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getOsList", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<OSVO> getOsList(@RequestParam(required = false) String osName, HttpServletRequest request) {
        Session session = HibernateUtil.getNewSession();
        try {
            List<OSVO> osVOList = new ArrayList<>();
            OSTypeService.OSTypeCriteria osTypeCriteria = new OSTypeService.OSTypeCriteria();
            osTypeCriteria.disabled = false;
            List<OSType> osTypes = OSTypeService.Instance.list(osTypeCriteria, 0, -1, null, true, session);
            List<OS> allOs = new ArrayList<>();

            for (OSType osType : osTypes) {

                List<OSType> osTypeList = new ArrayList<>();
                osTypeList.add(osType);
                OSService.OSCriteria osCriteria = new OSService.OSCriteria();
                osCriteria.osType = osTypeList;
                if (osName != null && !osName.trim().equals("")) {
                    osCriteria.osName = osName;
                }

                osCriteria.disabled = false;
                List<OS> tmpAllOs = OSService.Instance.list(osCriteria, 0, -1, null, true, session);
                if (tmpAllOs != null && !tmpAllOs.isEmpty()) {
                    allOs.addAll(tmpAllOs);
                }
            }
            osVOList = null;
            if (!allOs.isEmpty()) {
                osVOList = allOs.stream()
                        .map(os -> OSVO.buildOSVO(os)).collect(Collectors.toList());
            }

            return osVOList;
        } catch (Exception ex) {
            logger.error("error getting osList for osType", ex);
        } finally {
            session.close();
        }
        return null;
    }


    @RequestMapping(value = "/getRelatedAppsCount", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public Integer getRelatedAppsCount(
            @RequestParam(required = true) String packageName, HttpServletRequest request) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            App intendedApp = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);

            return AppService.Instance.getRelatedAppsCount(intendedApp).intValue();

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getRelatedAppsList", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getRelatedAppsList(@RequestParam(required = true) String packageName,
                                              int from,
                                              int count,
                                              HttpServletRequest request) {
        Session session = HibernateUtil.getCurrentSession();
        try {
            OSType osType = preProcess_OSTYPE();
            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }

            App intendedApp = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);

            if (intendedApp != null) {
                List<App> relatedApps = AppService.Instance.getRelatedApps(intendedApp, from, count, session);

                return setAndReturnProductVOsFromAppList(request, relatedApps);
            } else {
                return new ArrayList<>();
            }

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/uploadApp", headers = "Accept=*/*", method = RequestMethod.POST, produces = {"application/json"})
    @ResponseBody
    public ResponseVO uploadApp(
            @RequestParam(required = true) String jwtToken,
            @RequestParam(required = true, value = "appUploadVO") AppUploadVO appUploadVO,
            HttpServletRequest request,
            HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Session session = null;
        Transaction tx = null;
        try {
            OSType osType = preProcess_OSTYPE();

            Long userId;
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            } else {
                userId = Long.valueOf(jwtUser.getUserId());
            }
            session = HibernateUtil.getCurrentSession();


            appUploadVO.checkValidity(session, osType);
            responseVO = AppService.Instance.checkValidData(appUploadVO, session);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            String packageName = responseVO.getResult();

            boolean deletedApp = false;

            App app = AppService.Instance.getAppByPackageName(osType, appUploadVO.getPackageName(), deletedApp, session);
            User developerUser = UserService.Instance.getUserBySSOId(userId, session);
            Access access = null;
            if (app == null) {
                access = Access.APP_ADD;
            } else {
                access = Access.APP_EDIT;
            }
            UserService.Instance.checkAccessibility(jwtUser, access, responseVO, session);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }

            if (app == null) {
                app = new App();
                app.setPackageName(appUploadVO.getPackageName());
                AppCategory appCategory = AppCategoryService.Instance.loadCategoryById(appUploadVO.getCategoryId(), session);
                if (appCategory == null) {
                    throw new Exception("no such appCategory Exists!");
                }
                app.setAppCategory(appCategory);
                app.setDescription(appUploadVO.getDescription());

                if (developerUser == null) {
                    throw new Exception("no such developer user Exists!");
                }
                app.setDeveloper(developerUser);

                PrincipalUtil.setCurrentUser(developerUser);

                app.setOsType(osType);
                OS selectedOs = OSService.Instance.loadOSByOSId(appUploadVO.getOsId(), session);
                app.setOs(selectedOs);
            } else if (!app.getDeveloper().equals(developerUser)) {
                throw new Exception("you are not app Developer!");
            }

            Map<String, String> thumbIMagesMap = new HashMap<>();
            int counter = 0;
            if (appUploadVO != null && appUploadVO.getAppPackageVO() != null && appUploadVO.getAppPackageVO().getPackageImagesKeys() != null) {
                for (String packageKey : appUploadVO.getAppPackageVO().getPackageImagesKeys()) {
                    thumbIMagesMap.put(packageKey, appUploadVO.getAppPackageVO().getPackageImageNames().get(counter));
                    counter++;
                }
            }

            tx = session.beginTransaction();
            app.setDeleted(deletedApp);
            app.setPackageName(packageName);
            AppService.AppSearchCriteria appSearchCriteria = new AppService.AppSearchCriteria();
            appSearchCriteria.setPackageName(app.getPackageName());
            appSearchCriteria.setDeleted(false);
            if (app.getOsType() != null) {
                List<OSType> osTypeList = new ArrayList<>();
                osTypeList.add(app.getOsType());
                appSearchCriteria.setOsType(osTypeList);
            }
            appSearchCriteria.setId(app.getId());
            Boolean checkExistUnDeletedApp = AppService.Instance.checkExistUnDeletedApp(appSearchCriteria, session);
            if (checkExistUnDeletedApp == null) {
                throw new Exception("app is null");
            } else if (checkExistUnDeletedApp) {
                throw new Exception(AppStorePropertyReader.getString("error.unDeleted.app.found"));
            }

            AppService.Instance.savePackageForApp(
                    app,
                    appUploadVO.getAppPackageVO().getPackageFileName(),
                    appUploadVO.getAppPackageVO().getPackageFileKey(),
                    appUploadVO.getAppPackageVO().getIconFileName(),
                    appUploadVO.getAppPackageVO().getIconFileKey(),
                    thumbIMagesMap,
                    osType,
                    appUploadVO.getShortDescription(),
                    appUploadVO.getDescription(),
                    appUploadVO.getTitle(),
                    appUploadVO.getCategoryId(),
                    session
            );

            tx.commit();

            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(AppVO.buildAppVO(app)));

        } catch (Exception ex) {
            logger.error("error upload app via webservice ", ex);

            if (tx != null)
                tx.rollback();

            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(ex.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;
    }


    @RequestMapping(value = "/getRateAverageForApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO getRateAverageForApp(@RequestParam(required = true) String packageName
            , HttpServletResponse response
            , HttpServletRequest request) {
        ResponseVO responseVO = new ResponseVO();
        Session session = HibernateUtil.getCurrentSession();

        try {
            OSType osType = null;
            try {
                osType = preProcess_OSTYPE();
            } catch (Exception e) {
                responseVO.setResultStatus(ResultStatus.OS_NOT_FOUND);
                responseVO.setResult(ResultStatus.OS_NOT_FOUND.toString());
                return responseVO;
            }

            String packageNameStr = null;
            if (packageName != null && !packageName.trim().equals("")) {
                packageNameStr = packageName.trim();
            }
            App app = AppService.Instance.getAppByPackageName(osType, packageNameStr, false, session);
            responseVO = checkApp(app);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL))
                return responseVO;

            CommentService.CommentVOCriteria commentVOCriteria = new CommentService.CommentVOCriteria();
            commentVOCriteria.appId = app.getId();
            commentVOCriteria.approved = true;


            Double average = CommentService.Instance.getAppRateAverage(commentVOCriteria, 0, 0, null, false);
            if (average != Double.valueOf(-1)) {
                responseVO.setResult(average.toString());
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                return responseVO;

            } else {
                responseVO.setResult(ResultStatus.UNSUCCESSFUL.toString());
                responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                return responseVO;
            }

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            return responseVO;
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/getAppByKeyword", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public List<ProductVO> getAppByKeyword(
            @RequestParam(required = true) String keywords
            , @RequestParam(required = false) Integer from
            , @RequestParam(required = false) Integer count,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        Session session = HibernateUtil.getNewSession();
        try {
            OSType osType = preProcess_OSTYPE();
            List<OSType> osTypeList = new ArrayList<>();
            osTypeList.add(osType);
            AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
            criteria.osType = osTypeList;
            criteria.publishStates = Arrays.asList(PublishState.PUBLISHED);

            int fromInt = (from == null ? 0 : from);
            int countInt = (count == null ? 10 : count);
            String[] keywordList = keywords.split(",");
            ArrayList<String> keywordArrayList = new ArrayList<>();
            for (String str : keywordList) {
                keywordArrayList.add(str.trim());
            }
            criteria.keyword = keywordArrayList;
            criteria.setDeleted(false);

            List<AppService.AppSearchResultModel> appSearchResultModelList = AppService.Instance.list(criteria, fromInt, countInt, "app.creationDate", false, session);

            return setAndReturnProductVOs(request, appSearchResultModelList);

        } catch (Exception ex) {
            logger.error("Error occured in AppStoreRestController ", ex);
            if (ex instanceof PreProcessorException) {
                throw (RuntimeException) ex;
            } else {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ErrorPhrases.GENERAL_ERROR.getMessage(), ex);
            }
        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/isFavoriteApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO isFavoriteApp(@RequestParam(required = true) String packageName,
                                    @RequestParam(required = true) String jwtToken) {
        ResponseVO responseVO = new ResponseVO();
        JWTService.JWTUserClass jwtUser = null;
        Session session = null;

        if (packageName == null || packageName.trim().equals("")) {
            responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
            responseVO.setResult(ResultStatus.APP_NOT_FOUND.toString());
            return responseVO;
        }
        jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);

        if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
            return responseVO;
        }
        try {
            if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL) && responseVO.getResult() != null & !responseVO.getResult().trim().equals("")) {
                session = HibernateUtil.getCurrentSession();

                OSType osType = preProcess_OSTYPE();
                App app = AppService.Instance.getAppByPackageName(osType, packageName.trim(), false, session);
                if (app == null) {
                    responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
                    responseVO.setResult(ResultStatus.APP_NOT_FOUND.toString());
                    return responseVO;
                }

                String favoriteAppIdStr = getFavoriteAppIdByUserIdAndAppId(jwtUser.getUserId(), app.getId().toString());

                AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
                favoriteAppCriteria.setId(favoriteAppIdStr);

                boolean alreadySetFavorite = checkFavoriteApp(favoriteAppCriteria);

                responseVO.setResult("false");
                if (alreadySetFavorite) {
                    responseVO.setResult("true");
                }
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            }

        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
            return responseVO;

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/setFavoriteApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO setFavoriteApp(
            @RequestParam(required = true) String packageName,
            @RequestParam(required = true) String jwtToken,
            @RequestParam(required = true) boolean setFavorite
    ) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        if (packageName == null || packageName.trim().equals("")) {
            responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
            responseVO.setResult(ResultStatus.APP_NOT_FOUND.toString());
            return responseVO;
        }

        try {
            JWTService.JWTUserClass jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);

            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            session = HibernateUtil.getCurrentSession();

            if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                OSType osType = preProcess_OSTYPE();
                App app = AppService.Instance.getAppByPackageName(osType, packageName.trim(), false, session);
                if (app == null) {
                    responseVO.setResultStatus(ResultStatus.APP_NOT_FOUND);
                    responseVO.setResult(ResultStatus.APP_NOT_FOUND.toString());
                    return responseVO;
                }


                String favoriteAppIdStr = getFavoriteAppIdByUserIdAndAppId(jwtUser.getUserId(), app.getId().toString());

                AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
                favoriteAppCriteria.setId(favoriteAppIdStr);

                boolean alreadySetFavorite = checkFavoriteApp(favoriteAppCriteria);

                JestResult jestResult = null;
                if (setFavorite) {
                    if (alreadySetFavorite) {
                        setOkResponseVo(setFavorite, responseVO);
                    } else {
                        AppElasticService.FavoriteAppVO favoriteAppVOForInsert = new AppElasticService.FavoriteAppVO();
                        favoriteAppVOForInsert.setUserId(jwtUser.getUserId());
                        favoriteAppVOForInsert.setAppId(app.getId().toString());
                        favoriteAppVOForInsert.setModifyDateTime(DateTime.now().getTime());
                        favoriteAppVOForInsert.setId(favoriteAppIdStr);
                        jestResult = AppElasticService.Instance.insertFavoriteAppVO(favoriteAppVOForInsert);
                        if (jestResult != null && jestResult.isSucceeded()) {
                            setOkResponseVo(setFavorite, responseVO);
                        } else {
                            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                            responseVO.setResult(jestResult.getErrorMessage());
                        }
                    }

                } else {
                    if (!alreadySetFavorite) {
                        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                        responseVO.setResult(AppStorePropertyReader.getString("error.favoriteApp.favoriteAppNotFound"));
                    } else {
                        AppElasticService.Instance.deleteFavoriteApp(favoriteAppCriteria);
                        setOkResponseVo(setFavorite, responseVO);
                    }
                }
            }

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;

    }

    private String getFavoriteAppIdByUserIdAndAppId(String userId, String appId) {
        StringBuffer favoriteAppId = new StringBuffer();
        favoriteAppId.append(userId);
        favoriteAppId.append("-");
        favoriteAppId.append(appId);
        return favoriteAppId.toString();
    }

    private boolean checkFavoriteApp(AppElasticService.FavoriteAppCriteria favoriteAppCriteria) throws Exception {

        List<AppElasticService.FavoriteAppVO> favoriteAppVOList = AppElasticService.Instance.getFavoriteApp(favoriteAppCriteria, 0, -1, "userId", false);
        boolean alreadySetFavorite = (favoriteAppVOList != null && !favoriteAppVOList.isEmpty() ? true : false);

        return alreadySetFavorite;
    }

    private void setOkResponseVo(@RequestParam(required = true) boolean setFavorite, ResponseVO responseVO) {
        responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        responseVO.setResult(setFavorite ? "true" : "false");
    }


    @RequestMapping(value = "/getFavoriteApp", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO getFavoriteApp(@RequestParam(required = true) String jwtToken) {
        ResponseVO responseVO = getFavoriteAppIds(jwtToken);
        Session session = null;
        try {

            if (responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {

                session = HibernateUtil.getCurrentSession();

                JSONArray jsonArray = new JSONArray(responseVO.getResult());
                responseVO.setResult("[]");
                if (jsonArray != null && jsonArray.length() != 0) {
                    ArrayList<ProductVO> productVOArrayList = new ArrayList<>();

                    for (Object object : jsonArray) {
                        JSONObject jsonObject = (JSONObject) object;
                        if (jsonObject != null && jsonObject.has("appId")) {
                            String appId = (String) jsonObject.get("appId");
                            App app = (App) session.get(App.class, Long.valueOf(appId));
                            if (app != null) {
                                ProductVO productVO = ProductVO.buildProductVO(app);
                                productVOArrayList.add(productVO);
                            }
                        }
                    }

                    responseVO.setResult(JsonUtil.getJson(productVOArrayList));
                }
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            }

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return responseVO;
    }


    @RequestMapping(value = "/getJwtToken", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getJwtToken(@RequestParam(required = true) String fanapSSOToken) {
        ResponseVO responseVO = new ResponseVO();
        try {
            JSONObject fanapSSOTokenJson = new JSONObject(URLDecoder.decode(fanapSSOToken));
            String jwtToken = null;

            if (fanapSSOTokenJson.has("id_token")) {
                jwtToken = fanapSSOTokenJson.getString("id_token");
            }

            responseVO.setResult(jwtToken);
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

        } catch (Exception e) {
            responseVO.setResult(null);
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        }

        return responseVO.getResult();
    }


    @RequestMapping(value = "/getOrganizationList", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO getOrganizationList(@RequestParam(required = false) String from, @RequestParam(required = false) String count, @RequestParam(required = false) String nickName, @RequestParam(required = false) String englishFullName,
                                          @RequestParam(required = false) String fullName, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        try {
            Session session = HibernateUtil.getCurrentSession();
            List<Organization> organizationList = new ArrayList<>();
            OrgService.OrgCriteria orgCriteria = new OrgService.OrgCriteria();
            int first = 0;
            int size = -1;
            if (from != null && !from.trim().equals("")) {
                first = Integer.valueOf(from.trim());
            }
            if (count != null && !count.trim().equals("")) {
                size = Integer.valueOf(count.trim());
            }
            if (fullName != null && !fullName.trim().equals("")) {
                orgCriteria.setFullName(fullName.trim());
            }

            if (englishFullName != null && !englishFullName.trim().equals("")) {
                orgCriteria.setEnglishFullName(englishFullName.trim());
            }
            if (nickName != null && !nickName.trim().equals("")) {
                orgCriteria.setNickName(nickName.trim());
            }
            organizationList = OrgService.Instance.list(orgCriteria, first, size, null, true, session);

            List<OrgService.OrganizationVo> orgVoList = organizationList.stream()
                    .map(organization -> OrgService.buildOrganizationVo(organization)).collect(Collectors.toList());
            JSONArray jsonArray = new JSONArray(orgVoList);
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(jsonArray.toString());
        } catch (Exception e) {
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
            responseVO.setResult(e.getMessage());
        }

        return responseVO;
    }

    public ResponseVO getFavoriteAppIds(String jwtToken) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        JWTService.JWTUserClass jwtUser = null;
        if (jwtToken != null && !jwtToken.equals("")) {
            jwtUser = UserService.Instance.getJwtUserByGivenToken(jwtToken, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
        }
        session = HibernateUtil.getCurrentSession();
        try {
            AppElasticService.FavoriteAppCriteria favoriteAppCriteria = new AppElasticService.FavoriteAppCriteria();
            favoriteAppCriteria.setUserId(String.valueOf(jwtUser.getUserId()));
            List<AppElasticService.FavoriteAppVO> favoriteAppVOS = AppElasticService.Instance.getFavoriteApp(favoriteAppCriteria, 0, -1, "userId", false);
            responseVO.setResult(JsonUtil.getJson(favoriteAppVOS));
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return responseVO;

    }


    private List<AppService.AppSearchResultModel> getAppSearchResultModels(String from, String count, Session session) throws Exception {
        OSType osType = preProcess_OSTYPE();
        List<OSType> osTypeList = new ArrayList<>();
        osTypeList.add(osType);
        AppService.AppSearchCriteria criteria = new AppService.AppSearchCriteria();
        Integer beforeDate = Integer.valueOf(ConfigUtil.getProperty(ConfigUtil.APP_DATE_TIME_THRESHHOLD));
        DateTime[] dateTimeRange = {DateTime.beforeNow(beforeDate), DateTime.now()};
        criteria.creationDateTime = dateTimeRange;
        criteria.osType = osTypeList;
        List<PublishState> publishStateArrayList = new ArrayList<>();
        publishStateArrayList.add(PublishState.PUBLISHED);
        criteria.publishStates = publishStateArrayList;
        int fromInt = (from == null || from.trim().equals("") ? 0 : Integer.valueOf(from));
        int countInt = (count == null || count.trim().equals("") ? 10 : Integer.valueOf(count));
        criteria.setDeleted(false);
        return AppService.Instance.list(criteria, fromInt, countInt, "app.id", false, session);
    }


    @RequestMapping(value = "/userInfo", headers = "Accept=*/*", method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public ResponseVO userInfo(@RequestParam(required = true) String userName, HttpServletRequest request, HttpServletResponse response) {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
        Session session = null;
        try {

            if (userName != null && !userName.trim().equals("")) {
                Long userIdLong = SSOUserService.Instance.getUserIdByUserName(userName);
                UserVO userVO = new UserVO();
                session = HibernateUtil.getNewSession();

                User registeredUser = UserService.Instance.findUserWithUserId(userIdLong, session);
                if (registeredUser != null) {
                    userVO = UserVO.buildUserVo(registeredUser);
                    userVO.setRegisterInAppStore(true);
                } else {
                    userVO.setUserName(userName);
                    userVO.setUserId(userIdLong);
                    userVO.setRegisterInAppStore(false);
                }
                String jsonResult = JsonUtil.getJson(userVO);
                responseVO.setResult(jsonResult);
                responseVO.setResultStatus(ResultStatus.SUCCESSFUL);

            }

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


    public static void main(String[] args) throws Exception {

//        File file = new File("D:/android-release-v1.apk");
//        Map<String, String> st = FileServerService.Instance.uploadFilesToServer(Arrays.asList(file), new FileServerService.UploadDescriptor());
//        System.out.println("");

//        String type = "application/x-www-form-urlencoded";
//
//        URL u = new URL("http://localhost:9090/appStore/restAPI/spring/service/getDeltaUpdatePackage");
//        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
//        conn.setDoOutput(true);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty( "OSTYPE", "ANDROID");
//        OutputStream os = conn.getOutputStream();

//        List<ProductRequestVO> productRequestVOs = new ArrayList<>();
//        ProductRequestVO productRequestVO = new ProductRequestVO();
//        productRequestVO.setAppPackageName("com.football");
//        productRequestVO.setVersionCode("1");
//        productRequestVOs.add(productRequestVO);
//
//        ProductRequestVOs productRequestVOs1 = new ProductRequestVOs();
//        productRequestVOs1.setProductRequestVOList(productRequestVOs);

//        ObjectMapper mapper = new ObjectMapper();
//        StringWriter stringWriter = new StringWriter();
//        mapper.writer().writeValue(stringWriter, "com.ionicframework.apptemplate909794");
//
//        os.write(("packageName=" + stringWriter.toString()).getBytes());
//        os.flush();
//
//
//        mapper = new ObjectMapper();
//        stringWriter = new StringWriter();
//        mapper.writer().writeValue(stringWriter, "18");
//
//        os.write(("previousVersion=" + stringWriter.toString()).getBytes());
//        os.flush();
//
//        mapper = new ObjectMapper();
//        stringWriter = new StringWriter();
//        mapper.writer().writeValue(stringWriter, "19");
//
//        os.write(("forwardVersion=" + stringWriter.toString()).getBytes());
//        os.flush();
//
////
//        InputStream inputStream = conn.getInputStream();
//        int byt;
//        byte[] bytArray = new byte[inputStream.available()];
//        inputStream.read(bytArray);

//        System.out.println(new String(bytArray));

//        String url = "http://localhost:8080/appStore/restAPI/spring/service/getImageGalleries?packageName=com.football";
//
//        HttpClient client = HttpClientBuilder.create().build();
//        HttpGet get = new HttpGet(url);
//
//        // add header
//        get.setHeader("OSTYPE", "ANDROID");
//        get.setHeader("content-type", "application/x-www-form-urlencoded");
//        get.setHeader("accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");


//        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        for(int  i = 0; i < 2; i++) {
//
//
//
//        }
//        ObjectMapper mapper = new ObjectMapper();
//        StringWriter stringWriter = new StringWriter();
//        mapper.writer().writeValue(stringWriter, productRequestVOs);

//        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//        params.add(new BasicNameValuePair("requestVOs", stringWriter.toString()));
//
//        post.setEntity(new UrlEncodedFormEntity(params));

//        HttpResponse response = client.execute(get);
//        System.out.println("Response Code : "
//                + response.getStatusLine().getStatusCode());
//
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppostCategories = new HttpPost("http://172.16.105.67:8080/appStore/restAPI/spring/service/getCategories");
        HttpResponse responseCategories = httpclient.execute(httppostCategories);
        HttpEntity entityCategories = responseCategories.getEntity();


        HttpPost httppostUser = new HttpPost("http://172.16.105.67:8080/appStore/restAPI/spring/service/user/addUser");
        HttpResponse responseUser = httpclient.execute(httppostUser);
        HttpEntity entityUser = responseUser.getEntity();

        HttpPost httppost = new HttpPost("http://172.16.3.5:8080/appStore/restAPI/spring/service/setInstallInfoForApp");

//        HttpGet httppost = new HttpGet("http://172.16.3.5:8080/appStore/restAPI/spring/service/getApp");

        httppost.setHeader("OSTYPE", "ANDROID");
        httppost.setHeader("content-type", "application/x-www-form-urlencoded");
        httppost.setHeader("accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("title", "کارتابل");

////
// Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("appInstallInfoVO", "{\"packageList\":[\"com.monotype.android.font.rosemary\",\"com.sec.android.app.DataCreate\",\"com.sec.android.providers.mapcon\",\"com.skype.raider\",\"com.gd.mobicore.pa\",\"com.sec.android.widgetapp.samsungapps\",\"com.google.android.youtube\",\"com.samsung.android.app.galaxyfinder\",\"com.samsung.android.themestore\",\"com.sec.android.app.chromecustomizations\",\"com.android.providers.telephony\",\"com.sec.android.app.parser\",\"com.sec.android.providers.iwlansettings\",\"com.samsung.android.applock\",\"com.google.android.googlequicksearchbox\",\"com.android.providers.calendar\",\"com.osp.app.signin\",\"com.samsung.clipboardsaveservice\",\"com.sec.automation\",\"com.android.providers.media\",\"com.google.android.onetimeinitializer\",\"com.android.wallpapercropper\",\"com.samsung.android.provider.shootingmodeprovider\",\"com.sec.android.app.safetyassurance\",\"com.sec.factory.camera\",\"org.simalliance.openmobileapi.service\",\"com.sec.usbsettings\",\"com.samsung.android.easysetup\",\"com.android.documentsui\",\"com.android.externalstorage\",\"com.sec.android.easyonehand\",\"com.sec.factory\",\"com.android.htmlviewer\",\"com.android.mms.service\",\"com.sec.android.wallpapercropper2\",\"com.android.providers.downloads\",\"com.khan.fsarseifi.khanacademy4\",\"com.sec.android.easyMover.Agent\",\"com.samsung.ucs.agent.boot\",\"com.wsomacp\",\"com.sec.android.Kies\",\"com.samsung.faceservice\",\"com.sec.android.app.voicenote\",\"com.sec.android.app.easylauncher\",\"com.samsung.knox.rcp.components\",\"com.monotype.android.font.foundation\",\"com.sec.android.widgetapp.easymodecontactswidget\",\"com.samsung.android.email.provider\",\"com.samsung.android.intelligenceservice2\",\"com.sec.di.SmartSelfShot\",\"com.samsung.android.MtpApplication\",\"com.sec.android.app.factorykeystring\",\"com.sec.android.app.samsungapps\",\"com.sec.android.emergencymode.service\",\"com.google.android.configupdater\",\"com.sec.android.app.wlantest\",\"com.microsoft.office.excel\",\"com.sec.android.app.billing\",\"com.sec.android.app.minimode.res\",\"com.sec.epdgtestapp\",\"com.android.defcontainer\",\"com.sec.android.daemonapp\",\"com.sec.sve\",\"com.sec.enterprise.knox.attestation\",\"com.android.vending\",\"com.android.pacprocessor\",\"com.sec.android.app.popupuireceiver\",\"com.sec.android.AutoPreconfig\",\"com.microsoft.skydrive\",\"com.samsung.android.SettingsReceiver\",\"com.sec.android.app.soundalive\",\"com.sec.android.providers.security\",\"com.sec.android.provider.badge\",\"com.android.certinstaller\",\"com.samsung.android.securitylogagent\",\"com.android.carrierconfig\",\"com.google.android.marvin.talkback\",\"com.samsung.android.app.assistantmenu\",\"com.samsung.android.communicationservice\",\"com.samsung.SMT\",\"com.samsung.cmh\",\"android\",\"com.android.contacts\",\"com.samsung.hs20provider\",\"com.samsung.android.sm.devicesecurity\",\"com.android.mms\",\"com.android.stk\",\"com.sec.knox.foldercontainer\",\"com.android.backupconfirm\",\"com.samsung.klmsagent\",\"com.sec.android.app.SecSetupWizard\",\"com.android.statementservice\",\"com.google.android.gm\",\"com.tonyodev.fetchdemo\",\"com.sec.android.app.hwmoduletest\",\"com.sec.bcservice\",\"com.android.calendar\",\"com.sec.modem.settings\",\"com.sec.android.app.sysscope\",\"com.sec.android.app.wallpaperchooser\",\"com.samsung.android.providers.context\",\"com.sec.android.app.servicemodeapp\",\"com.sec.android.preloadinstaller\",\"com.google.android.setupwizard\",\"com.sec.android.gallery3d\",\"com.android.providers.settings\",\"com.sec.app.TransmitPowerService\",\"com.android.sharedstoragebackup\",\"com.samsung.android.app.colorblind\",\"com.google.android.music\",\"com.android.printspooler\",\"com.samsung.storyservice\",\"com.android.dreams.basic\",\"com.android.incallui\",\"com.sec.app.samsungprintservice\",\"com.farsitel.bazaar\",\"com.sec.android.app.dictionary\",\"com.android.inputdevices\",\"com.estrongs.android.pop\",\"com.sec.esdk.elm\",\"com.android.stk2\",\"com.samsung.android.app.simplesharing\",\"com.sec.smartcard.manager\",\"com.sec.android.Preconfig\",\"com.google.android.apps.docs\",\"com.google.android.apps.maps\",\"com.sec.enterprise.mdm.vpn\",\"com.samsung.android.weather\",\"com.microsoft.office.word\",\"com.sec.android.inputmethod\",\"com.sec.android.app.clockpackage\",\"com.sec.android.RilServiceModeApp\",\"com.google.android.webview\",\"com.sec.android.app.simsettingmgr\",\"com.microsoft.office.powerpoint\",\"com.sec.enterprise.knox.myknoxsetupwizard\",\"com.fanap.midhco.navin\",\"com.android.server.telecom\",\"com.google.android.syncadapters.contacts\",\"com.samsung.android.clipboarduiservice\",\"com.android.keychain\",\"com.android.chrome\",\"com.samsung.android.asksmanager\",\"com.samsung.android.themecenter\",\"com.google.android.packageinstaller\",\"com.samsung.android.sm\",\"com.google.android.gms\",\"com.google.android.gsf\",\"com.google.android.tts\",\"com.android.calllogbackup\",\"com.google.android.partnersetup\",\"com.sec.android.diagmonagent\",\"com.google.android.videos\",\"com.sec.spp.push\",\"com.sec.android.app.myfiles\",\"com.android.proxyhandler\",\"com.samsung.android.allshare.service.fileshare\",\"com.sec.android.mimage.photoretouching\",\"com.sec.android.app.launcher\",\"com.google.android.feedback\",\"app.greyshirts.sslcapture\",\"com.google.android.apps.photos\",\"com.google.android.syncadapters.calendar\",\"com.android.managedprovisioning\",\"com.sec.android.providers.tasks\",\"com.sec.android.app.sbrowser\",\"com.monotype.android.font.chococooky\",\"com.android.dreams.phototable\",\"b4a.example\",\"com.sec.android.service.health\",\"com.samsung.safetyinformation\",\"com.sec.android.app.ringtoneBR\",\"com.android.providers.partnerbookmarks\",\"com.google.android.gsf.login\",\"com.cleanmaster.sdk\",\"com.samsung.android.app.accesscontrol\",\"com.android.wallpaper.livepicker\",\"com.samsung.android.beaconmanager\",\"com.sec.enterprise.mdm.services.simpin\",\"com.ionicframework.apptemplate909794\",\"com.samsung.android.app.FileShareClient\",\"com.sec.android.app.popupcalculator\",\"com.sec.android.soagent\",\"com.samsung.android.fmm\",\"com.samsung.android.mdm\",\"com.sec.android.app.shealth\",\"com.sec.phone\",\"com.samsung.knox.appsupdateagent\",\"com.google.android.backuptransport\",\"com.sec.knox.knoxsetupwizardclient\",\"com.samsung.android.scloud\",\"com.samsung.android.app.soundpicker\",\"com.sec.app.RilErrorNotifier\",\"com.android.bookmarkprovider\",\"com.android.settings\",\"com.samsung.android.app.notes\",\"com.sec.android.app.bluetoothtest\",\"com.samsung.android.sm.policy\",\"com.sec.android.emergencylauncher\",\"com.w3android.husinsajadi.myapplication33\",\"com.sec.hearingadjust\",\"com.samsung.android.dlp.service\",\"com.samsung.android.bbc.bbcagent\",\"com.sec.android.splitsound\",\"com.wssnps\",\"com.samsung.android.app.watchmanagerstub\",\"com.policydm\",\"com.samsung.android.app.FileShareServer\",\"com.enhance.gameservice\",\"com.android.vpndialogs\",\"com.google.android.talk\",\"com.samsung.dcmservice\",\"com.android.phone\",\"com.android.shell\",\"com.android.providers.userdictionary\",\"com.sec.enterprise.knox.cloudmdm.smdms\",\"com.wssyncmldm\",\"com.android.location.fused\",\"com.samsung.android.sm.provider\",\"com.android.systemui\",\"com.sec.android.app.personalization\",\"com.monotype.android.font.cooljazz\",\"com.android.bluetoothmidiservice\",\"com.sec.android.provider.logsprovider\",\"com.samsung.aasaservice\",\"com.sec.android.app.fm\",\"com.sec.android.app.mt\",\"com.sec.android.provider.emergencymode\",\"com.samsung.android.fingerprint.service\",\"com.sec.knox.switcher\",\"com.sec.android.app.camera\",\"com.samsung.android.tzdata.update_M\",\"com.android.bluetooth\",\"com.android.providers.contacts\",\"com.sec.android.app.magnifier\",\"com.sec.android.widgetapp.webmanual\",\"com.samsung.sec.android.application.csc\",\"com.android.captiveportallogin\",\"com.samsung.upsmtheme\",\"com.android.android\",\"com.samsung.android.coreapps\",\"com.samsung.android.video\",\"com.samsung.location\"],\"deviceId\":\"id01\"}"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//        params.add(new BasicNameValuePair("packageName", "com.farsitel.bazaar"));
//        params.add(new BasicNameValuePair("previousVersion", "16"));
//        params.add(new BasicNameValuePair("forwardVersion", "17"));
//        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//
////Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                int b;
                byte[] bn = new byte[256];
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while ((b = instream.read(bn)) != -1) {
                    bout.write(bn);
                }

                System.out.println(new String(bout.toByteArray()));
            } finally {
                instream.close();
            }
        }
    }
}
