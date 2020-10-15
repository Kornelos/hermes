var consistency = angular.module('hermes.consistency', [
    'ui.bootstrap',
    'hermes.consistency.repository'
]);

consistency.controller('ConsistencyController', ['ConsistencyRepository', '$scope', '$state',
    function (consistencyRepository, $scope, $state) {

        $scope.fetching = true;
        $scope.inconsistentGroups = [];

        consistencyRepository.findInconsistentGroups().$promise
            .then(function (response) {
                $scope.inconsistentGroups = response;
            })
            .catch(function (response) {
                console.log(response);
            })
            .finally(function () {
                $scope.fetching = false;
            });

        $scope.goToGroup = function (group) {
            $state.go('groupConsistency', { groupName: group.name, group: group });
        };

    }]);

consistency.controller('GroupConsistencyController', ['$scope', '$stateParams', '$state',
    function ($scope, $stateParams, $state) {

        if (!$stateParams.group) {
            $state.go('consistency');
        }

        $scope.groupName = $stateParams.groupName;
        $scope.group = $stateParams.group;

        $scope.goToTopic = function (topic) {
            $state.go('topicConsistency', { groupName: $scope.groupName, topicName: topic.name, topic: topic });
        };
    }]);

consistency.controller('TopicConsistencyController', ['$scope', '$stateParams',
    function ($scope, $stateParams) {

        $scope.groupName = $stateParams.groupName;
        $scope.topicName = $stateParams.topicName;
        $scope.topic = $stateParams.topic;
    }]);
