/***********************************************************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 * 
 * This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8
 * \#317790)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
define(function(require) {
	var app = require('durandal/app'), ko = require('knockout'), http = require('plugins/http'), d3 = require('d3'), JsonHuman = require('json_human'), comot = require('comot_client'), utils = require('comot_utils'), $ = require("jquery"), bootstrap = require('bootstrap'), router = require('plugins/router');

	var repeaterModule = require('repeater');
	var notify = require('notify');

	var repeater = repeaterModule.create("Changes", 10000);
	var LONG_MAX = 9223372036854776000;
	var LONG_MAX_STRING = "9223372036854775807";

	var model = {
		// properties
		serviceId : ko.observable(""),
		selectedEvent : ko.observable(),
		selectedObject : ko.observable(),
		changes : ko.observableArray(),

		// functions
		eventDetails : function(event) {

			model.selectedEvent(event);

			var viewEvent = $.extend(true, {}, event);
			delete viewEvent.propertiesMap;
			delete viewEvent.eventTime;
			delete viewEvent.timestamp;
			delete viewEvent.from;
			delete viewEvent.to;
			delete viewEvent.type;
			delete viewEvent.targetObjectId;
			delete viewEvent.origin;
			delete viewEvent.eventName;
			delete viewEvent.time;

			if (typeof viewEvent.exceptionDetail !== 'undefined') {
				try {
					viewEvent.exceptionDetail = jQuery.parseJSON(viewEvent.exceptionDetail);
				} catch (e) {
				}
			}
			if (typeof viewEvent.optionalMessage !== 'undefined') {
				try {
					viewEvent.optionalMessage = jQuery.parseJSON(viewEvent.optionalMessage);
				} catch (e) {
				}
			}

			$("#eventDetails").empty();
			var details = JsonHuman.format(viewEvent);

			if (details == null) {
				details = "<p>No details</p>";
			}
			$("#eventDetails").append(details);

		},
		serviceData : function(event) {
			getServiceRevision(model.serviceId(), model.serviceId(), event.timestamp);
		},
		objectData : function(event) {
			getServiceRevision(model.serviceId(), event.targetObjectId, event.timestamp);
		},
		// life-cycle
		activate : function(serviceId) {
			model.serviceId(serviceId);
		},
		attached : function() {
			repeater.runWith(model.serviceId(), function() {
				refreshChanges(model.serviceId());
			})
		},
		detached : function() {
			repeater.stop();
		}
	};

	return model;

	function getServiceRevision(serviceId, objectId, timestamp) {
		var timeToUse;

		if (timestamp == LONG_MAX) {
			timeToUse = LONG_MAX_STRING;
		} else {
			timeToUse = timestamp + 1;
		}

		comot.getRecording(serviceId, objectId, timeToUse, function(data) {
			$("#output_revisions").html(JsonHuman.format(data));
			$('#myModal').modal();
			
			model.selectedObject(objectId);

		}, function(error) {
			$("#output_revisions").html("");
			notify.info("No revision for service '"
					+ serviceId
					+ "', object '"
					+ objectId
					+ ((timeToUse == LONG_MAX_STRING) ? " currently valid" : " ' at time '"
							+ utils.longToDateString(timeToUse)) + "'");
		});

	}

	function refreshChanges(serviceId) {

		// refresh changes
		comot.getAllEvents(serviceId, function(data) {
			model.changes.removeAll();
			for (var i = data.length - 1; i >= 0; i--) {

				var event = data[i];
				var propsArr = event.propertiesMap.entry;
				var props = {};

				for (var j = 0; j < propsArr.length; j++) {
					event[propsArr[j].key] = propsArr[j].value.value;
				}

				// event.props = props;
				event.time = utils.longToDateString(event.eventTime);

				model.changes.push(event);
			}
		}, function(error) {
			model.changes.removeAll();
			notify.info("No changes for service '" + serviceId + "'");
		})
	}

});
