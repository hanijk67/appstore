package com.fanap.midhco.appstore.restControllers.controllers;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.applicationUtils.JsonUtil;
import com.fanap.midhco.appstore.entities.AppCategory;
import com.fanap.midhco.appstore.entities.File;
import com.fanap.midhco.appstore.entities.StereoType;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.restControllers.vos.CategoryVO;
import com.fanap.midhco.appstore.restControllers.vos.ResponseVO;
import com.fanap.midhco.appstore.wicketApp.ResultStatus;
import com.fanap.midhco.appstore.service.BaseEntityService;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.appstore.service.appcategory.AppCategoryService;
import com.fanap.midhco.appstore.service.fileServer.FileServerService;
import com.fanap.midhco.appstore.service.user.UserService;
import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by A.Moshiri on 4/7/2018.
 */
@RestController
@RequestMapping("/service/category")
public class AppCategoryRestController {
    Logger logger = Logger.getLogger(AppCategoryRestController.class);

    @RequestMapping(value = "/listCategory", method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json")
    @ResponseBody
    public ResponseVO listCategory(@RequestParam(required = false) CategoryVO categoryVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            UserService.Instance.checkAccessibility(requesterUser, Access.APPCATEGORY_LIST, responseVO);
            if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            List<AppCategory> appCategoryList = new ArrayList<>();
            Integer fromIndex = PrincipalUtil.getFromIndex();
            Integer countIndex = PrincipalUtil.getCountIndex();
            Boolean getResultCount = PrincipalUtil.getResultCount();
            Boolean isAscending = PrincipalUtil.isAscending();


            AppCategoryService.AppCategoryCriteria appCategoryCriteria = new AppCategoryService.AppCategoryCriteria();
            session = HibernateUtil.getCurrentSession();
            Object resultObject = null;

            if (categoryVO != null) {
                boolean hasValue = false;
                if (categoryVO.getId() != null) {
                    appCategoryCriteria.setId(categoryVO.getId());
                    hasValue=true;
                }
                if (categoryVO.getAssignable() != null) {
                    appCategoryCriteria.setAssignable(categoryVO.getAssignable());
                    hasValue=true;
                }
                if (categoryVO.getEnabled() != null) {
                    appCategoryCriteria.setEnabled(categoryVO.getEnabled());
                    hasValue=true;
                }
                if (categoryVO.getCategoryName() != null && !categoryVO.getCategoryName().trim().equals("")) {
                    appCategoryCriteria.setCategoryName(categoryVO.getCategoryName());
                    hasValue=true;
                }
                if (categoryVO.getParentId() != null) {
                    appCategoryCriteria.setParentId(categoryVO.getParentId());
                    hasValue=true;
                }
                if (!hasValue) {
                    responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
                    responseVO.setResult("");
                    return  responseVO;
                }
            }

            String sortBy = PrincipalUtil.getSortBy();
            String sortProperties = null;
            if (sortBy != null) {
                try {
                    List<String> declaredField = new AppCategory().getDeclaredField();
                    if (declaredField.contains(sortBy)) {
                        sortProperties = sortBy;
                    }
                } catch (Exception e) {
                    sortProperties = null;
                }
            }
            if(getResultCount){
                Long count = AppCategoryService.Instance.count(appCategoryCriteria,session);
                resultObject = count;
            }else {
                appCategoryList = AppCategoryService.Instance.list(appCategoryCriteria, fromIndex, countIndex, sortProperties, isAscending, session);
                if (appCategoryList != null && !appCategoryList.isEmpty()) {
                    Stream<CategoryVO> appCategoryVOStream = appCategoryList.stream().map(appCategory -> CategoryVO.buildCategoryVO(appCategory));
                    resultObject = appCategoryVOStream.collect(Collectors.<CategoryVO>toList());

                } else {
                    resultObject = new ArrayList<CategoryVO>();
                }
            }
            responseVO.setResultStatus(ResultStatus.SUCCESSFUL);
            responseVO.setResult(JsonUtil.getJson(resultObject));

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

    @RequestMapping(value = "addCategory", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO addCategory(@RequestParam(required = true) CategoryVO categoryVO) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (categoryVO != null && categoryVO.getId() != null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.APPCATEGORY_ADD, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveCategoryToDataBase(categoryVO);
            }

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);

        }
        return responseVO;
    }

    @RequestMapping(value = "editCategory", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseVO editCategory(@RequestParam(required = true) CategoryVO categoryVO) {
        ResponseVO responseVO = new ResponseVO();

        try {
            User requesterUser = null;
            requesterUser = UserService.Instance.getUserFromJwtToken(responseVO);
            if (requesterUser == null || responseVO == null || !responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                return responseVO;
            }
            if (categoryVO == null || categoryVO.getId() == null) {
                responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                responseVO.setResult(ResultStatus.INVALID_DATA.toString());
            } else {
                UserService.Instance.checkAccessibility(requesterUser, Access.APPCATEGORY_EDIT, responseVO);
                if (!responseVO.getResultStatus().equals(ResultStatus.SUCCESSFUL)) {
                    return responseVO;
                }
                PrincipalUtil.setCurrentUser(requesterUser);
                responseVO = saveCategoryToDataBase(categoryVO);
            }

        } catch (Exception e) {
            responseVO.setResult(e.getMessage());
            responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);

        }
        return responseVO;
    }


