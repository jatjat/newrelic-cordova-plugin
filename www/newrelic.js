/*global cordova, module*/

module.exports = {
    greet: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'NewRelicCordovaPlugin', 'greet', [name]);
    },
    recordCustomEvent: function (eventType, eventName, eventAttributes, successCallback, errorCallback) {
        var invalid = false;

        for (var i = 0; i < eventAttributes.length; i++) {
            var ele = eventAttributes[i];

            if (Array.isArray(ele) || typeof ele === 'object') {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            errorCallback('Each event attribute cannot be a list or object');
        } else {
            cordova.exec(successCallback, errorCallback, 'NewRelicCordovaPlugin', 'RecordCustomEvent', [eventType, eventName, eventAttributes]);
        }
    }
};