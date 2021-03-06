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
package at.ac.tuwien.dsg.comot.m.core.test.analysis;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticPlanReport;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticityAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.analytics.TimeAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextTest.class, AppContextCore.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class AnalyticsManualTest {

	private static final Logger LOG = LoggerFactory.getLogger(AnalyticsManualTest.class);

	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected ChangeRepo changeRepo;

	@Autowired
	private TimeAnalyzis deplTimesAnalysis;
	@Autowired
	protected ElasticityAnalyzis elastAnalysis;

	@Test
	public void deplTimesTest() throws JAXBException, InstantiationException, IllegalAccessException,
			IllegalArgumentException,
			ClassNotFoundException, RecorderException {

		for (ElasticPlanReport report : elastAnalysis.doOneService("HelloElasticity_1")) {
			LOG.info("{}", Utils.asJsonString(report));
		}

	}

	@Test
	public void alastAnalysisTest() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, RecorderException {

		CloudService service = STemplates.fullService();
		cutResources(service);
		Navigator nav = new Navigator(service);
		String osInstId = STemplates.instanceId(STemplates.osNodeId);

		// change 0 created the instance

		Long time1 = System.currentTimeMillis();
		Map<String, Object> changeProperties = createProps(Action.DEPLOYMENT_STARTED.toString(), time1);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// change 1 still deploying

		nav.getInstance(osInstId).setState(State.DEPLOYING);
		nav.getInstance(osInstId).setEnvId("aaa");

		changeProperties = createProps("CONFIGURING", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_CUSTOM, changeProperties);

		// change 2 finished deployemnt

		nav.getInstance(osInstId).setState(State.RUNNING);
		Long time2 = System.currentTimeMillis();
		changeProperties = createProps(Action.DEPLOYED.toString(), time2);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// change 3

		nav.getInstance(osInstId).setEnvId("bbb");

		changeProperties = createProps("SOME_EVENT", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, Recording.CHANGE_TYPE_LIFECYCLE, changeProperties);

		// UtilsTest.sleepInfinit();

		deplTimesAnalysis.deploymentEvents(STemplates.serviceId);

	}

	public static Map<String, Object> createProps(String action, Long time) {
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put(Recording.PROP_EVENT_NAME, action);
		changeProperties.put(Recording.PROP_EVENT_TIME, time);
		return changeProperties;
	}

	public static void cutResources(CloudService service) {
		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			OfferedServiceUnit osu = unit.getOsuInstance().getOsu();
			osu.setResources(null);
		}
	}

}
