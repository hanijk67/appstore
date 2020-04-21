var packageListModule = (function () {
    var innerAppContext = "";

    var getRestEndPointPath = function (restEndPointName) {
        var siteLocation = window.location;
        return siteLocation.protocol + "//" + siteLocation.hostname + (siteLocation.port != null ? ":" + siteLocation.port : "") +
            (siteLocation.pathname != null ? siteLocation.pathname : "") + "restAPI/spring/service/" + restEndPointName;
    };

    var loadAppPackages = function(from, to, selectedOSType, osTypeListService, appService, callBackListener) {

        var osTypeList = [];

        async.waterfall([function (callback) {
            if (selectedOSType == null) {
                osTypeListService.getOSTypeList().then(function (osTypeData) {
                    osTypeData.forEach(function (value, index) {
                        osTypeList.push(value.osName.toUpperCase());
                    });
                    callback();
                });
            } else {
                osTypeList.push(selectedOSType.toUpperCase());
                callback();
            }
        }, function(callback) {
            appService.getNumberOfProducts(osTypeList).then(function(osTypeName2NumberMap) {
                console.log('recieved osTypeName2NumberMap is ', JSON.stringify(osTypeName2NumberMap));
                callback(null, osTypeName2NumberMap);
            });
        }, function(osTypeName2NumberMap, callback) {
            appService.getAllProducts(osTypeList, from, to).then(function(osTypeName2ProductMap) {
                console.log('recieved app list is ', osTypeName2ProductMap);

                var categoryList = [];
                var _size = _.size(osTypeName2ProductMap);
                var counter = 0;
                var defer = {};
                var promise = new Promise(function(resolve, reject) {
                    defer.resolve = function(data) {
                        resolve(data);
                    }

                    defer.reject = function(rejectCause) {
                        reject(rejectCause);
                    }
                });

                if(_size == 0) {
                    callBackListener.call(null, categoryList);
                    return;
                }

                async.series([function(callback) {
                    _.each(osTypeName2ProductMap, function(osTypeProductData, osTypeName) {
                        var category = {};
                        var innerCounter = 1;
                        category.categoryTitle = osTypeName;
                        category.count = osTypeName2NumberMap[osTypeName];
                        category.appList = [];

                        categoryList.push(category);

                        _.each(osTypeProductData, function(productData, index) {
                            var packageName = productData.packageName;

                            var downloadLink = productData.downLoadLink;
                            if(downloadLink) {
                                get_filesize(downloadLink).then(function(productSize) {
                                    productData.size = productSize;
                                }).then(function() {
                                    appService.getProductDescription(osTypeName.toUpperCase(), packageName).then(function(descriptionData) {
                                        console.log('description data recieved is ', descriptionData);
                                        productData.description = descriptionData.description;

                                        category.appList.push(productData);

                                        if(innerCounter == osTypeProductData.length) {
                                            counter++;
                                            if(counter == _size)
                                                defer.resolve();
                                        } else
                                            innerCounter++;
                                    });
                                });
                            }
                        });
                    });

                    promise.then(callback);
                }, function(callback) {
                    callBackListener.call(null, categoryList);
                }]);
            });
        }
        ]);
    }

    return {
        init: function (appContext) {
            innerAppContext = appContext;
            //$('.owl-carousel').owlCarousel();

            var osTypeListWebServicePath = getRestEndPointPath("getOSTypeList");

            var listPopulatorModule = angular.module('listPopulators', []);

            listPopulatorModule.factory("osTypeProvider", function () {
                var osType;
                return {
                    setOSType: function (value) {
                        osType = value;
                    },
                    getOSType: function () {
                        return osType;
                    }
                };
            });

            listPopulatorModule.controller('imageSliderController', ['$rootScope', '$scope', function($rootScope, $scope) {
                $scope.changeCurrentDiv = function(index) {
                    currentDiv(index);
                }

                $rootScope.$on('imageSliderShow', function(event, imageVOList) {
                    $scope.$applyAsync(function() {
                        $scope.imageVOList = imageVOList;
                    });

                    $('#imageSlider_modal').on('shown.bs.modal', function() {
                        showDivs(slideIndex);
                    });

                    $('#imageSlider_modal').modal('show');
                });
            }]);


            listPopulatorModule.controller('packageDetailController', ['$scope', '$rootScope', '$http' ,'$sce', function($scope, $rootScope, $http,$sce) {
                $scope.downloadPackage = function(app) {
                    downloadURI(app.downLoadLink, app.title);
                }

                $scope.showImageSlider = function(imageVOList) {
                    $scope.$emit('imageSliderShow', imageVOList);
                }

                $rootScope.$on('packageDetailShow', function(event, app) {
                    console.log();
                    var packageImageVOSEndpoint = getRestEndPointPath('getImageGalleries');

                    var osTypeName = app.osTypeName;
                    var packageName = app.packageName;
                    var versionCode = app.versionCode;

                    var changeLogVOSEndpoint = getRestEndPointPath("getChangeLog");
                    var changeLog ="";
                    var changeLogAppPackage ="";
                    $http.get(changeLogVOSEndpoint,
                        {headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}, params : {packageName:packageName,versionCode:versionCode}}).then(function (response) {
                        console.log('app package change Log is :  ', response.data);
                        changeLog = response.data;
                        if (changeLog!=null) {
                            changeLogAppPackage = changeLog.appPackageChangeLog;
                        }
                    });

                    $http.get(packageImageVOSEndpoint,
                        {headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}, params : {packageName:packageName}}).then(function (response) {
                        console.log('currently recieved imageVoList recieved ', response.data);
                        var imageVOList = response.data;
                        app.imageVOList = imageVOList;
                        $scope.$applyAsync(function() {
                            $scope.app = app;
                            $scope.changeLogAppPackage =$sce.trustAsHtml(changeLogAppPackage);
                            $('#package_detail_modal').modal('show');
                        });
                    });
                });
            }]);

            listPopulatorModule.controller("appContextController", ['$scope', '$rootScope', function($scope, $rootScope) {
                var siteLocation = window.location;
                $scope.loginLocation =
                    siteLocation.protocol + "//" + siteLocation.hostname + (siteLocation.port != null ? ":" + siteLocation.port : "") +
                    (siteLocation.pathname != null ? siteLocation.pathname : "") + "loginPage";
            }]);

            listPopulatorModule.controller("showCategoryController", ['$scope', '$rootScope', 'osTypeProvider' , 'osTypeListService','appService',
                function($scope, $rootScope, osTypeProvider, osTypeListService, appService) {

                    var _pageIndex=0;
                    var _pageSize=9;
                    var osTypeName = null;
                    var pageCount = 0;

                    var launchPageIndexLoad = function() {
                        var from = _pageIndex * _pageSize;

                        loadAppPackages(from, _pageSize, osTypeName, osTypeListService, appService, function(categoryList) {
                            $scope.$apply(function() {
                                console.log('recieved categories are ', categoryList);
                                var category = categoryList[0];

                                category.paginationNumbers = [];
                                pageCount = parseInt(category.count / _pageSize);
                                var remnant = category.count % _pageSize;

                                if(remnant != 0)
                                    pageCount += 1;

                                for(var i = 0; i < pageCount; i++) {
                                    category.paginationNumbers.push(i + 1);
                                }

                                if(_pageIndex == 0) {
                                    category.showPrev = false;
                                } else {
                                    category.showPrev = true;
                                }

                                if(_pageIndex == pageCount - 1) {
                                    category.showNext = false;
                                } else {
                                    category.showNext = true;
                                }

                                $scope._currentIndex = _pageIndex +1;
                                $scope.appCategoryList = categoryList;
                            });
                        });
                    }

                    $scope.launchAppDetailWindow = function(app) {
                        $scope.$emit('packageDetailShow', app);
                    }

                    $scope.showPrev = function() {
                        console.log('showPrev. index number is ', _pageIndex);
                        if(_pageIndex - 1 < 0)
                            return;
                        _pageIndex--;
                        launchPageIndexLoad();
                    }

                    $scope.showPageIndex = function(index) {
                        console.log('showPageIndex. index number is ', index);
                        if(_pageIndex == index -1)
                            return;
                        _pageIndex = index - 1;
                        launchPageIndexLoad();
                    }

                    $scope.showNext = function() {
                        console.log('showNext. index number is ', _pageIndex);
                        if(_pageIndex + 1 > (pageCount -1))
                            return;
                        _pageIndex++;
                        launchPageIndexLoad();
                    }

                    $rootScope.$on('showCategory', function (event, data) {
                        $rootScope.showCategoryAll = false;
                        $rootScope.showCategory = true;
                        osTypeName = data;
                        launchPageIndexLoad();
                    });
            }]);

            listPopulatorModule.controller('appListController', ['$scope', '$rootScope', 'osTypeProvider' , 'osTypeListService','appService', function ($scope, $rootScope, osTypeProvider, osTypeListService, appService) {
                $rootScope.showCategoryAll = true;
                $rootScope.showCategory = false;


                $scope.showCategory = function(appCategory) {
                    var osTypeName = appCategory.categoryTitle;
                    osTypeName = osTypeName.toUpperCase();

                    $scope.$emit('showCategory', osTypeName);
                }

                $scope.launchAppDetailWindow = function(app) {
                    $scope.$emit('packageDetailShow', app);
                }

                $rootScope.$on('osTypeChangeEvent', function (event, data) {
                    $rootScope.showCategoryAll = true;
                    $rootScope.showCategory = false;

                    var selectedOSTypeName = osTypeProvider.getOSType() != null ? osTypeProvider.getOSType().osName.toUpperCase() : null;

                    loadAppPackages(0, 3, selectedOSTypeName, osTypeListService, appService, function(categoryList) {
                        $scope.$apply(function() {
                            $scope.appCategoryList = categoryList;
                        });
                    });

                });

                $scope.$emit('osTypeChangeEvent');
            }]);

            listPopulatorModule.service('appService', ['$http', 'osTypeProvider', 'osTypeListService', function ($http, osTypeProvider, osTypeListService) {
                this.getNumberOfProducts = function (osTypeList) {
                    var numberOfProductsMap = {};

                    var defer = {};
                    var numberOfProductsPromise= new Promise(function(resolve, reject) {
                        defer.resolve = function(data) {
                            resolve(data);
                        };

                        defer.reject = function(rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    var getNumberOfProductsWebServiceEndPoint = getRestEndPointPath("getNumberOfProducts");
                    var counter = 1;

                    osTypeList.forEach(function (osTypeName, index) {
                        $http.get(getNumberOfProductsWebServiceEndPoint,
                            {headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}}).then(function (response) {
                            numberOfProductsMap[osTypeName] = response.data;
                            if(counter == osTypeList.length) {
                                defer.resolve(numberOfProductsMap);
                            } else {
                                counter++;
                            }
                        }, function (errorResponse) {
                            console.error(errorResponse);
                        });
                    });

                    return numberOfProductsPromise;
                },

                    this.getAllProducts = function(osTypeList, from, count) {

                        var productsMap = {};

                        var defer = {};
                        var productsPromise= new Promise(function(resolve, reject) {
                            defer.resolve = function(data) {
                                resolve(data);
                            };

                            defer.reject = function(rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getAllProductsWebServiceEndPoint = getRestEndPointPath("getAllProducts");
                        getAllProductsWebServiceEndPoint += "/" + from + "/" + count;
                        var counter = 1;

                        osTypeList.forEach(function (osTypeName, index) {
                            $http.get(getAllProductsWebServiceEndPoint,
                                {headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}}).then(function (response) {
                                productsMap[osTypeName] = response.data;
                                if(counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            }, function (errorResponse) {
                                console.error(errorResponse);
                                if(counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            });
                        });

                        return productsPromise;
                    },

                    this.getProductDescription = function(osTypeName, packageName) {
                        var defer = {};
                        var promise = new Promise(function(resolve, reject) {
                            defer.resolve = function(data) {
                                resolve(data);
                            }

                            defer.reject = function(rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getProductDescriptinEndPoint = getRestEndPointPath("getProductDescription");

                        $http.get(getProductDescriptinEndPoint,
                            {params: {packageName: packageName}, headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}}).then(function(response) {
                            console.log('description recieved from server is ', response.data);
                            defer.resolve(response.data);
                        });

                        return promise;
                    },
                    this.getChangeLog = function(versionCode, packageName) {
                    var defer = {};
                    var promise = new Promise(function(resolve, reject) {
                        defer.resolve = function(data) {
                            resolve(data);
                        }

                        defer.reject = function(rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    var getChangeLogVo = getRestEndPointPath("getChangeLog");

                    $http.get(getChangeLogVo,
                        {params: {packageName: packageName , versionCode :versionCode}, headers: {OSTYPE: encodeURIComponent(osTypeName.toUpperCase())}}).then(function(response) {
                        console.log('changeLog is ', response.data);
                        defer.resolve(response.data);
                    });

                        return promise;
                    }
            }]);

            listPopulatorModule.service('osListService', ['$http', function ($http) {
                var internalOSTypeOSListMap = {};
                var osTypeId2PromiseMap = {};

                var osListForOSTypeWebServicePath = getRestEndPointPath("getOSlistForOSType");

                this.getOSList = function (osType) {
                    var osTypeId = osType.osTypeID;
                    var osList = internalOSTypeOSListMap[osTypeId];

                    if (osList == null || _.isEmpty(osList)) {
                        if (osTypeId2PromiseMap[osTypeId]) {
                            return osTypeId2PromiseMap[osTypeId].promise;
                        }

                        var defer = {};
                        var promise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            }

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });
                        defer.promise = promise;
                        osTypeId2PromiseMap[osTypeId] = defer;

                        $http.get(osListForOSTypeWebServicePath,
                            {params: {osTypeVO: JSON.stringify(osType)}}).then(function (response) {
                            delete osTypeId2PromiseMap[osTypeId];

                            console.log("data recieved from server for osList is ", response.data);

                            internalOSTypeOSListMap[osTypeId] = response.data;
                            defer.resolve(response.data);
                        });

                        return promise;
                    } else {
                        var promise = new Promise(function (resolve, reject) {
                            resolve(osList);
                        });
                        return promise;
                    }
                }
            }]);

            listPopulatorModule.service('osTypeListService', ['$http',
                function ($http) {
                    var osTypes = null;
                    var promiseIsCompleted = false;
                    var prevPromise = null;

                    this.getOSTypeList = function () {
                        if (osTypes == null) {
                            if (prevPromise && !promiseIsCompleted)
                                return prevPromise;

                            var defer = {};
                            var promise = new Promise(function (resolve, reject) {
                                defer.resolve = function (data) {
                                    resolve(data);
                                }

                                defer.reject = function (rejectCause) {
                                    reject(rejectCause);
                                }
                            });

                            $http.get(osTypeListWebServicePath).then(function (response) {
                                promiseIsCompleted = true;
                                defer.resolve(response.data);
                            });

                            prevPromise = promise;
                            return promise;
                        } else {
                            var promise = new Promise(function (resolve, reject) {
                                resolve(osTypes);
                            });
                            return promise;
                        }
                    }

                }]);

            listPopulatorModule
                .controller('osTypeController', ['$scope', '$rootScope', 'osTypeProvider', 'osTypeListService', function ($scope, $rootScope, osTypeProvider, osTypeListService) {
                    console.log('-------> osTypeProvider is ', osTypeProvider);

                    $scope.selectOSType = function (osType) {
                        osTypeProvider.setOSType(osType);
                        $scope.$emit('osTypeChangeEvent', osType);
                    };

                    $rootScope.removeOSType = function() {
                        osTypeProvider.setOSType(null);
                        $scope.$emit('osTypeChangeEvent', null);
                    }

                    osTypeListService.getOSTypeList().then(function (listData) {
                        $scope.$apply(function () {
                            $scope.osTypeListData = listData;
                        });
                    });
                }]);

            listPopulatorModule
                .controller('navinDownloadController', function ($scope, osTypeListService, osListService) {

                    $scope.loadOSList = function () {
                        var navinSelectedPackage = $scope.navinSelectedPackage;
                        if (navinSelectedPackage == null)
                            $scope.showSelectOSTypeAlert = true;
                        else {
                            $scope.showSelectOSTypeAlert = false;

                            osListService.getOSList(navinSelectedPackage).then(function (osData) {
                                $scope.$apply(function () {
                                    $scope.navinOsList = osData;
                                });
                            });
                        }
                    };

                    $scope.loadHandlerApps = function() {
                        var navinSelectedOS = $scope.navinSelectedOS;
                        if(navinSelectedOS != null)
                            $scope.handlerApps = navinSelectedOS.handlerAppVOs;
                        else
                            $scope.handlerApps = null;
                    }

                    $scope.handleNavinDownload = function () {
                        $scope.showSelectOSTypeAlert = false;
                        $scope.showSelectOSAlert = false;
                        $scope.osHandlerAppAlert = false;

                        var navinSelectedPackage = $scope.navinSelectedPackage;
                        var navinSelectedOS = $scope.navinSelectedOS;
                        var selectedHandlerApp = $scope.selectedHandlerApp;

                        if (navinSelectedPackage == null) {
                            $scope.showSelectOSTypeAlert = true;
                        } else if (navinSelectedOS == null) {
                            $scope.showSelectOSAlert = true;
                        } else if(selectedHandlerApp == null) {
                                $scope.osHandlerAppAlert = true;
                            } else {
                                $scope.osHandlerAppAlert = false;

                            downloadURI(selectedHandlerApp.fileHandlerAppKey, selectedHandlerApp.fileName);
                        }
                    };

                    osTypeListService.getOSTypeList().then(function (listData) {
                        $scope.$apply(function () {
                            $scope.navinOstypeList = listData;
                        });
                    });
                });
        }
    }

})();
