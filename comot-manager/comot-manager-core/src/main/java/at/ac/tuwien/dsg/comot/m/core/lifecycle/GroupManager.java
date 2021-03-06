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
package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class GroupManager {

	private static final Logger LOG = LoggerFactory.getLogger(GroupManager.class);

	protected String serviceId;
	protected Group serviceGroup;
	protected Group serviceGroupReadOnly;
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	public Group checkAndExecute(Action action, String groupId) throws ComotLifecycleException,
			IOException, ClassNotFoundException {

		Group group = serviceGroup.getMemberNested(groupId);

		if (group == null) {
			throw new ComotLifecycleException("The group '" + groupId + "' does not exist");
		}

		LOG.info(logId() + "checkAndExecute BEFORE groupId={} : {}", groupId, serviceGroup);

		if (!group.canExecute(action)) {

			throw new ComotLifecycleException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'\n" + group.notAllowedExecutionReason(action));
		}

		group.executeAction(action, strategy);
		serviceGroupReadOnly = (Group) Utils.deepCopy(serviceGroup);

		LOG.info(logId() + "checkAndExecute AFTER groupId={} : {}", groupId, serviceGroup);

		return group;
	}

	public void check(Action action, String groupId) throws ComotLifecycleException {

		Group group = serviceGroup.getMemberNested(groupId);

		if (!group.canExecute(action)) {
			throw new ComotLifecycleException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'. Group " + groupId + "\n" + group.notAllowedExecutionReason(action));
		}
	}

	public Group createGroupService(Action action, CloudService service) throws ComotLifecycleException,
			IOException, ClassNotFoundException {

		if (serviceGroup != null) {
			throw new ComotLifecycleException("Action '" + action
					+ "' is not allowed to be targeted at an existing CloudServiceInstance");
		}

		serviceGroup = new Group(service, State.INIT);

		for (Group group : serviceGroup.getAllMembersNested()) {
			lastStates.put(group.getId(), State.INIT);
		}

		checkAndExecute(action, serviceGroup.getId());
		LOG.info(serviceGroup.toString());

		return serviceGroup;
	}

	public Group createGroupUnitTopo(Action action, String parentId, ServiceEntity entity)
			throws ComotLifecycleException, ClassNotFoundException, IOException {

		String groupId = entity.getId();

		if (containsGroup(groupId)) {
			throw new ComotLifecycleException("The same group '" + groupId + "' can not be created twice");
		}

		if (entity instanceof ServiceUnit) {
			getGroup(parentId).addGroup(new Group((ServiceUnit) entity, State.INIT));
		} else if (entity instanceof ServiceTopology) {
			getGroup(parentId).addGroup(new Group((ServiceTopology) entity, State.INIT));
		} else {
			throw new IllegalArgumentException(entity.getClass().getName());
		}

		try {
			Group result;

			result = checkAndExecute(action, groupId);
			lastStates.put(groupId, State.INIT);

			return result;

		} catch (ComotLifecycleException e) {
			serviceGroup.removeMemberNested(groupId);
			throw e;
		}
	}

	public Group createGroupInstance(Action action, String parentId, UnitInstance instance)
			throws ClassNotFoundException, IOException, ComotException, ComotLifecycleException {

		String groupId = instance.getId();

		if (!containsGroup(parentId)) {
			throw new ComotLifecycleException("No such group: '" + parentId + "'");
		}

		getGroup(parentId).addGroup(new Group(groupId, Type.INSTANCE, State.INIT));

		try {
			Group result;

			result = checkAndExecute(action, groupId);
			lastStates.put(groupId, State.INIT);

			return result;

		} catch (ComotLifecycleException e) {
			serviceGroup.removeMemberNested(groupId);
			throw e;
		}
	}

	public Map<String, Transition> extractTransitions(AbstractEvent event) throws ClassNotFoundException,
			IOException {

		Map<String, Transition> transitions = new HashMap<>();

		if (event instanceof LifeCycleEvent) {

			Map<String, State> tempStates = new HashMap<>();
			boolean fresh;

			for (Group tempG : serviceGroup.getAllMembersNested()) {

				if (tempG.getCurrentState() != State.FINAL) {
					tempStates.put(tempG.getId(), tempG.getCurrentState());
				}

				if (tempG.getCurrentState().equals(lastStates.get(tempG.getId()))) {
					fresh = false;
				} else {
					fresh = true;
				}
				transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
						tempG.getCurrentState(), fresh));
			}
			lastStates = tempStates;

		} else if (event instanceof CustomEvent) {
			for (Group tempG : serviceGroup.getAllMembersNested()) {
				transitions.put(tempG.getId(), new Transition(
						tempG.getId(), tempG.getType(), tempG.getPreviousState(), tempG.getCurrentState(), false));
			}
		}

		return transitions;
	}

	public boolean containsGroup(String groupId) {
		return serviceGroup.getMemberNested(groupId) != null;
	}

	public Group getGroup(String groupId) {
		return serviceGroup.getMemberNested(groupId);
	}

	public State getCurrentState(String groupId) {

		if (serviceGroupReadOnly == null || serviceGroupReadOnly.getMemberNested(groupId) == null) {
			return null;
		}
		final State temp = serviceGroupReadOnly.getMemberNested(groupId).getCurrentState();

		LOG.debug("getCurrentState(instanceId={}, groupId={}): {}", serviceId, groupId, temp);

		return temp;
	}

	public Map<String, Transition> getCurrentState() {
		Map<String, Transition> transitions = new HashMap<>();

		if (serviceGroupReadOnly == null) {
			return null;
		}

		for (Group tempG : serviceGroupReadOnly.getAllMembersNested()) {
			transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
					tempG.getCurrentState(), false));
		}
		return transitions;
	}

	public String logId() {
		return "[ MANAGER_" + serviceId + "] ";
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}
