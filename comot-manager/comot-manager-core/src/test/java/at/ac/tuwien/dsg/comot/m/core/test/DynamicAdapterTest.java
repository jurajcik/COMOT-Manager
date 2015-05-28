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
package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsT;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCoreInsertData;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TeAgentAdapter;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class DynamicAdapterTest extends AbstractTest {

	protected TeAgentAdapter agent;
	protected String serviceId;
	protected String staticDeplId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, EpsException {

		agent = new TeAgentAdapter("prototype", env.getProperty(ConfigConstants.BROKER_HOST));

		staticDeplId = infoService.instanceIdOfStaticEps(AppContextCoreInsertData.SALSA_SERVICE_STATIC);

	}

	@Test
	public void testDynamic() throws Exception {

		OfferedServiceUnit melaOsu = infoService.getOsu(AppContextCoreInsertData.RSYBL_SERVICE_DYNAMIC);

		coordinator.createDynamicEps(melaOsu.getId(), null);

		UtilsT.sleepInfinit();

	}
}
