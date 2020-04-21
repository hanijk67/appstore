/**
 * Created by A.Moshiri on 12/4/2017.
 */

var ANNOUCEMENTTYPE = Object.freeze({
    VOID: 0,
    PRODUCTLISTTYPE: 1
});

function downloadURI(uri, name) {
    var link = document.createElement("a");
    link.download = name;
    link.href = uri;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    delete link;
}

var loginLocation = '';
var packageListModule = (function () {
    var innerAppContext = "";

    var getRestEndPointPath = function (restEndPointName) {
        var siteLocation = window.location;
        return siteLocation.protocol + "//" + siteLocation.hostname + (siteLocation.port != null ? ":" + siteLocation.port : "") +
            (siteLocation.pathname != null ? siteLocation.pathname : "") + "restAPI/spring/service/" + restEndPointName;


        // return "http://172.16.3.5:8080/appStore/" + "restAPI/spring/service/" + restEndPointName;
    }

    var loadAppPackages = function (from, to, selectedOSType, osTypeListService, appService, callBackListener) {

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
        }, function (callback) {
            appService.getNumberOfProducts(osTypeList).then(function (osTypeName2NumberMap) {
                console.log('recieved osTypeName2NumberMap is ', JSON.stringify(osTypeName2NumberMap));
                callback(null, osTypeName2NumberMap);
            });
        },
            function (osTypeName2NumberMap, callback) {
                var categoryList = {};
                categoryList.allApps = [];
                categoryList.popApps = [];

                appService.getPopularProducts(osTypeList, from, to).then(function (osTypeName2ProductMap) {
                    console.log('recieved popapp list is ', osTypeName2ProductMap);

                    var _size = _.size(osTypeName2ProductMap);
                    var counter = 0;
                    var defer = {};
                    var promise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        }

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    if (_size == 0) {
                        callBackListener.call(null, categoryList);
                        return;
                    }

                    async.series([function (callback) {
                        _.each(osTypeName2ProductMap, function (osTypeProductData, osTypeName) {
                            var category = {};
                            var innerCounter = 1;
                            category.popcategoryTitle = osTypeName;
                            category.popcount = osTypeName2NumberMap[osTypeName];
                            category.popappList = [];

                            categoryList.popApps.push(category);

                            _.each(osTypeProductData, function (productData, index) {
                                var packageName = productData.packageName;

                                var downloadLink = productData.downLoadLink;
                                if (downloadLink) {
                                    appService.getProductDescription(osTypeName.toUpperCase(), packageName).then(function (descriptionData) {
                                        console.log('description data recieved is ', descriptionData);
                                        productData.description = descriptionData.description;

                                        category.popappList.push(productData);

                                        if (innerCounter == osTypeProductData.length) {
                                            counter++;
                                            if (counter == _size)
                                                defer.resolve();
                                        } else
                                            innerCounter++;
                                    });
                                }
                            });
                        });

                        //promise.then(callback);
                    }, function (callback) {
                        //callBackListener.call(null, categoryList);
                    }]);
                }).then(function () {
                    appService.getAllProducts(osTypeList, from, to).then(function (osTypeName2ProductMap) {
                        console.log('recieved app list is ', osTypeName2ProductMap);

                        //var categoryList = [];
                        var _size = _.size(osTypeName2ProductMap);
                        var counter = 0;
                        var defer = {};
                        var promise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            }

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        if (_size == 0) {
                            callBackListener.call(null, categoryList);
                            return;
                        }

                        async.series([function (callback) {
                            _.each(osTypeName2ProductMap, function (osTypeProductData, osTypeName) {
                                var category = {};
                                var innerCounter = 1;
                                category.categoryTitle = osTypeName;
                                category.count = osTypeName2NumberMap[osTypeName];
                                category.appList = [];

                                categoryList.allApps.push(category);

                                _.each(osTypeProductData, function (productData, index) {
                                    var packageName = productData.packageName;

                                    var downloadLink = productData.downLoadLink;
                                    if (downloadLink) {
                                        appService.getProductDescription(osTypeName.toUpperCase(), packageName).then(function (descriptionData) {
                                            console.log('description data recieved is ', descriptionData);
                                            productData.description = descriptionData.description;

                                            category.appList.push(productData);

                                            if (innerCounter == osTypeProductData.length) {
                                                counter++;
                                                if (counter == _size)
                                                    defer.resolve();
                                            } else
                                                innerCounter++;
                                        });
                                    }
                                });
                            });

                            promise.then(callback);
                        }, function (callback) {
                            callBackListener.call(null, categoryList);
                        }]);
                    });
                });
            }
        ]);
    }

    return {
        init: function (appContext) {

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

            listPopulatorModule.service('rateService', function ($http, $rootScope) {
                this.getTopApp = function (from, size) {
                    var getTopRatedAppsEndPoint = getRestEndPointPath('getTopApp');

                    var defer = {};
                    var promise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        }

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    $http.get(getTopRatedAppsEndPoint).then(function (response) {
                        promiseIsCompleted = true;
                        defer.resolve(response.data);
                    });

                    return promise;
                }
            });

            listPopulatorModule.service('announcementService', ['$http', function ($http) {
                var listAnnouncementServiceEndPoint = getRestEndPointPath("listAnnouncement");
                var callAnnouncementServiceEndpoint = getRestEndPointPath("callAnouncement");

                this.callAnouncement = function (announcementId, paramsJsonString) {
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

                    $http.get(callAnnouncementServiceEndpoint,
                        {
                            params: {
                                announcementId: announcementId,
                                paramsJsonString: paramsJsonString
                            }
                        }).then(function (response) {
                        console.log("data recieved from server for listAnnouncementService is ", response.data);
                        defer.resolve(response.data);
                    });

                    return promise;
                }

                this.listAnnouncement = function (osTypeName) {
                    var lastActiveAnnouncements = 10;

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

                    if (osTypeName && !(osTypeName.trim() == "")) {
                        $http.get(listAnnouncementServiceEndPoint,
                            {
                                headers: {OSTYPE: osTypeName.toUpperCase()},
                                params: {lastActiveAnnouncements: lastActiveAnnouncements}
                            }).then(function (response) {
                            console.log("data recieved from server for listAnnouncementService is ", response.data);
                            defer.resolve(response.data);
                        });
                    } else {
                        $http.get(listAnnouncementServiceEndPoint,
                            {params: {lastActiveAnnouncements: lastActiveAnnouncements}}).then(function (response) {
                            console.log("data recieved from server for listAnnouncementService is ", response.data);
                            defer.resolve(response.data);
                        });
                    }

                    return promise;
                }

                this.listAllOsTypesAnnouncement = function () {
                    var lastActiveAnnouncements = 10;

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


                    $http.get(listAllOsTypesAnnouncementServiceEndPoint,
                        {
                            params: {lastActiveAnnouncements: lastActiveAnnouncements}
                        }).then(function (response) {
                        console.log("data recieved from server for listAnnouncementService is ", response.data);
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

            listPopulatorModule.service('categoryService', function ($scope, $rootScope) {
                var getCategoryServiceEndPoint = getRestEndPointPath("getCategories");

                this.getCategories = function (from, count) {
                    var defer = {};
                    var promise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        }

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    $http.get(getCategoryServiceEndPoint,
                        {params: {from: from, count: count}}).then(function (response) {
                        defer.resolve(response.data);
                    });

                    return promise;
                }

                this.getCountCategories = function (categoryName) {
                    var getCategoriesCountEndPoint = getRestEndPointPath("getCountCategories");
                    var defer = {};
                    var promise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        }

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    $http.get(getCategoriesCountEndPoint,
                        {params: {from: from, count: count}}).then(function (response) {
                        defer.resolve(response.data);
                    });

                    return promise;
                }

                this.getAppByCategory = function (catId, from, count) {
                    var getAppByCateryEndPoint = getRestEndPointPath("getAppByCategory");

                    var defer = {};
                    var promise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        }

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    $http.get(getAppByCateryEndPoint,
                        {params: {from: from, count: count}}).then(function (response) {
                        defer.resolve(response.data);
                    });

                    return promise;
                }
            });

            listPopulatorModule.service('appService', ['$http', 'osTypeProvider', 'osTypeListService', function ($http, osTypeProvider, osTypeListService) {
                this.getNumberOfProducts = function (osTypeList) {
                    var numberOfProductsMap = {};

                    var defer = {};
                    var numberOfProductsPromise = new Promise(function (resolve, reject) {
                        defer.resolve = function (data) {
                            resolve(data);
                        };

                        defer.reject = function (rejectCause) {
                            reject(rejectCause);
                        }
                    });

                    var getNumberOfProductsWebServiceEndPoint = getRestEndPointPath("getNumberOfProducts");
                    var counter = 1;

                    osTypeList.forEach(function (osTypeName, index) {
                        $http.get(getNumberOfProductsWebServiceEndPoint,
                            {headers: {OSTYPE: osTypeName.toUpperCase()}}).then(function (response) {
                            numberOfProductsMap[osTypeName] = response.data;
                            if (counter == osTypeList.length) {
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

                    this.getAppByAppTitleInAllOsTypes = function (appName) {
                        var defer = {};
                        var allApps = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            };

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }

                            return defer.promise;
                        });

                        var getAppByAppTitleInAllOsTypes = getRestEndPointPath("getAppByAppTitleInAllOsTypes");
                        if(!appName || appName=="null"){
                            appName="";
                        }
                        $http.get(getAppByAppTitleInAllOsTypes,
                            {
                                params: {appTitle: appName, from: 0, count: 10}
                            }).then(function (response) {
                            defer.resolve(response.data);
                        });

                        return allApps;

                    },

                    this.getAllProducts = function (osTypeList, from, count) {

                        var productsMap = {};

                        var defer = {};
                        var productsPromise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            };

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getAllProductsWebServiceEndPoint = getRestEndPointPath("getAllProducts");
                        getAllProductsWebServiceEndPoint += "/" + from + "/" + count;
                        var counter = 1;

                        osTypeList.forEach(function (osTypeName, index) {
                            $http.get(getAllProductsWebServiceEndPoint,
                                {headers: {OSTYPE: osTypeName.toUpperCase()}}).then(function (response) {
                                productsMap[osTypeName] = response.data;
                                if (counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            }, function (errorResponse) {
                                console.error(errorResponse);
                                if (counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            });
                        });

                        return productsPromise;
                    },

                    this.getPopularProducts = function (osTypeList, from, count) {

                        var productsMap = {};

                        var defer = {};
                        var productsPromise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            };

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getAllProductsWebServiceEndPoint = getRestEndPointPath("getTopApp");
                        getAllProductsWebServiceEndPoint += "?from=" + from + "&size=" + count;
                        var counter = 1;

                        osTypeList.forEach(function (osTypeName, index) {
                            $http.get(getAllProductsWebServiceEndPoint,
                                {headers: {OSTYPE: osTypeName.toUpperCase()}}).then(function (response) {
                                productsMap[osTypeName] = response.data;
                                if (counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            }, function (errorResponse) {
                                console.error(errorResponse);
                                if (counter == osTypeList.length) {
                                    defer.resolve(productsMap);
                                } else {
                                    counter++;
                                }
                            });
                        });

                        return productsPromise;
                    },

                    this.getProductDescription = function (osTypeName, packageName) {
                        var defer = {};
                        var promise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            }

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getProductDescriptinEndPoint = getRestEndPointPath("getProductDescription");

                        $http.get(getProductDescriptinEndPoint,
                            {
                                params: {packageName: packageName},
                                headers: {OSTYPE: osTypeName.toUpperCase()}
                            }).then(function (response) {
                            console.log('description recieved from server is ', response.data);
                            defer.resolve(response.data);
                        });

                        return promise;
                    },

                    this.getChangeLog = function (versionCode, packageName, osTypeName) {
                        var defer = {};
                        var promise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            }

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getChangeLogVo = getRestEndPointPath("getChangeLog");

                        $http.get(getChangeLogVo,
                            {
                                params: {packageName: packageName, versionCode: versionCode},
                                headers: {OSTYPE: osTypeName.toUpperCase()}
                            }).then(function (response) {
                            console.log('changeLog is ', response.data);
                            defer.resolve(response.data);
                        });

                        return promise;
                    },

                    this.getAppByCategoryName = function (categoryName) {

                        var defer = {};
                        var allApps = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            };

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }

                            return defer.promise;
                        });

                        var getAppByCategoryNameInAllOsTypes = getRestEndPointPath("getAppByCategoryNameInAllOsTypes");

                        $http.get(getAppByCategoryNameInAllOsTypes,
                            {
                                params: {categoryName: categoryName}
                            }).then(function (response) {
                            console.log('all apps that have this Category ' + categoryName + ' recieved from server is ', response.data);
                            defer.resolve(response.data);
                        });

                        return allApps;
                    },

                    this.getPackageImages = function (versionCode, packageName, osTypeName) {
                        var defer = {};
                        var promise = new Promise(function (resolve, reject) {
                            defer.resolve = function (data) {
                                resolve(data);
                            }

                            defer.reject = function (rejectCause) {
                                reject(rejectCause);
                            }
                        });

                        var getImageGalleries = getRestEndPointPath("getImageGalleries");

                        $http.get(getImageGalleries,
                            {
                                params: {packageName: packageName, versionCode: versionCode},
                                headers: {OSTYPE: osTypeName.toUpperCase()}
                            }).then(function (response) {
                            console.log('image galleries ', response.data);
                            defer.resolve(response.data);
                        });

                        return promise;
                    }

            }]);

            listPopulatorModule.controller('headerBarController', ['$scope', '$rootScope', 'appService', function ($scope, $rootScope, appService) {

                $scope.generalAppListButton = function () {
                    $('.appList').click(function () {
                        $('.appListModal').modal({
                            inverted: true
                        }).modal('show');
                    });
                }

                $scope.downloadMarketFunction = function () {
                    $('.downloadOSModal').modal({
                        inverted: true
                    }).modal('show');
                }

                $scope.openAllAppsModal = function (appName) {

                    $rootScope.$applyAsync(function () {
                        $rootScope.appSearchVoList = null;
                    })

                    appService.getAppByAppTitleInAllOsTypes(appName).then(function (appList) {

                        $rootScope.$apply(function () {

                            if (appList && appList.length > 0) {
                                $rootScope.appSearchVoList = appList;
                            }
                            $('.slider').slick('unslick');

                        })


                    }).then(function () {

                        $('.allAppsModal').modal({
                            inverted: true
                        }).modal('show');
                        setTimeout(function () {
                            $('.slider').slick({
                                dots: true,
                                infinite: false,
                                speed: 300,
                                slidesToShow: 4,
                                slidesToScroll: 4,
                                responsive: [
                                    {
                                        breakpoint: 1024,
                                        settings: {
                                            slidesToShow: 3,
                                            slidesToScroll: 3,
                                            infinite: true,
                                            dots: true
                                        }
                                    },
                                    {
                                        breakpoint: 600,
                                        settings: {
                                            slidesToShow: 2,
                                            slidesToScroll: 2
                                        }
                                    },
                                    {
                                        breakpoint: 480,
                                        settings: {
                                            slidesToShow: 1,
                                            slidesToScroll: 1
                                        }
                                    }
                                ]
                            });
                        }, 200);
                    })

                }

                $rootScope.showAppDetail = function (appVO) {
                    $scope.$emit('appDetailShow', appVO);
                }
                // $scope.searchAppByAppTitleInAllOsTypes = function(appName) {
                //
                //   // alert('your app name is '+ appName);
                // }
                var siteLocation = window.location;

                var location =  siteLocation.protocol + "//" + siteLocation.hostname + (siteLocation.port != null ? ":" + siteLocation.port : "")
                    +"/appStore/loginPage" ;

                $scope.location = location;
            }]);

            listPopulatorModule.controller('appListController', ['$scope', '$rootScope', function ($scope, $rootScope) {

                var loadMore = function (from, to) {
                    var dataProvider = $scope.dataProvider;
                    dataProvider.getAppList(from, to).then(function (loadedApps) {
                        var appVOList = [];

                        for (var j = 0; j < loadedApps.length; j++) {
                            loadedApps[j].hasImage = true;

                            if (loadedApps[j].iconThumbNail == null || loadedApps[j].iconThumbNail.trim == "")
                                loadedApps[j].hasImage = false;

                            if (!loadedApps[j].categoryIcon || loadedApps[j].categoryIcon.trim == "")
                                loadedApps[j].categoryIcon = true;

                            appVOList.push(loadedApps[j]);
                        }


                        $rootScope.$applyAsync(function () {
                            $rootScope.appSearchVoList = null;

                            $rootScope.appSearchVoList = appVOList;
                        })
                        $('.slider').slick('unslick');

                        $('.allAppsModal').modal({
                            inverted: true
                        }).modal('show');


                        setTimeout(function () {
                            $('.slider').slick({
                                dots: true,
                                infinite: false,
                                speed: 300,
                                slidesToShow: 4,
                                slidesToScroll: 4,
                                responsive: [
                                    {
                                        breakpoint: 1024,
                                        settings: {
                                            slidesToShow: 3,
                                            slidesToScroll: 3,
                                            infinite: true,
                                            dots: true
                                        }
                                    },
                                    {
                                        breakpoint: 600,
                                        settings: {
                                            slidesToShow: 2,
                                            slidesToScroll: 2
                                        }
                                    },
                                    {
                                        breakpoint: 480,
                                        settings: {
                                            slidesToShow: 1,
                                            slidesToScroll: 1
                                        }
                                    }
                                ]
                            });
                        }, 200);
                    });
                }

                $scope.loadMore = loadMore;

                $rootScope.$on('appListShowEvent', function (event, dataProvider, title) {
                    var from = 0;
                    var to = 10;

                    $scope.appListWindowTitle = title;

                    $scope.dataProvider = dataProvider;
                    loadMore(from, to);
                });

            }]);

            listPopulatorModule.controller('osTypeController', ['$scope', '$rootScope', 'osTypeProvider', 'osTypeListService', function ($scope, $rootScope, osTypeProvider, osTypeListService) {

                $scope.selectOSType = function (osType) {
                    osTypeProvider.setOSType(osType);
                    $scope.$emit('osTypeChangeEvent', osType);
                }

                $rootScope.removeOSType = function () {
                    osTypeProvider.setOSType(null);
                    $scope.$emit('osTypeChangeEvent', null);
                }

                osTypeListService.getOSTypeList().then(function (listData) {
                    $scope.$apply(function () {
                        $scope.osTypeListData = listData;
                    });
                }, function (e) {
                    console.log('error occured getting osTypeList ', e);
                });
            }]);

            listPopulatorModule.controller('announcementFilterController', function ($scope, osTypeListService, appService, osListService) {
                var colorClassCodes = ['red', 'blue', 'black', 'purple', 'orange', 'yellow', 'pink', 'green', 'teal', 'brown', 'olive', 'violet'];

                $scope.loading = 'loading';
                $scope.filter_icon = "filter";

                $scope.removeFilter = function ($event) {
                    if ($scope.filter_icon == "remove") {
                        $('.announcementsOSTypeDropDown').dropdown('clear');
                    }
                }

                osTypeListService.getOSTypeList().then(function (osTypeListData) {
                    $scope.$apply(function () {

                        for (var i = 0; i < osTypeListData.length; i++) {
                            var colorCode = 'empty';
                            if (i < colorClassCodes.length)
                                colorCode = colorClassCodes[i];

                            osTypeListData[i].colorCode = colorCode;
                        }

                        $scope.osTypeListData = osTypeListData;

                        var osTypeNames = [];

                        for (var k = 0; k < osTypeListData.length; k++) {
                            osTypeNames.push(osTypeListData[k].osName.toUpperCase());
                        }

                        $scope.loading = '';

                        $('.announcementsOSTypeDropDown').dropdown({
                            onChange: function (osTypeId, osName) {
                                if (osName != null && !osName.trim() == "") {
                                    var ix = osName.indexOf("\n");
                                    $scope.$emit('announcementChangeEvent', [osName.substring(ix).trim().toUpperCase()]);
                                    $scope.$apply(function () {
                                        $scope.filter_icon = "remove";
                                    });
                                } else {
                                    $scope.filter_icon = "filter";
                                    $('.announcementsOSTypeDropDown').find('span.samimFontFamily').text('انتخاب محیط اجرایی');
                                    $scope.$emit('announcementChangeEvent', osTypeNames);
                                }
                            }
                        });

                        $scope.$emit('announcementChangeEvent', osTypeNames);


                    });

                }, function (e) {
                    console.log('error occured getting osTypeList for announcementFilterController ', e);
                });
            });

            listPopulatorModule.controller('announcementSliderController', function ($scope, $rootScope, announcementService) {
                $scope.announcementsLoading = 'loading';
                $scope.announcementVOList = [];

                $scope.handleAnnouncementClick = function (announcement) {

                    var anouncementType = announcement.anouncementType;
                    if (anouncementType.type == ANNOUCEMENTTYPE.PRODUCTLISTTYPE) {
                        var announcementId = announcement.id;
                        var announcementProductListDataProvider = {
                            getAppList: function (from, to) {
                                var paramsJsonString = {};
                                paramsJsonString.from = '' + from;
                                paramsJsonString.to = '' + to;

                                var defer = {};
                                var promise = new Promise(function (resolve, reject) {
                                    defer.resolve = function (data) {
                                        resolve(data);
                                    }

                                    defer.reject = function (rejectCause) {
                                        reject(rejectCause);
                                    }
                                });

                                announcementService.callAnouncement(announcementId, paramsJsonString).then(function (announcementListData) {
                                    var appList = JSON.parse(announcementListData.result);

                                    for (var j = 0; j < appList.length; j++) {
                                        appList[j].hasImage = true;

                                        if (appList[j].iconThumbNail == null || appList[j].iconThumbNail.trim == "")
                                            appList[j].hasImage = false;

                                        if (!appList[j].categoryIcon || appList[j].categoryIcon.trim == "")
                                            appList[j].categoryIcon = true;
                                    }

                                    defer.resolve(appList);
                                });

                                return promise;
                            }
                        }

                        var announcementTitle = announcement.anouncementText;
                        // this.announcementProductListDataProvider;
                        $scope.$emit('appListShowEvent', announcementProductListDataProvider, announcementTitle);


                    }
                }

                $rootScope.$on('announcementChangeEvent', function (event, data) {

                    $scope.announcementShowLoadingBar = true;
                    var selectedOSTypes = data;

                    $scope.announcementsLoading = 'loading';

                    var allAnouncements = [];
                    async.waterfall([function (callback) {
                        var responsesRecieved = 0;
                        if (selectedOSTypes && selectedOSTypes.length > 0) {
                        for (var i = 0; i < selectedOSTypes.length; i++) {
                            var selectedOSType = selectedOSTypes[i];

                            announcementService.listAnnouncement(selectedOSType).then(function (loadedAnnouncementVOs) {
                                allAnouncements.push(loadedAnnouncementVOs);
                                responsesRecieved++;
                                if (responsesRecieved == selectedOSTypes.length) {
                                    callback();
                                }
                            });

                        }
                        } else {
                            $scope.announcementShowLoadingBar = false;
                        }
                    }, function (callback) {
                        var announcementVOList = [];

                        for (var j = 0; j < allAnouncements.length; j++) {
                            var loadedAnnouncementVOs = allAnouncements[j];

                            for (var i = 0; i < loadedAnnouncementVOs.length; i++) {
                                var announcementVO = loadedAnnouncementVOs[i];
                                announcementVOList.push(announcementVO);
                            }
                        }

                        $scope.$apply(function () {
                            $scope.announcementVOList = announcementVOList;

                            if (announcementVOList && announcementVOList != null) {
                                $scope.announcementShowLoadingBar = false;
                            }
                            else {
                                setTimeout(function () {
                                    $scope.announcementShowLoadingBar = false;
                                },6000);
                            }
                            $scope.announcementsLoading = '';
                            try {
                                $('.ui.imageSlider').slick('unslick');
                            } catch (e) {
                            }
                            setTimeout(function () {
                                $('.ui.imageSlider').slick({
                                    infinite: true,
                                    slidesToShow: 1,
                                    dots: true,
                                    centerMode: true,
                                    centerPadding: '60px',
                                    arrows: true,
                                    adaptiveHeight: false
                                });
                            }, 200);
                        });
                    }]);

                });
            });

            listPopulatorModule.controller("appContextController", ['$scope', '$rootScope', function ($scope, $rootScope) {
                var siteLocation = window.location;
                $scope.loginLocation =
                    siteLocation.protocol + "//" + siteLocation.hostname + (siteLocation.port != null ? ":" + siteLocation.port : "") +
                    (siteLocation.pathname != null ? siteLocation.pathname : "") + "loginPage";

                //     "http://172.16.3.5:8080/appStore/loginPage";
                //
                //
                // loginLocation = $scope.loginLocation;
                // console.log('set login location to ===>'+ $scope.loginLocation );
                //
                // console.log('set siteLocation.protocol to ===>'+siteLocation.protocol );
                // console.log('set siteLocation.hostname to ===>'+  siteLocation.hostname);
                // console.log('set siteLocation.port  to ===>'+ siteLocation.port );
                // console.log('set siteLocation.pathnameto ===>'+ siteLocation.pathname);

            }]);

            listPopulatorModule.controller('appDetailController', function ($scope, $rootScope, appService) {

                $rootScope.downloadApplication = function (appDetail) {
                    downloadURI(appDetail.downLoadLink, appDetail.title);
                }

                $rootScope.$on('appDetailShow', function (event, data) {

                    var appDetail = data;
                    $scope.appDetail = appDetail;

                    var versionCode = data.versionCode;
                    var packageName = data.packageName;
                    var osTypeName = data.osTypeName;

                    appService.getChangeLog(versionCode, packageName, osTypeName).then(function (changeLogData) {
                        appDetail.changeLog = changeLogData.appPackageChangeLog;

                        $('.bindhtml').html(appDetail.changeLog);
                        $('.slider').slick('unslick');

                        $('.ui.appDetailModal')
                            .modal({
                                inverted: true
                            }).modal('show');

                        setTimeout(function () {
                            $('.slider').slick({
                                dots: true,
                                infinite: false,
                                speed: 300,
                                slidesToShow: 4,
                                slidesToScroll: 4,
                                responsive: [
                                    {
                                        breakpoint: 1024,
                                        settings: {
                                            slidesToShow: 3,
                                            slidesToScroll: 3,
                                            infinite: true,
                                            dots: true
                                        }
                                    },
                                    {
                                        breakpoint: 600,
                                        settings: {
                                            slidesToShow: 2,
                                            slidesToScroll: 2
                                        }
                                    },
                                    {
                                        breakpoint: 480,
                                        settings: {
                                            slidesToShow: 1,
                                            slidesToScroll: 1
                                        }
                                    }
                                ]
                            });
                        }, 200);

                        new Medium({
                            element: $('.ui.changeLog')[0],
                            mode: Medium.richMode
                        });

                    });


                    appService.getPackageImages(versionCode, packageName, osTypeName).then(function (imageGalleryVOList) {
                        var imageList = [];

                        for (var j = 0; j < imageGalleryVOList.length; j++) {
                            var imageGalleryVO = imageGalleryVOList[j];
                            imageList.push(imageGalleryVO.imageUrl);
                        }
                        $('.slider').slick('unslick');

                        $scope.appImageList = null;
                        $scope.$applyAsync(function () {
                            if (imageList && imageList != null) {
                                $scope.appImageList = imageList;
                            }
                        })

                        setTimeout(function () {
                            $('.slider').slick({
                                dots: true,
                                infinite: false,
                                speed: 300,
                                slidesToShow: 4,
                                slidesToScroll: 4,
                                responsive: [
                                    {
                                        breakpoint: 1024,
                                        settings: {
                                            slidesToShow: 3,
                                            slidesToScroll: 3,
                                            infinite: true,
                                            dots: true
                                        }
                                    },
                                    {
                                        breakpoint: 600,
                                        settings: {
                                            slidesToShow: 2,
                                            slidesToScroll: 2
                                        }
                                    },
                                    {
                                        breakpoint: 480,
                                        settings: {
                                            slidesToShow: 1,
                                            slidesToScroll: 1
                                        }
                                    }
                                ]
                            });
                        }, 200);

                    });

                    //appImageList

                });
            });

            listPopulatorModule.controller('firstOSTypeFilterController', function ($scope, osTypeListService, appService, osListService) {
                var colorClassCodes = ['red', 'blue', 'black', 'purple', 'orange', 'yellow', 'pink', 'green', 'teal', 'brown', 'olive', 'violet'];

                $scope.loading = 'loading';

                $scope.filter_icon = "filter";

                $scope.removeFilter = function ($event) {
                    if ($scope.filter_icon == "remove") {
                        $('.allAppOSTypeDropDown').dropdown('clear');
                    }
                }

                osTypeListService.getOSTypeList().then(function (listData) {
                    $scope.$apply(function () {

                        for (var i = 0; i < listData.length; i++) {
                            var colorCode = 'empty';
                            if (i < colorClassCodes.length)
                                colorCode = colorClassCodes[i];

                            listData[i].colorCode = colorCode;
                        }

                        $scope.osTypeListData = listData;

                        $scope.loading = '';

                        $('.allAppOSTypeDropDown').dropdown({
                            onChange: function (osTypeId, osName) {
                                if (osName != null && !osName.trim() == "") {
                                    var ix = osName.indexOf("\n");
                                    $scope.$emit('firstOSTypeChangeEvent', osName.substring(ix).trim().toUpperCase());
                                    $scope.$apply(function () {
                                        $scope.filter_icon = "remove";
                                    });
                                } else {
                                    $scope.filter_icon = "filter";

                                    $('.allAppOSTypeDropDown').find('span.samimFontFamily').text('انتخاب محیط اجرایی');
                                    $scope.$emit('firstOSTypeChangeEvent', null);
                                }
                            }
                        });

                        $scope.$emit('firstOSTypeChangeEvent', null);

                    });

                }, function (e) {
                    console.log('error occured getting osTypeList for allAppsController ', e);
                });
            });


            listPopulatorModule.controller('newAppsOSTypeFilterController', function ($scope, osTypeListService, appService, osListService) {
                var colorClassCodes = ['red', 'blue', 'black', 'purple', 'orange', 'yellow', 'pink', 'green', 'teal', 'brown', 'olive', 'violet'];

                $scope.loading = 'loading';

                $scope.newApps_filter_icon = "filter";

                $scope.removeFilter = function ($event) {
                    if ($scope.newApps_filter_icon == "remove") {
                        $('.newAppOSTypeDropDown').dropdown('clear');
                    }
                }

                osTypeListService.getOSTypeList().then(function (listData) {
                    $scope.$apply(function () {

                        for (var i = 0; i < listData.length; i++) {
                            var colorCode = 'empty';
                            if (i < colorClassCodes.length)
                                colorCode = colorClassCodes[i];

                            listData[i].colorCode = colorCode;
                        }

                        $scope.osTypeListData = listData;

                        $scope.loading = '';

                        $('.newAppOSTypeDropDown').dropdown({
                            onChange: function (osTypeId, osName) {

                                var slickCount = $('.slider').slick('getSlick').slideCount;
                                for(var i =0 ; i< slickCount ; i++){
                                    $('.slider').slick('slickRemove', 0);
                                }
                                if (osName != null && !osName.trim() == "") {
                                    var ix = osName.indexOf("\n");
                                    $scope.$emit('newAppsOSTypeChangeEvent', osName.substring(ix).trim().toUpperCase());
                                    $scope.$apply(function () {
                                        $scope.newApps_filter_icon = "remove";
                                    });
                                } else {
                                    $scope.newApps_filter_icon = "filter";

                                    $('.newAppOSTypeDropDown').find('span.samimFontFamily').text('انتخاب محیط اجرایی');
                                    $scope.$emit('newAppsOSTypeChangeEvent', null);
                                }
                            }
                        });

                        $scope.$emit('newAppsOSTypeChangeEvent', null);

                    });

                }, function (e) {
                    console.log('error occured getting osTypeList for allAppsController ', e);
                });
            });

            listPopulatorModule.controller('topRateAppsOSTypeFilterController', function ($scope, osTypeListService, appService, osListService) {
                var colorClassCodes = ['red', 'blue', 'black', 'purple', 'orange', 'yellow', 'pink', 'green', 'teal', 'brown', 'olive', 'violet'];

                $scope.loading = 'loading';

                $scope.topRate_filter_icon = "filter";

                $scope.removeFilter = function ($event) {
                    if ($scope.topRate_filter_icon == "remove") {
                        $('.popAppOSTypeDropDown').dropdown('clear');
                    }
                }

                osTypeListService.getOSTypeList().then(function (listData) {
                    $scope.$apply(function () {

                        for (var i = 0; i < listData.length; i++) {
                            var colorCode = 'empty';
                            if (i < colorClassCodes.length)
                                colorCode = colorClassCodes[i];

                            listData[i].colorCode = colorCode;
                        }

                        $scope.osTypeListData = listData;

                        $scope.loading = '';

                        $('.popAppOSTypeDropDown').dropdown({
                            onChange: function (osTypeId, osName) {
                                var slickCount = $('.slider').slick('getSlick').slideCount;
                                for(var i =0 ; i< slickCount ; i++){
                                    $('.slider').slick('slickRemove', 0);
                                }

                                if (osName != null && !osName.trim() == "") {
                                    var ix = osName.indexOf("\n");
                                    $scope.$emit('topRateAppsOSTypeChangeEvent', osName.substring(ix).trim().toUpperCase());
                                    $scope.$apply(function () {
                                        $scope.topRate_filter_icon = "remove";
                                    });
                                } else {
                                    $scope.topRate_filter_icon = "filter";

                                    $('.popAppOSTypeDropDown').find('span.samimFontFamily').text('انتخاب محیط اجرایی');
                                    $scope.$emit('topRateAppsOSTypeChangeEvent', null);
                                }
                            }
                        });

                        $scope.$emit('topRateAppsOSTypeChangeEvent', null);

                    });

                }, function (e) {
                    console.log('error occured getting osTypeList for allAppsController ', e);
                });
            });


            listPopulatorModule.controller('allAppsCardsController', function ($scope, $rootScope, osTypeListService, appService) {
                $scope.cardsLoading = 'loading';
                var prevAppListCount = 0;

                $rootScope.showAppDetail = function (appVO) {
                    $scope.$emit('appDetailShow', appVO);
                }

                $rootScope.$on('firstOSTypeChangeEvent', function (event, data) {
                    var selectedOSType = data;

                    //try {
                    //    $('.ui.cards.allApps').slick('unslick');
                    //} catch (e) {}

                    $scope.cardsLoading = 'loading';

                    loadAppPackages(0, 3, selectedOSType, osTypeListService, appService, function (loadedApps) {

                        $scope.$apply(function () {
                            var appVOList = [];
                            var popappVOList = [];


                            for (var i = 0; i < loadedApps.allApps.length; i++) {
                                var app = loadedApps.allApps[i];
                                var tempAppList = app.appList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    appVOList.push(tempAppList[j]);
                                }
                            }


                            // popapp
                            for (var i = 0; i < loadedApps.popApps.length; i++) {
                                var app = loadedApps.popApps[i];
                                var tempAppList = app.popappList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    popappVOList.push(tempAppList[j]);
                                }
                            }


                            $scope.appVOList = appVOList;
                            $scope.popappVOList = popappVOList;


                            $scope.cardsLoading = '';
                            try {
                                $('.slider').slick('unslick');

                            } catch (e) {
                            }

                            setTimeout(function () {
                                $('.slider').slick({
                                    dots: true,
                                    infinite: false,
                                    speed: 300,
                                    slidesToShow: 4,
                                    slidesToScroll: 4,
                                    responsive: [
                                        {
                                            breakpoint: 1024,
                                            settings: {
                                                slidesToShow: 3,
                                                slidesToScroll: 3,
                                                infinite: true,
                                                dots: true
                                            }
                                        },
                                        {
                                            breakpoint: 600,
                                            settings: {
                                                slidesToShow: 2,
                                                slidesToScroll: 2
                                            }
                                        },
                                        {
                                            breakpoint: 480,
                                            settings: {
                                                slidesToShow: 1,
                                                slidesToScroll: 1
                                            }
                                        }
                                    ]
                                });
                            }, 200);
                        });

                    });

                });

                $rootScope.$on('newAppsOSTypeChangeEvent', function (event, data) {
                    var selectedOSType = data;


                    $scope.cardsLoading = 'loading';
                    $scope.newAppsShowLoadingBar = true;

                    loadAppPackages(0, 3, selectedOSType, osTypeListService, appService, function (loadedApps) {

                        if (loadedApps && loadedApps.allApps && loadedApps.allApps.length > 0) {
                        $scope.$apply(function () {
                            var appVOList = [];
                            var popappVOList = [];


                            for (var i = 0; i < loadedApps.allApps.length; i++) {
                                var app = loadedApps.allApps[i];
                                var tempAppList = app.appList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    appVOList.push(tempAppList[j]);
                                }
                            }


                            // popapp
                            for (var i = 0; i < loadedApps.popApps.length; i++) {
                                var app = loadedApps.popApps[i];
                                var tempAppList = app.popappList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    popappVOList.push(tempAppList[j]);
                                }
                            }

                            $scope.appVOList = null;
                            $scope.$applyAsync(function () {
                                if (appVOList && appVOList != null) {
                                    $scope.appVOList = appVOList;
                                $scope.newAppsShowLoadingBar = false;
                                }
                                else {
                                    setTimeout(function () {
                                        $scope.newAppsShowLoadingBar = false;
                                    },6000);
                                }
                            })

                            $scope.cardsLoading = '';
                            try {
                                $('.slider').slick('unslick');

                            } catch (e) {
                            }

                            setTimeout(function () {
                                $('.slider').slick({
                                    dots: true,
                                    infinite: false,
                                    speed: 300,
                                    slidesToShow: 4,
                                    slidesToScroll: 4,
                                    responsive: [
                                        {
                                            breakpoint: 1024,
                                            settings: {
                                                slidesToShow: 3,
                                                slidesToScroll: 3,
                                                infinite: true,
                                                dots: true
                                            }
                                        },
                                        {
                                            breakpoint: 600,
                                            settings: {
                                                slidesToShow: 2,
                                                slidesToScroll: 2
                                            }
                                        },
                                        {
                                            breakpoint: 480,
                                            settings: {
                                                slidesToShow: 1,
                                                slidesToScroll: 1
                                            }
                                        }
                                    ]
                                });
                            }, 200);

                        });
                        } else {
                            $scope.newAppsShowLoadingBar = false;
                        }

                    });

                    osTypeListService.getOSTypeList().then(function (listData) {
                        if (!listData || listData.length < 1) {
                            $scope.newAppsShowLoadingBar = false;
                        }
                    });

                });

                $rootScope.$on('topRateAppsOSTypeChangeEvent', function (event, data) {
                    var selectedOSType = data;

                    $scope.cardsLoading = 'loading';
                    $scope.topAppsShowLoadingBar = true;
                    loadAppPackages(0, 3, selectedOSType, osTypeListService, appService, function (loadedApps) {
                        if (loadedApps && loadedApps.allApps && loadedApps.allApps.length > 0) {
                        $scope.$apply(function () {
                            var appVOList = [];
                            var popappVOList = [];


                            for (var i = 0; i < loadedApps.allApps.length; i++) {
                                var app = loadedApps.allApps[i];
                                var tempAppList = app.appList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    appVOList.push(tempAppList[j]);
                                }
                            }
                            // popapp
                            for (var i = 0; i < loadedApps.popApps.length; i++) {
                                var app = loadedApps.popApps[i];
                                var tempAppList = app.popappList;

                                for (var j = 0; j < tempAppList.length; j++) {
                                    tempAppList[j].hasImage = true;

                                    if (tempAppList[j].iconThumbNail == null || tempAppList[j].iconThumbNail.trim == "")
                                        tempAppList[j].hasImage = false;

                                    if (!tempAppList[j].categoryIcon || tempAppList[j].categoryIcon.trim == "")
                                        tempAppList[j].categoryIcon = true;

                                    popappVOList.push(tempAppList[j]);
                                }
                            }

                            $scope.$applyAsync(function () {
                                if (popappVOList && popappVOList != null) {
                                    $scope.popappVOList = popappVOList;
                                $scope.topAppsShowLoadingBar = false;
                                }
                                else {
                                    setTimeout(function () {
                                        $scope.topAppsShowLoadingBar = false;
                                    },6000);
                                }
                            })

                            $scope.cardsLoading = '';
                            try {
                                $('.slider').slick('unslick');

                            } catch (e) {
                            }
                            if (popappVOList && popappVOList != null) {

                            setTimeout(function () {
                                $('.slider').slick({
                                    dots: true,
                                    infinite: false,
                                    speed: 300,
                                    slidesToShow: 4,
                                    slidesToScroll: 4,
                                    responsive: [
                                        {
                                            breakpoint: 1024,
                                            settings: {
                                                slidesToShow: 3,
                                                slidesToScroll: 3,
                                                infinite: true,
                                                dots: true
                                            }
                                        },
                                        {
                                            breakpoint: 600,
                                            settings: {
                                                slidesToShow: 2,
                                                slidesToScroll: 2
                                            }
                                        },
                                        {
                                            breakpoint: 480,
                                            settings: {
                                                slidesToShow: 1,
                                                slidesToScroll: 1
                                            }
                                        }
                                    ]
                                });
                            }, 200);
                            }
                        });
                        } else {
                            $scope.topAppsShowLoadingBar = false;
                        }

                    });

                    osTypeListService.getOSTypeList().then(function (listData) {
                        if (!listData || listData.length < 1) {
                            $scope.$apply(function () {
                                $scope.topAppsShowLoadingBar = false;
                            });

                        }
                    });
                });


                $scope.openRelatedCategoryAppsModal = function (categoryName) {
                    $rootScope.$applyAsync(function () {
                        $rootScope.appSearchVoList = null;
                    })
                    appService.getAppByCategoryName(categoryName).then(function (appList) {

                        $rootScope.$apply(function () {

                            if (appList && appList.length > 0) {

                                $rootScope.appSearchVoList = appList;
                            }
                            $('.slider').slick('unslick');

                        })


                    }).then(function () {

                        $('.allAppsModal').modal({
                            inverted: true
                        }).modal('show');

                        setTimeout(function () {
                            $('.slider').slick({
                                dots: true,
                                infinite: false,
                                speed: 300,
                                slidesToShow: 4,
                                slidesToScroll: 4,
                                responsive: [
                                    {
                                        breakpoint: 1024,
                                        settings: {
                                            slidesToShow: 3,
                                            slidesToScroll: 3,
                                            infinite: true,
                                            dots: true
                                        }
                                    },
                                    {
                                        breakpoint: 600,
                                        settings: {
                                            slidesToShow: 2,
                                            slidesToScroll: 2
                                        }
                                    },
                                    {
                                        breakpoint: 480,
                                        settings: {
                                            slidesToShow: 1,
                                            slidesToScroll: 1
                                        }
                                    }
                                ]
                            });

                        }, 200);

                    })
                }


            });

            listPopulatorModule.controller('navinDownloadController', function ($scope, osTypeListService, osListService) {

                $scope.osTypeloading = 'loading';

                $scope.showOsList = false;
                $scope.showHandlerAppList = false;
                $scope.showGetHandlerAppBtn = false;
                $scope.selectedHandlerApp = null;
                var selectedHandlerApp = null;

                $scope.getHandlerApp = function () {
                    var selectedValue = $('.handlerAppDropDown').dropdown('get value');
                    downloadURI(selectedHandlerApp.value, selectedHandlerApp.name);
                }

                osTypeListService.getOSTypeList().then(function (listData) {
                    $scope.$apply(function () {
                        $scope.osTypeloading = '';

                        var values = [];
                        for (var i = 0; i < listData.length; i++) {
                            values.push(
                                {
                                    name: listData[i].osName,
                                    value: listData[i].osTypeID
                                }
                            );
                        }

                        $('.ui.dropdown.osTypeDropDown').dropdown({
                            onChange: function (osTypeId, osTypeName) {
                                var osTypeVO = {};
                                osTypeVO.osTypeID = osTypeId;

                                $scope.showOsList = true;
                                $scope.showHandlerAppList = false;
                                $scope.osloading = 'loading';
                                $scope.showGetHandlerAppBtn = false;

                                $('.ui.dropdown.osDropDown').dropdown({values: []});

                                var osDataMap = {};

                                osListService.getOSList(osTypeVO).then(function (osData) {
                                    $scope.$apply(function () {
                                        $scope.osloading = '';
                                    });

                                    if (osData && osData.length > 0) {
                                        var osValues = [];
                                        for (var i = 0; i < osData.length; i++) {
                                            osValues.push(
                                                {
                                                    name: osData[i].osName,
                                                    value: osData[i].osId
                                                });
                                            osDataMap[osData[i].osId] = osData[i];
                                        }

                                        $('.ui.dropdown.osDropDown').dropdown({
                                            onChange: function (osId, osName) {
                                                $scope.$apply(function () {
                                                    $scope.showGetHandlerAppBtn = false;
                                                });
                                                //if(selectOSData) {
                                                var selectOSData = osDataMap[osId];

                                                var handlerAppVos = selectOSData.handlerAppVOs;

                                                if (handlerAppVos.length > 0) {
                                                    $scope.$apply(function () {
                                                        $scope.showHandlerAppList = true;
                                                    });

                                                    var handlerAppValues = [];
                                                    for (var i = 0; i < handlerAppVos.length; i++) {
                                                        handlerAppValues.push(
                                                            {
                                                                name: handlerAppVos[i].fileName,
                                                                value: handlerAppVos[i].fileHandlerAppKey
                                                            }
                                                        );
                                                    }

                                                    $('.ui.dropdown.handlerAppDropDown').dropdown({
                                                        onChange: function (handlerAppId, handlerAppName) {
                                                            $scope.$apply(function () {
                                                                $scope.showGetHandlerAppBtn = true;
                                                            });

                                                            selectedHandlerApp =
                                                                {
                                                                    name: handlerAppName,
                                                                    value: handlerAppId

                                                                }
                                                        },
                                                        values: handlerAppValues
                                                    });

                                                } else {
                                                    $scope.$apply(function () {
                                                        $scope.showHandlerAppList = false;
                                                    });
                                                }
                                                //}
                                            },
                                            values: osValues
                                        });
                                    } else {
                                        $scope.$apply(function () {
                                            $scope.showOsList = false;
                                        });
                                    }

                                });
                            },
                            values: values
                        });

                    });
                });
            });

        }
    }

})();
