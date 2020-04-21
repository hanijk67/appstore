var makeReady = function (inputData) {

    var app = angular.module('appDashboard', []);

    app.controller('commentOnDashboardController', function ($scope, $rootScope) {

        $rootScope.$on('getLastCommentsEvent', function () {
            var commentVoLst = JSON.parse(inputData);

            $scope.$apply(function () {
                $scope.commentVoList = commentVoLst;
            });
        });

    });



    app.run(function ($rootScope) {

    });

    //boot-strap angular!
    angular.bootstrap($('div[jid="appDashboardParent"]'), ['appDashboard']);

    var angScope = $('div[jid="appDashboardParent"]').closest('div').scope()
    var $rootScope = angScope.$root;

    $rootScope.$emit('getLastCommentsEvent', inputData);

};