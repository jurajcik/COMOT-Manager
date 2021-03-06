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
package at.ac.tuwien.dsg.comot.m.common.event.state;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.exception.ExceptionUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExceptionMessage extends ComotMessage {

	private static final long serialVersionUID = -3675993125733600072L;

	@XmlAttribute
	protected String serviceId;
	@XmlAttribute
	protected String origin;
	@XmlAttribute
	protected Long time;
	@XmlElement(name = "exceptionType")
	// !!! in JSON name 'type' together with inheritance would cause problems
	protected String type;
	@XmlElement
	protected String message;
	@XmlElement
	protected String details;
	@XmlElement
	protected String eventCauseId;

	public ExceptionMessage() {

	}

	public ExceptionMessage(String serviceId, String origin, Long time, String eventCauseId, Exception exception) {
		super();

		Throwable root = ExceptionUtils.getRootCause(exception);
		String type;
		String message;
		String details;

		if (root == null) {
			type = exception.getClass().getName();
			message = exception.getMessage();
			details = ExceptionUtils.getStackTrace(exception);
		} else {
			type = root.getClass().getName();
			message = root.getMessage();
			details = ExceptionUtils.getStackTrace(root);
		}

		this.origin = origin;
		this.type = type;
		this.message = message;
		this.details = details;
		this.serviceId = serviceId;
		this.time = time;
		this.eventCauseId = eventCauseId;
	}

	public ExceptionMessage(String serviceId, String origin, Long time, String type, String message, String details,
			String eventCauseId) {
		super();
		this.serviceId = serviceId;
		this.origin = origin;
		this.time = time;
		this.type = type;
		this.message = message;
		this.details = details;
		this.eventCauseId = eventCauseId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getEventCauseId() {
		return eventCauseId;
	}

	public void setEventCauseId(String eventCauseId) {
		this.eventCauseId = eventCauseId;
	}

	@Override
	public String toString() {
		return "ExceptionMessage [serviceId=" + serviceId + ", origin=" + origin
				+ ", time=" + time + ", type=" + type + ", message=" + message + ", details=" + details + "]";
	}

}
