'use strict';

angular.module('openspecimen', [
  'os.common',
  'os.biospecimen',
  'os.administrative',
  'os.query',

  'ngMessages',
  'ngCookies',
  'ngSanitize', 
  'ngGrid',
  'ui.router', 
  'ui.bootstrap', 
  'ui.mask', 
  'ui.keypress', 
  'ui.select',
  'ui.sortable',
  'ui.autocomplete',
  'mgcrea.ngStrap.popover',
  'angular-loading-bar',
  'pascalprecht.translate'])

  .config(function(
    $stateProvider, $urlRouterProvider, 
    $httpProvider, $translateProvider,
    uiSelectConfig, ApiUrlsProvider) {

    $translateProvider.useStaticFilesLoader({
      prefix: 'modules/i18n/',
      suffix: '.js'
    });

    $translateProvider.preferredLanguage('en_US');

    $stateProvider
      .state('default', {
        abstract: true,
        templateUrl: 'modules/common/default.html',
        controller: function($scope, Alerts) {
          $scope.alerts = Alerts.messages;
        }
      })
      .state('signed-in', {
        abstract: true,
        templateUrl: 'modules/common/appmenu.html',
        resolve: {
          currentUser: function(User) {
            return User.getCurrentUser();
          }
        },
        controller: 'SignedInCtrl'
      });

    $urlRouterProvider.otherwise('/');

    $httpProvider.interceptors.push('httpRespInterceptor');

    /*ApiUrlsProvider.hostname = "localhost"; // used for testing purpose
    ApiUrlsProvider.port = 9090;*/
    ApiUrlsProvider.secure = false;
    ApiUrlsProvider.app = "/openspecimen";
    ApiUrlsProvider.urls = {
      'sessions': '/rest/ng/sessions',
      'sites': '/rest/ng/sites',
      'form-files': '/rest/ng/form-files'
    };

    uiSelectConfig.theme = 'bootstrap';
  })
  .factory('httpRespInterceptor', function($q, $injector, Alerts, $window) {
    return {
      request: function(config) {
        return config || $q.when(config);
      },

      requestError: function(rejection) {
        $q.reject(rejection);
      },

      response: function(response) {
        return response || $q.when(response);
      },

      responseError: function(rejection) {
        if (rejection.status == 0) {
          Alerts.error("common.server_connect_error");
        } else if (rejection.status == 401) {
          delete $window.localStorage['osAuthToken'];
          delete $injector.get("$http").defaults.headers.common['X-OS-API-TOKEN'];
          $injector.get('$state').go('login'); // using injector to get rid of circular dependencies
        } else if (rejection.status / 100 == 5) {
          Alerts.error("common.server_error");
        } else if (rejection.status / 100 == 4) {
          var errMsgs = [];

          if (rejection.data instanceof Array) {
            angular.forEach(rejection.data, function(err) {
              errMsgs.push(err.message + "(" + err.code + ")");
            });
            Alerts.errorText(errMsgs);
          } else if (rejection.config.method != 'HEAD') {
            Alerts.error('common.ui_error');
          }
        } 

        return $q.reject(rejection);
      }
    };
  })
  .factory('ApiUtil', function($window, $http) {
    return {
      processResp: function(result) {
        var response = {};
        if (result.status / 100 == 2) {
          response.status = "ok";
        } else if (result.status / 100 == 4) {
          response.status = "user_error";
        } else if (result.status / 100 == 5) {
          response.status = "server_error";
        }

        response.data = result.data;
        return response;
      },

      initialize: function(token) {
        $http.defaults.headers.common['X-OS-API-CLIENT'] = "webui";

        if (!token) {
          token = $window.localStorage['osAuthToken'];
          if (!token) {
            return;
          }
        }

        $http.defaults.headers.common['X-OS-API-TOKEN'] = token;
        $http.defaults.withCredentials = true;
      }
    };
  })
  .provider('ApiUrls', function() {
    var that = this;

    this.hostname = "";
    this.port = "";
    this.secure = false;
    this.app = "";
    this.urls = {};

    this.$get = function() {
      return {
        hostname: that.hostname,
        port    : that.port,
        secure  : that.secure,
        app     : that.app,
        urls    : that.urls,

        getBaseUrl: function() {
          var prefix = '';
          if (this.hostname) {
            var protocol = this.secure ? 'https://' : 'http://';
            prefix = protocol + this.hostname + ':' + this.port;
          }

          return prefix + this.app + '/rest/ng/';
        },

        getUrl: function(key) {
          var url = '';
          if (key) {
            url = this.urls[key];
          }

          var prefix = "";
          if (this.hostname) {
            var protocol = this.secure ? 'https://' : 'http://';
            prefix = protocol + this.hostname + ":" + this.port;
          }

          return prefix + this.app + url;
        }
      };
    }
  })
  .run(function($rootScope, $window, $cookieStore, ApiUtil) {
    if ($window.localStorage['osAuthToken']) {
      $cookieStore.put('osAuthToken', $window.localStorage['osAuthToken']);
      $rootScope.loggedIn = true;
    }

    ApiUtil.initialize();

    $rootScope.$on('$stateChangeSuccess', 
      function(event, toState, toParams, fromState, fromParams) {
        $rootScope.state = toState;
      });

    $rootScope.back = function() {
      $window.history.back();
    };

    $rootScope.global = {
      defaultDomain: 'openspecimen',
      dateFmt: 'MMM dd, yyyy',
      queryDateFmt: {format: 'mm-dd-yyyy'},
      timeFmt: 'hh:mm',
      dateTimeFmt: 'MMM dd, yyyy hh:mm',
      filterWaitInterval: 500
    };
  });
