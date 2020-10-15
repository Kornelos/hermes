var filters = angular.module('hermes.filters', []);

filters.filter('prettyJson', function () {

    return function (jsonString) {
        return JSON.stringify(JSON.parse(jsonString), null, 2);
    }
});
