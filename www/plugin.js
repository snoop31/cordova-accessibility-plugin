
var exec = require('cordova/exec');

var PLUGIN_NAME = 'AccessibilityPlugin';

var AccessibilityPlugin = {
  check: function(obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "checkAccessibility", [obj]);
  },
  start: function (obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "startAccessibility", [obj]);
  },
  stop: function(obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "stopAccessibility", [obj]);
  },
  open: function(successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "openAccessibility", null);
  },
  action: function(obj, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "actionAccessibility", [obj]);
  },
  mode: function(successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "checkMultiWindowMode");
  },
  service: {
    check: function(successCallback, errorCallback){
      exec(successCallback, errorCallback, PLUGIN_NAME, "checkService", null);
    },
    stop: function(successCallback, errorCallback){
      exec(successCallback, errorCallback, PLUGIN_NAME, "stopService", null);
    }
  }
};

module.exports = AccessibilityPlugin;
