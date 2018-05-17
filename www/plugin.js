
var exec = require('cordova/exec');

var PLUGIN_NAME = 'CustomAccessibilityPlugin';

var AccessibilityPlugin = {
  check: function(obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "checkAccessibility", [obj]);
  },
  start: function (obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "startAccessibility", [obj]);
  },
  stop: function(obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "stopAccessibility", [obj]);
  }

};

module.exports = AccessibilityPlugin;