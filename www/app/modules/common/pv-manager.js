
/**
 * TODO: The PvManager will actually do following
 * 1. make REST API calls to get PVs for the input attribute
 * 2. Cache the PVs so that frequent calls are not needed
 */
angular.module('openspecimen')
  .factory('PvManager', function($http, $q, $translate, ApiUrls, ApiUtil, Site, AuthDomain, Util) {
    var url = ApiUrls.getBaseUrl() + 'permissible-values';
    
    var anatomicSites = [
      'DIGESTIVE ORGANS',
      'SKIN',
      'MALE GENITAL ORGANS',
      'UNKNOWN PRIMARY SITE',
      'PERIPHERAL NERVES AND AUTONOMIC NERVOUS SYSTEM',
      'FEMALE GENITAL ORGANS',                       
      'OTHER AND ILL-DEFINED SITES',
      'HEMATOPOIETIC AND RETICULOENDOTHELIAL SYSTEMS',
      'RETROPERITONEUM AND PERITONEUM',
      'RESPIRATORY SYSTEM AND INTRATHORACIC ORGANS',
      'BONES, JOINTS AND ARTICULAR CARTILAGE',
      'THYROID AND OTHER ENDOCRINE GLANDS',
      'MENINGES',
      'CONNECTIVE, SUBCUTANEOUS AND OTHER SOFT TISSUES',
      'BREAST',
      'LIP, ORAL CAVITY AND PHARYNX',
      'LYMPH NODES',
      'URINARY TRACT',
      'BRAIN',
      'SPINAL CORD, CRANIAL NERVES, AND OTHER PARTS OF CENTRAL NERVOUS SYSTEM',
      'EYE, BRAIN AND OTHER PARTS OF CENTRAL NERVOUS SYSTEM',
      'Not Specified'
    ];
    
    var domains = [
      'openspecimen',
      'ldap'
    ];

    var positionLabelingSchemes = [
      'Numbers',
      'Alphabets Upper Case',
      'Alphabets Lower Case',
      'Roman Upper Case',
      'Roman Lower Case'
    ];

    /** We need to have i18n keys for these as well **/
    var storageTypes = [
      'Auto',
      'Manual',
      'Virtual'
    ];

    var visitStatuses = [
      'Complete',
      'Pending'
    ];

    var specimenStatuses = [
      'Collected',
      'Missed Collection',
      'Pending'
    ];

    var activityStatuses = [
      'Active',
      'Pending',
      'Disabled',
      'Closed'
    ];

    var pvMap = {
      anatomicSite: anatomicSites,
      domains:domains,
      'storage-type': storageTypes,
      'visit-status': visitStatuses,
      'specimen-status': specimenStatuses,
      'container-position-labeling-schemes': positionLabelingSchemes,
      'activity-status': activityStatuses
    };

    var pvIdMap = {
      'clinical-status'     : '2003988',
      'gender'              : '2003989',
      'genotype'            : '2003990',
      'specimen-class'      : '2003991',
      'laterality'          : '2003992',
      'pathology-status'    : '2003993',
      'collection-procedure': '2003996',
      'collection-container': '2003997',
      'vital-status'        : '2004001',

      'ethnicity'           : 'Ethnicity_PID',
      'race'                : 'Race_PID',
      'anatomic-site'       : 'Tissue_Site_PID',
      'site-type'           : 'Site_Type_PID',
      'clinical-diagnosis'  : 'Clinical_Diagnosis_PID'
    };

    function valueOf(input) {
      return input.value;
    };

    function parentAndValue(input) {
      return {parent: input.parentValue, value: input.value};
    };

    function transform(pvs, transformfn, incParentVal, result) {
      transformfn = transformfn || (incParentVal ? parentAndValue : valueOf);
      return pvs.map(function(pv) { return transformfn(pv); });
    };

    function loadPvs(attr, srchTerm, transformFn) {
      var pvId = pvIdMap[attr];
      if (!pvId) {
        return _getPvs(attr);
      }

      return $http.get(url, {params: {attribute: pvId, searchString: srchTerm}}).then(
        function(result) {
          return transform(result.data, transformFn, null);
        }
      );
    };

    function loadPvsByParent(parentAttr, parentVal, incParentVal, transformFn) {
      var pvId = pvIdMap[parentAttr];
      if (!pvId) {
        return [];
      }

      var params = {
        parentAttribute: pvId, 
        parentValue: parentVal,  
        includeParentValue: incParentVal
      };

      return $http.get(url, {params: params}).then(
        function(result) {
          return transform(result.data, transformFn, incParentVal);
        }
      );
    };

    function  _getPvs(attr) {
      var deferred = $q.defer();
      var result = undefined;
      if (pvMap[attr]) {
        result = pvMap[attr];
      } else {
        result = [];
      }
      deferred.resolve(result);
      return deferred.promise;
    };


    return {
      getPvs: function(attr, srchTerm, transformFn) {
        var pvs = [];
        loadPvs(attr, srchTerm, transformFn).then(
          function(result) {
            Util.unshiftAll(pvs, result);
          }
        );    
        return pvs;
      },

      loadPvs: loadPvs,

      getPvsByParent: function(parentAttr, parentVal, incParentVal, transformFn) {
        var pvs = [];

        loadPvsByParent(parentAttr, parentVal, incParentVal, transformFn).then(
          function(result) {
            Util.unshiftAll(pvs, result);
          }
        );

        return pvs;
      },

      loadPvsByParent: loadPvsByParent,

      getSites: function() {
        var sites = [];
        Site.query().then(
          function(siteList) {
            angular.forEach(siteList, function(site) {
              sites.push(site.name);
            });
          }
        );
        return sites;
      },
    
      getDomains: function(callback) {
        var domains = [];
        AuthDomain.query().then(
          function(domainList) {
            angular.forEach(domainList, function(domain) {
              domains.push(domain.name);
            });

            if (callback) {
              callback();
            }
          }
        );
        return domains;
      },

      notSpecified: function() {
        return $translate.instant('pvs.not_specified');
      }
    };
  });