    private ResponseVO saveCategoryToDataBase(CategoryVO categoryVO) {
        ResponseVO responseVO = new ResponseVO();
        Session session = null;
        try {

            AppCategory appCategory = null;
            session = HibernateUtil.getCurrentSession();
            if (categoryVO.getId() != null) {
                appCategory = (AppCategory) session.get(AppCategory.class, categoryVO.getId());
                if (appCategory == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_APP_CATEGORY);
                    responseVO.setResult(ResultStatus.INVALID_APP_CATEGORY.toString());
                }
            } else {
                boolean inValidAppCategory = AppCategoryService.Instance.checkAppCategoryVO(categoryVO);
                if (inValidAppCategory) {
                    responseVO.setResultStatus(ResultStatus.NULL_DATA);
                    responseVO.setResult(ResultStatus.NULL_DATA.toString());
                    return responseVO;
                }
                appCategory = new AppCategory();
            }
            if (categoryVO.getParentId() != null) {
                AppCategory parentCategory = (AppCategory) session.get(AppCategory.class, categoryVO.getParentId());
                if (parentCategory == null) {
                    responseVO.setResultStatus(ResultStatus.INVALID_DATA);
                    responseVO.setResult(ResultStatus.INVALID_DATA.toString());
                    return responseVO;
                } else {
                    appCategory.setParent(parentCategory);
                }
            }

            if (categoryVO.getCategoryName() != null && !categoryVO.getCategoryName().trim().equals("")) {
                AppCategoryService.AppCategoryCriteria appCategoryCriteria = new AppCategoryService.AppCategoryCriteria();
                appCategoryCriteria.setCategoryName(categoryVO.getCategoryName());
                List<AppCategory> appCategoryList = AppCategoryService.Instance.list(appCategoryCriteria , 0 ,-1 , null ,false ,session);
                if (categoryVO.getId()==null) {
                    if(appCategoryList!=null && appCategoryList.size()>0){
                        responseVO.setResultStatus(ResultStatus.APP_CATEGORY_EXIST);
                        responseVO.setResult(ResultStatus.APP_CATEGORY_EXIST.toString());
                        return responseVO;
                    }
                }else {
                    for(AppCategory appCategoryInList : appCategoryList){
                        if(!appCategoryInList.getId().equals(appCategory.getId())){
                            responseVO.setResultStatus(ResultStatus.APP_CATEGORY_EXIST);
                            responseVO.setResult(ResultStatus.APP_CATEGORY_EXIST.toString());
                            return responseVO;
                        }
                    }
                }
                appCategory.setCategoryName(categoryVO.getCategoryName().trim());
            }
            if (categoryVO.getEnabled() != null) {
                appCategory.setEnabled(categoryVO.getEnabled());
            }
            if (categoryVO.getAssignable() != null) {
                appCategory.setAssignable(categoryVO.getAssignable());
            }
            if (categoryVO.getIconPath() != null && !categoryVO.getIconPath().trim().equals("")) {

                String tempFileLocation = null;
                tempFileLocation = FileServerService.Instance.copyFileFromServerToTemp(categoryVO.getIconPath());
                java.io.File file = new java.io.File(tempFileLocation);

                BufferedImage bufferedImage = ImageIO.read(new java.io.File(file.getPath()));
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                int categoryIconMaxPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_CATEGORY_ICON_MAX_PIXEL));
                int categoryIconMinPixel = Integer.parseInt(ConfigUtil.getProperty(ConfigUtil.APP_CATEGORY_ICON_MIN_PIXEL));
                StringBuffer sizeError = new StringBuffer();
                if (categoryIconMinPixel > width || categoryIconMaxPixel < width) {
                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);
                    sizeError.append(AppStorePropertyReader.getString("error.appCategory.icon.width.pixel").replace("${min}", String.valueOf(categoryIconMinPixel))
                            .replace("${max}", String.valueOf(categoryIconMaxPixel)));
                    sizeError.append(System.lineSeparator());
                    responseVO.setResult(sizeError.toString());
                    return responseVO;
                }
                if (categoryIconMinPixel > height || categoryIconMaxPixel < height) {

                    responseVO.setResultStatus(ResultStatus.UNSUCCESSFUL);

                    sizeError.append(AppStorePropertyReader.getString("error.appCategory.icon.height.pixel").replace("${min}", String.valueOf(categoryIconMinPixel))
                            .replace("${max}", String.valueOf(categoryIconMaxPixel)));
                    responseVO.setResult(sizeError.toString());
                    return responseVO;
                }

                File iconFile = new File();
                iconFile.setStereoType(StereoType.THUMB_FILE);
                iconFile.setFilePath(categoryVO.getIconPath());
                iconFile.setFileName(FileServerService.Instance.getFileNameFromFilePath(categoryVO.getIconPath()));
                BaseEntityService.Instance.saveOrUpdate(iconFile, session);
                appCategory.setIconFile(iconFile);

            }
            Transaction tx = session.beginTransaction();
            BaseEntityService.Instance.saveOrUpdate(appCategory, session);
            tx.commit();

            CategoryVO convertedCategoryVO = CategoryVO.buildCategoryVO(appCategory);
            String categoryStr = JsonUtil.getJson(convertedCategoryVO);
            categoryStr.replaceAll("\\\\", "");
            String replaceStr = categoryStr.replaceAll("\\\\", "").replaceAll("\\\"", "\"");
            responseVO.setResult(replaceStr);
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
}
