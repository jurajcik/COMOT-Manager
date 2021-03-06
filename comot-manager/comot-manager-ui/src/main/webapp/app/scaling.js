/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
		instanceId : ko.observable(""),
		selectedObjectId : ko.observable(""),
		selectedTime : ko.observable(LONG_MAX),
		selectedEvent : ko.observable(),
		serviceObj : ko.observableArray(),
		topologiesObj : ko.observableArray(),
		unitsObj : ko.observableArray(),
		otherObj : ko.observableArray(),
		changes : ko.observableArray(),
		// functions
		revisionsForChange : function(event) {


			var viewEvent = $.extend(true, {}, event);
			
			delete viewEvent.propertiesMap;
			delete viewEvent.eventTime;
			delete viewEvent.timestamp;
			delete viewEvent.from;
			delete viewEvent.to;
			
			$("#eventDetails").html(JsonHuman.format(viewEvent));
			$('#myModal').modal()

			model.selectedEvent(event);
			model.selectedTime(event.timestamp);
			//getRevision();
		},
		revisionsForObject : function(id) {
			model.selectedObjectId(id);
			getRevision();
		},
		// life-cycle
		activate : function(serviceId, instanceId) {

			model.serviceId(serviceId);
			model.instanceId(instanceId);

		},
		attached : function() {

			showThisInstance(model.serviceId(), model.instanceId());

			$(document).ready(
					function() {

						$('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title',
								'Collapse this branch');
						$('.tree li.parent_li > span').on(
								'click',
								function(e) {
									var children = $(this).parent('li.parent_li').find(' > ul > li');
									if (children.is(":visible")) {
										children.hide('fast');
										$(this).attr('title', 'Expand this branch').find(' > i').addClass(
												'icon-plus-sign').removeClass('icon-minus-sign');
									} else {
										children.show('fast');
										$(this).attr('title', 'Collapse this branch').find(' > i').addClass(
												'icon-minus-sign').removeClass('icon-plus-sign');
									}
									e.stopPropagation();
								});
					});
		},
		detached : function() {
			repeater.stop();
		}
	};

	return model;

	function showThisInstance(serviceId, instanceId) {

		repeater.runWith(instanceId, function() {

			if (model.selectedObjectId() == "" || model.instanceId() != instanceId) {
				model.selectedObjectId(model.serviceId());
				model.selectedTime(LONG_MAX);
			}

			model.serviceId(serviceId);
			model.instanceId(instanceId);

			// refresh objects
			comot.getObjects(instanceId, function(data) {
				model.serviceObj.removeAll();
				model.topologiesObj.removeAll();
				model.unitsObj.removeAll();
				model.otherObj.removeAll();

				for (var i = 0; i < data.length; i++) {

					if (data[i].label == "CloudService") {
						model.serviceObj.push(data[i].id);
					} else if (data[i].label == "ServiceTopology") {
						model.topologiesObj.push(data[i].id);
					} else if (data[i].label == "ServiceUnit") {
						model.unitsObj.push(data[i].id);
					} else {
						model.otherObj.push(data[i].id);
					}
				}

				refreshChanges();

			}, function(error) {
				model.serviceObj.removeAll();
				model.topologiesObj.removeAll();
				model.unitsObj.removeAll();
				model.otherObj.removeAll();
				$("#output_revisions").html("");
				model.changes.removeAll();
				notify.info("No managed objects for service '" + instanceId + "'");
			});

		})
	}

	function getRevision() {
		var timestamp;
		var instanceId = model.instanceId();
		var objectId = model.selectedObjectId();

		if (model.selectedTime() == LONG_MAX) {
			timestamp = LONG_MAX_STRING;
		} else {
			timestamp = model.selectedTime() + 1;
		}

		comot.getRecording(instanceId, objectId, timestamp, function(data) {
			$("#output_revisions").html(JsonHuman.format(data));
		}, function(error) {
			$("#output_revisions").html("");
			notify.info("No revision for service '"
					+ instanceId
					+ "', object '"
					+ objectId
					+ ((timestamp == LONG_MAX_STRING) ? " currently valid" : " ' at time '"
							+ utils.longToDateString(timestamp)) + "'");
		});

		refreshChanges();
	}

	function refreshChanges() {

		var instanceId = model.instanceId();
		var objectId = model.selectedObjectId();

		// refresh changes
		comot.getAllEvents(instanceId, function(data) {
			model.changes.removeAll();
			for (var i = 0; i < data.length; i++) {
				
				var event = data[i];
				var propsArr = event.propertiesMap.entry;
				var props = {};
				
				for (var j = 0; j < propsArr.length; j++) {
					event[propsArr[j].key] = propsArr[j].value.value;
				}
				
				//event.props = props;
				event.time = utils.longToDateString(event.eventTime);
				
				model.changes.push(event);
			}
		}, function(error) {
			model.changes.removeAll();
			notify.info("No changes for service '" + instanceId + "', object '" + objectId + "'");
		})
	}

});
