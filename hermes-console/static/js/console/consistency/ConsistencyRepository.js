var repository = angular.module('hermes.consistency.repository', []);

repository.factory('ConsistencyRepository', ['DiscoveryService', '$resource',
    function (discovery, $resource) {

        var consistencyResource = $resource(discovery.resolve('/consistency'));

        return {
            findInconsistentGroups: consistencyResource.query
        };
    }]);
