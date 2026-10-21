/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/
package org.osgi.test.cases.upnp.tbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;
import org.osgi.test.cases.upnp.tbc.device.discovery.ServicesListener;
import org.osgi.test.cases.upnp.tbc.export.UPnPExportedDevice;
import org.osgi.test.cases.upnp.tbc.parser.XMLParser;
import org.osgi.test.cases.upnp.tbc.parser.XMLTag;

public class UPnPControl extends UPnPTestSupport {
	private final int			desiredCount	= 3;
	private HttpService			http;
	private TestStarter			start;
	private ServicesListener	listener;

	@BeforeEach
	public void setUp() throws Exception {
		System.out.println("Prepare for UPnP Test Case");
		prepareTestStart();
		System.out.println("Prepared for UPnP Test Case");
	}

	@AfterEach
	public void tearDown() throws Exception {
		System.out.println("Tear down UPnP Test Case");
		finalizeTestEnd();
		System.out.println("Torn down UPnP Test Case");
	}

	// ==========================================TEST
	// METHODS====================================================================//
	@Test
	public void testDiscovery() {
		UPnPDevice dev = listener.getUPnPDevice();
		assertThat(dev).as("UPnPDevice is NULL").isNotNull();
		String udn = (String) dev.getDescriptions(null).get(UPnPDevice.UDN);
		Dictionary<String,Object> dict = dev.getDescriptions(null);
		String test = (String) dict.get(UPnPDevice.UPC);
		System.out.println("UPnPDevice UPC: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD + " UPC not matched").isEqualTo(UPnPConstants.V_UPC);
		test = (String) dict.get(UPnPDevice.FRIENDLY_NAME);
		System.out.println("UPnPDevice FRIENDLY_NAME: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Friendly name not matched").isEqualTo(UPnPConstants.V_FN);
		test = (String) dict.get(UPnPDevice.MANUFACTURER);
		System.out.println("UPnPDevice MANUFACTURER: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Manufacturer not matched").isEqualTo(UPnPConstants.V_MANU);
		test = (String) dict.get(UPnPDevice.MANUFACTURER_URL);
		System.out.println("UPnPDevice MANUFACTURER_URL: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Manufacturer URL not matched").isEqualTo(UPnPConstants.V_MANU_URL);
		test = (String) dict.get(UPnPDevice.MODEL_DESCRIPTION);
		System.out.println("UPnPDevice MODEL_DESCRIPTION: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Model Description not matched").isEqualTo(UPnPConstants.V_MOD_DESC);
		test = (String) dict.get(UPnPDevice.MODEL_NAME);
		System.out.println("UPnPDevice MODEL_NAME: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Model Name not matched").isEqualTo(UPnPConstants.V_MOD_NAME);
		test = (String) dict.get(UPnPDevice.MODEL_NUMBER);
		System.out.println("UPnPDevice MODEL_NUMBER: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Model Number not matched").isEqualTo(UPnPConstants.V_MOD_NUMB);
		test = (String) dict.get(UPnPDevice.MODEL_URL);
		System.out.println("UPnPDevice MODEL_URL: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Model URL not matched").isEqualTo(UPnPConstants.V_MOD_URL);
		test = (String) dict.get(UPnPDevice.SERIAL_NUMBER);
		System.out.println("UPnPDevice SERIAL_NUMBER: " + test);
		assertThat(test).as("SERVER: " + udn + UPnPConstants.DD
				+ " Serial Number not matched").isEqualTo(UPnPConstants.V_SN);
		UPnPIcon[] icos = dev.getIcons("En");
		if (icos.length == 2) {
			for (int i = 0; i < icos.length; i++) {
				InputStream fis = null;
				if (icos[i].getHeight() == 16) {
					if (icos[i].getWidth() != 16) {
						fail("SERVER: " + udn + UPnPConstants.DD
								+ " Not property width for icon: " + i);
					}
					fis = getClass()
							.getResourceAsStream(
									"/org/osgi/test/cases/upnp/tbc/resources/images/small.gif");
				}
				else
					if (icos[i].getHeight() == 32) {
						if (icos[i].getWidth() != 32) {
							fail("SERVER: " + udn + UPnPConstants.DD
									+ " Not property width for icon: " + i);
						}
						fis = getClass()
								.getResourceAsStream(
										"/org/osgi/test/cases/upnp/tbc/resources/images/big.gif");
					}
					else {
						fail("SERVER: " + udn + UPnPConstants.DD
								+ " Not property height for icon: " + i);
					}
				if (icos[i].getDepth() != 8) {
					fail("SERVER: " + udn + UPnPConstants.DD
							+ " Not property width for icon: " + i);
				}
				if (!icos[i].getMimeType().equals(UPnPConstants.V_MT)) {
					fail("SERVER: " + udn + UPnPConstants.DD
							+ " Not property mime type for icon: " + i);
				}
				if (fis != null) {
					try {
						InputStream ricon = icos[i].getInputStream();
						byte[] bfis = readFully(fis);
						byte[] bricon = readFully(ricon);
						if (!checkArrays(bfis, bricon)) {
							fail("SERVER: " + udn + UPnPConstants.DD
									+ " Icons are not the same: " + i);
						}
						else {
							System.out.println("Compared icons are the same.Icon test is OK");
						}
						ricon.close();
						fis.close();
					}
					catch (Exception exc) {
						fail("SERVER: " + udn + UPnPConstants.DD
								+ " Exception :" + exc + " while icon: " + i, exc);
					}
				}
				else {
					fail("SERVER: " + udn + UPnPConstants.DD
							+ " Unable to get file for icon: " + i);
				}
			}
		}
		else {
			fail("SERVER: " + udn + UPnPConstants.DD + " Wrong Number of Icons");
		}
	}

	@Test
	public void testControl() {
		UPnPDevice dev = listener.getUPnPDevice();
		assertThat(dev).as("UPnPDevice is NULL").isNotNull();
		UPnPService cs = dev.getService(UPnPConstants.SCONT_ID);
		Hashtable<String,Object> hash = new Hashtable<>();
		hash.put(UPnPConstants.N_IN_OUT, UPnPConstants.V_IN_OUT);
		UPnPAction act = cs.getAction(UPnPConstants.ACT_PF);
		System.out.println("Invoking an action Post Fail");
		try {
			act.invoke(hash);
			fail("SERVER: Exception not thrown while invoking: "
					+ UPnPConstants.ACT_PF);
		}
		catch (Exception exc) {
			System.out.println("Exception is thrown OK");
		}
		act = cs.getAction(UPnPConstants.ACT_MPF);
		System.out.println("Invoking an action MPost Fail");
		try {
			act.invoke(hash);
			fail("SERVER: Exception not thrown while invoking: "
					+ UPnPConstants.ACT_MPF);
		}
		catch (Exception exc) {
			// exc.printStackTrace();
			System.out.println("Exception is thrown OK");
		}
		act = cs.getAction(UPnPConstants.ACT_PB);
		System.out.println("Invoking an action Post Block.Please wait");
		try {
			act.invoke(hash);
			fail("SERVER: Exception not thrown while invoking: "
					+ UPnPConstants.ACT_PB);
		}
		catch (Exception exc) {
			System.out.println("Exception is thrown OK");
		}
		act = cs.getAction(UPnPConstants.ACT_MPB);
		System.out.println("Invoking an action MPost Block.Please wait");
		try {
			act.invoke(hash);
			fail("SERVER: Exception not thrown while invoking: "
					+ UPnPConstants.ACT_MPB);
		}
		catch (Exception exc) {
			System.out.println("Exception is thrown OK");
		}
		act = cs.getAction(UPnPConstants.ACT_POS);
		System.out.println("Invoking an action Post Out Success");
		try {
			Dictionary<String,Object> dict = act.invoke(hash);
			checkRet(dict);
		}
		catch (Exception exc) {
			fail("Unexpected Exception", exc);
		}
		act = cs.getAction(UPnPConstants.ACT_MPOS);
		System.out.println("Invoking an action MPost Out Success");
		try {
			Dictionary<String,Object> dict = act.invoke(hash);
			checkRet(dict);
		}
		catch (Exception exc) {
			fail("Unexpected Exception", exc);
		}
		hash.clear();
		Double duble = Double.valueOf(UPnPConstants.V_IN_NUMBER);
		Float fl = Float.valueOf(UPnPConstants.V_IN_FLOAT);
		hash.put(UPnPConstants.N_IN_INT, UPnPConstants.V_IN_INT);
		hash.put(UPnPConstants.N_IN_UI4, UPnPConstants.V_IN_UI4);
		hash.put(UPnPConstants.N_IN_FLOAT, fl);
		hash.put(UPnPConstants.N_IN_NUMBER, duble);
		hash.put(UPnPConstants.N_IN_CHAR, UPnPConstants.V_IN_CHAR);
		hash.put(UPnPConstants.N_IN_STRING, UPnPConstants.V_IN_STRING);
		hash.put(UPnPConstants.N_IN_BOOLEAN, UPnPConstants.V_IN_BOOLEAN);
		hash.put(UPnPConstants.N_IN_HEX, UPnPConstants.V_IN_HEX.getBytes());
		act = cs.getAction(UPnPConstants.ACT_PIS);
		System.out.println("Invoking an action Post In Success with invalid arguments");
		try {
			act.invoke(hash);
			fail("SERVER: "
					+ UPnPConstants.ACT_PIS
					+ " Invoke was successful: The UPnP Driver doesn't check action arguments for validity");
		}
		catch (Exception exc) {
			System.out.println("Exception is thrown OK");
		}
		hash = new Hashtable<>(9, 1f);
		hash.put(UPnPConstants.N_IN_INT, Integer.valueOf(UPnPConstants.V_IN_INT));
		hash.put(UPnPConstants.N_IN_UI4, Long.valueOf(UPnPConstants.V_IN_UI4));
		hash.put(UPnPConstants.N_IN_FLOAT, fl);
		hash.put(UPnPConstants.N_IN_NUMBER, duble);
		hash.put(UPnPConstants.N_IN_CHAR, Character.valueOf(UPnPConstants.V_IN_CHAR
				.charAt(0)));
		hash.put(UPnPConstants.N_IN_STRING, UPnPConstants.V_IN_STRING);
		hash.put(UPnPConstants.N_IN_BOOLEAN, Boolean.valueOf(
				UPnPConstants.V_IN_BOOLEAN));
		hash.put(UPnPConstants.N_IN_HEX, UPnPConstants.V_IN_HEX.getBytes());
		System.out.println("Invoking an action Post In Success");
		try {
			Dictionary<String,Object> dict = act.invoke(hash);
			if (dict != null) {
				String res = (String) dict.get(UPnPConstants.N_OUT_OUT);
				assertThat(res).as("SERVER: " + UPnPConstants.ACT_PIS
						+ " Invoke Failed: " + res).isEqualTo(UPnPConstants.V_OUT_OUT_OK);
			}
		}
		catch (Exception exc) {
			fail("SERVER: " + UPnPConstants.ACT_PIS
					+ " Invoke Failed: Threre was an exception", exc);
		}
		hash = new Hashtable<>(9, 1f);
		hash.put(UPnPConstants.N_IN_INT, Integer.valueOf(UPnPConstants.V_IN_INT));
		hash.put(UPnPConstants.N_IN_UI4, Long.valueOf(UPnPConstants.V_IN_UI4));
		hash.put(UPnPConstants.N_IN_FLOAT, fl);
		hash.put(UPnPConstants.N_IN_NUMBER, duble);
		hash.put(UPnPConstants.N_IN_CHAR, Character.valueOf(UPnPConstants.V_IN_CHAR
				.charAt(0)));
		hash.put(UPnPConstants.N_IN_STRING, UPnPConstants.V_IN_STRING);
		hash.put(UPnPConstants.N_IN_BOOLEAN, Boolean.valueOf(
				UPnPConstants.V_IN_BOOLEAN));
		hash.put(UPnPConstants.N_IN_HEX, UPnPConstants.V_IN_HEX.getBytes());
		act = cs.getAction(UPnPConstants.ACT_MPIS);
		System.out.println("Invoking an action MPost In Success");
		try {
			Dictionary<String,Object> dict = act.invoke(hash);
			if (dict != null) {
				String res = (String) dict.get(UPnPConstants.N_OUT_OUT);
				assertThat(res).as("SERVER: " + UPnPConstants.ACT_MPIS
						+ " Invoking Failed: " + res).isEqualTo(UPnPConstants.V_OUT_OUT_OK);
			}
		}
		catch (Exception exc) {
			fail("Unexpected Exception", exc);
		}
	}

	@Test
	public void testEvent() throws Exception {
		UPnPDevice dev = listener.getUPnPDevice();
		assertThat(dev).as("UPnPDevice is NULL").isNotNull();
		final String udn = (String) dev.getDescriptions(null).get(
				UPnPDevice.UDN);
		Hashtable<String,Object> hash = new Hashtable<>();
		System.out.println("Device gets service " + UPnPConstants.SEV_ID);
		UPnPService es = dev.getService(UPnPConstants.SEV_ID);
		final String currentServ = es.getId();
		Filter filter = context.createFilter(
				"(&(" + UPnPDevice.ID + "=" + udn + ")(" + UPnPService.ID + "="
						+ UPnPConstants.SEV_ID + "))");
		hash.put(UPnPEventListener.UPNP_FILTER, filter);
		hash.put("upnptest", "true");
		System.out.println("Register UPnPEventListener to listen for events");
		final List<Dictionary<String,Object>> vector = new ArrayList<>();
		UPnPEventListener el = new UPnPEventListener() {
			private int	ender;
			{
				synchronized (this) {
					ender = 10;
				}
			}

			public void notifyUPnPEvent(String did, String sid,
					Dictionary<String,Object> vals) {
				System.out.println("SERVER: Received Event for UDN: " + did + " & SID: " + sid
						+ ".\r\nDictionary: " + vals);
				if (!did.equals(udn) || !sid.equals(currentServ)) {
					System.out.println("SERVER: Received Event for UDN: " + did + " & SID: "
							+ sid + ".\r\nExpected UDN: " + udn + " SID: "
							+ currentServ);
					return;
				}
				synchronized (this) {
					vector.add(vals);
					ender--;
					if (ender == 0) {
						notify();
					}
				}
			}
		};
		ServiceRegistration<UPnPEventListener> sr = context
				.registerService(UPnPEventListener.class, el, hash);
		try {
			synchronized (el) {
				for (int tries = 0; (tries < 10) && (vector.size() < 10); tries++) {
					System.out.println("Waiting for events: try=" + tries + "; size="
							+ vector.size());
					el.wait(10000 * OSGiTestCaseProperties.getScaling());
				}
				assertThat(vector).as("SERVER: Wrong number of events").hasSize(10);
				int evSize = vector.size();
				for (int i = 0; i < evSize; i++) {
					Dictionary<String,Object> dict = vector.get(i);
					testEvent(dict, dict.size());
				}
			}
		}
		finally {
			sr.unregister();
		}
	}

	@Test
	public void testExport() throws Exception {
		System.out.println("Creating new device and making it UPnP device");
		UPnPDevice dev = listener.getUPnPDevice();
		assertThat(dev).as("UPnPDevice is NULL").isNotNull();
		String udn = (String) dev.getDescriptions(null).get(UPnPDevice.UDN);
		UPnPExportedDevice ex_device = UPnPExportedDevice.newUPnPTestervice();
		ServiceRegistration<UPnPDevice> sr = context.registerService(
				UPnPDevice.class, ex_device,
				ex_device.getDescriptions(null));
		try {
			Thread.sleep(4000 * OSGiTestCaseProperties.getScaling());
		}
		catch (Exception er) {
			// ignored
		}
		try {
			System.out.println("Start export test");
			Hashtable<String,Object> tagHash = new Hashtable<>();
			Hashtable<String,Object> servsHash = new Hashtable<>();
			System.out.println("Prepare to start my Control Point");
			ControlPoint cp = new ControlPoint();
			cp.start();
			System.out.println("Create MSearch package and send it");
			DatagramPacket search = cp.createMSearch();
			cp.send(search);
			int count = 0;
			while (cp.received == null) {
				count++;
				Thread.sleep(50);
				if (count == 1000) {
					fail("No answer is received to an MSearch message");
				}
			}
			System.out.println("Parsing received Discovery Response xml");
			String URL_BASE = null;
			String toParse = cp.received;
			XMLParser parser = new XMLParser(toParse);
			XMLTag rootTag = parser.getRootXMLTag();
			if (!rootTag.hasOnlyTags()) {
				fail("Root tag has something else except tags");
			}
			Vector<Object> content = rootTag.getContent();
			int len = content.size();
			for (int i = 0; i < len; i++) {
				XMLTag tag = (XMLTag) content.elementAt(i);
				if (tag.getName().equalsIgnoreCase("device")) {
					Vector<Object> elem = tag.getContent();
					for (int j = 0; j < elem.size(); j++) {
						XMLTag tagDev = (XMLTag) elem.elementAt(j);
						Vector<Object> vv = tagDev.getContent();
						if (tagDev.getName().equalsIgnoreCase("serviceList")) {
							Vector<Object> servs = tagDev.getContent();
							for (int k = 0; k < servs.size(); k++) {
								XMLTag tagServ = (XMLTag) servs.elementAt(k);
								if (tagServ.getName().equalsIgnoreCase(
										"service")) {
									Vector<Object> sss = tagServ.getContent();
									for (int p = 0; p < sss.size(); p++) {
										XMLTag tagSSS = (XMLTag) sss
												.elementAt(p);
										Vector<Object> sps = tagSSS
												.getContent();
										if (tagSSS.hasOnlyText()
												&& sps.size() > 0) {
											servsHash.put(tagSSS.getName(), sps
													.elementAt(0));
										}
									}
								}
							}
						}
						if (tagDev.hasOnlyText() && vv.size() > 0) {
							tagHash.put(tagDev.getName(), vv.elementAt(0));
						}
					}
				}
				if (tag.hasOnlyText()) {
					URL_BASE = tag.getContent().elementAt(0).toString();
				}
			}
			testProps(udn, tagHash);
			System.out.println("Properties of the exported device are OK");
			String SERVICE_TYPE = servsHash.get("serviceType").toString();
			String SCPD_URL = servsHash.get("SCPDURL").toString();
			String CONTROL_URL = servsHash.get("controlURL").toString();

			String url = URL_BASE + SCPD_URL;
			System.out.println("Get SCPD xml from discovery message");
			String xml = cp.getXML(url);
			String action = null;
			Hashtable<String,Vector<String>> names = parseSCPD_XML(xml); // action
																			// names
			if (names.containsKey("testALL")) {
				action = "testALL";
			}
			String controlURL = null;
			if (CONTROL_URL.toLowerCase().startsWith("http")) {
				controlURL = CONTROL_URL;
			}
			else {
				controlURL = URL_BASE + CONTROL_URL;
			}
			URL contrlURL = new URL(controlURL);
			String host = contrlURL.getHost();
			int port = contrlURL.getPort();
			String[] args = changeType(names.get(action));
			System.out.println("Control Server is created and started");
			ControlServer cps = new ControlServer(host, port);
			String[] values = new String[] {UPnPConstants.V_OUT_STRING,
					UPnPConstants.V_OUT_BOOLEAN, UPnPConstants.V_OUT_NUMBER,
					UPnPConstants.V_OUT_INT, UPnPConstants.V_OUT_CHAR,
					UPnPConstants.V_OUT_FLOAT};
			System.out.println("Creating Post message and send it");
			byte[] post = cp.createPOST(CONTROL_URL, host, port, SERVICE_TYPE,
					action, args, values);
			cps.send(post);
			Thread th = new Thread(cps, "CPS");
			th.start();
			while (!cps.isFinished) {
				Thread.sleep(100);
			}
			cps.checkAns();
			eventExportTest();
			cp.interrupt();
			cps.finish();

		}
		finally {
			sr.unregister();
		}
	}

	@Test
	public void testRemovedExport() throws Exception {
		UPnPDevice upnpDevice = listener.getUPnPDevice();
		assertThat(upnpDevice).as("UPnP device is NULL!").isNotNull();
		UPnPService upnpService = upnpDevice.getService(UPnPConstants.SCONT_ID);
		assertThat(upnpService).as("UPnP service is missing: " + UPnPConstants.SCONT_ID).isNotNull();
		UPnPIcon[] upnpDeviceIcons = upnpDevice.getIcons(null);
		assertThat(upnpDeviceIcons).as("No UPnP device icons!").isNotNull();
		UPnPAction upnpAction = upnpService.getAction(UPnPConstants.ACT_PF);
		assertThat(upnpAction).as("UPnP action is missing: " + UPnPConstants.ACT_PF
				+ " for service: " + UPnPConstants.SCONT_ID).isNotNull();
		UPnPStateVariable upnpStateVariable = upnpAction
				.getStateVariable(UPnPConstants.N_IN_OUT);
		assertThat(upnpStateVariable).as("UPnP state variable is missing for argument: "
				+ UPnPConstants.N_IN_OUT).isNotNull();
		finalizeTestEnd();

		assertThat(upnpDevice.getDescriptions(null)).as("The UPnP device description is missing!").isNotNull();
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP device icons must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpDevice.getIcons(null));
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP device services must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpDevice.getServices());
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP device service must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpDevice.getService(UPnPConstants.SEV_ID));

		assertThat(upnpService.getId()).as("The UPnP service identifier is missing!").isNotNull();
		assertThat(upnpService.getType()).as("The UPnP service type is missing!").isNotNull();
		assertThat(upnpService.getVersion()).as("The UPnP service version is missing!").isNotNull();
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP service actions must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpService.getActions());
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP service actions must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpService.getAction(UPnPConstants.ACT_PF));
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP state variables must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpService.getStateVariables());

		assertThat(upnpAction.getName()).as("The UPnP action name is missing!").isNotNull();
		assertThat(upnpAction.getInputArgumentNames()).as("The UPnP action input argument names are missing!").isNotNull();
		assertThat(upnpAction.getOutputArgumentNames()).as("The UPnP action output argument names are missing!").isNotNull();
		assertThat(upnpAction.getReturnArgumentName()).as("The UPnP action return argument name must be NULL!").isNull();
		assertThatExceptionOfType(IllegalStateException.class).as("The UPnP state variables must not be accessible for a disconnected device!")
				.isThrownBy(() -> upnpAction.getStateVariable(UPnPConstants.N_IN_OUT));

		assertThat(upnpStateVariable.getAllowedValues()).as("The UPnP state variable allowed values must be accessible!").isNull();
		assertThat(upnpStateVariable.getDefaultValue()).as("The UPnP state variable default value must be accessible!").isNull();
		assertThat(upnpStateVariable.getJavaDataType()).as("The UPnP state variable java type must be accessible!").isNotNull();
		assertThat(upnpStateVariable.getMaximum()).as("The UPnP state variable maximum must be accessible!").isNull();
		assertThat(upnpStateVariable.getMinimum()).as("The UPnP state variable minimum must be accessible!").isNull();
		assertThat(upnpStateVariable.getName()).as("The UPnP state variable name is not correct!").isEqualTo(UPnPConstants.N_OUT);
		assertThat(upnpStateVariable.getStep()).as("The UPnP state variable step must be accessible!").isNull();
		assertThat(upnpStateVariable.sendsEvents()).as("The UPnP state variable event notification must be accessible!").isFalse();

		prepareTestStart();
	}

	private byte[] readFully(InputStream is) throws IOException {
		byte[] buff = new byte[1024];
		int size = is.read(buff);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (size >= 0) {
			baos.write(buff, 0, size);
			size = is.read(buff);
		}
		return baos.toByteArray();
	}

	private boolean checkArrays(byte[] arr, byte[] to) {
		if (arr.length != to.length) {
			return false;
		}
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != to[i]) {
				return false;
			}
		}
		return true;
	}

	private void checkRet(Dictionary<String,Object> dict) {
		assertThat(dict).isNotNull();
		String st = new String((byte[]) dict.get(UPnPConstants.N_OUT_HEX));
		assertThat(st).as("SERVER: " + UPnPConstants.N_OUT_HEX
				+ " value is not ok: " + st).isEqualTo(UPnPConstants.V_OUT_HEX);
		Integer inint = (Integer) dict.get(UPnPConstants.N_OUT_INT);
		assertThat(inint).as("SERVER: " + UPnPConstants.N_OUT_INT
				+ " value is not ok: " + inint).isEqualTo(Integer.valueOf(
				UPnPConstants.V_OUT_INT));
		Long lo = (Long) dict.get(UPnPConstants.N_OUT_UI4);
		assertThat(lo).as("SERVER: " + UPnPConstants.N_OUT_UI4
				+ " value is not ok: " + lo).isEqualTo(Long.valueOf(UPnPConstants.V_OUT_UI4));
		Float fl = (Float) dict.get(UPnPConstants.N_OUT_FLOAT);
		assertThat(fl).as("SERVER: " + UPnPConstants.N_OUT_FLOAT
				+ " value is not ok: " + fl).isEqualTo(Float.valueOf(
				UPnPConstants.V_OUT_FLOAT));
		Double dl = (Double) dict.get(UPnPConstants.N_OUT_NUMBER);
		assertThat(dl).as("SERVER: " + UPnPConstants.N_OUT_NUMBER
				+ " value is not ok: " + dl).isEqualTo(Double.valueOf(
				UPnPConstants.V_OUT_NUMBER));
		Character ch = (Character) dict.get(UPnPConstants.N_OUT_CHAR);
		assertThat(ch).as("SERVER: " + UPnPConstants.N_OUT_CHAR
				+ " value is not ok: " + ch).isEqualTo(Character.valueOf(
				UPnPConstants.V_OUT_CHAR.charAt(0)));
		String str = (String) dict.get(UPnPConstants.N_OUT_STRING);
		assertThat(str).as("SERVER: " + UPnPConstants.N_OUT_STRING
				+ " value is not ok: " + str).isEqualTo(UPnPConstants.V_OUT_STRING);
		Boolean bool = (Boolean) dict.get(UPnPConstants.N_OUT_BOOLEAN);
		assertThat(bool).as("SERVER: " + UPnPConstants.N_OUT_BOOLEAN
				+ " value is not ok: " + bool).isEqualTo(Boolean.valueOf(
				UPnPConstants.V_OUT_BOOLEAN));
	}

	private void testEvent(Dictionary<String,Object> dict, int si) {
		assertThat(dict).isNotNull();
		switch (si) {
			case 9 : {
				String str1 = (String) dict.get(UPnPConstants.N_OUT);
				assertThat(str1).as("SERVER: " + UPnPConstants.N_OUT + " " + si
						+ " value is not ok: " + str1).isEqualTo(UPnPConstants.V_OUT);
			}
			case 8 : {
				String st = new String((byte[]) dict.get(UPnPConstants.N_HEX));
				assertThat(st).as("SERVER: " + UPnPConstants.N_HEX + " " + si
						+ " value is not ok: " + st).isEqualTo(UPnPConstants.V_HEX);
			}
			case 7 : {
				Boolean bool = (Boolean) dict.get(UPnPConstants.N_BOOLEAN);
				assertThat(bool).as("SERVER: " + UPnPConstants.N_BOOLEAN + " " + si
						+ " value is not ok: " + bool).isEqualTo(Boolean.valueOf(
						UPnPConstants.V_BOOLEAN));
			}
			case 6 : {
				String str = (String) dict.get(UPnPConstants.N_STRING);
				assertThat(str).as("SERVER: " + UPnPConstants.N_STRING + " " + si
						+ " value is not ok: " + str).isEqualTo(UPnPConstants.V_STRING);
			}
			case 5 : {
				Character ch = (Character) dict.get(UPnPConstants.N_CHAR);
				assertThat(ch).as("SERVER: " + UPnPConstants.N_CHAR + " " + si
						+ " value is not ok: " + ch).isEqualTo(Character.valueOf(
						UPnPConstants.V_CHAR.charAt(0)));
			}
			case 4 : {
				Float fl = (Float) dict.get(UPnPConstants.N_FLOAT);
				assertThat(fl).as("SERVER: " + UPnPConstants.N_FLOAT + " " + si
						+ " value is not ok: " + fl).isEqualTo(Float.valueOf(
						UPnPConstants.V_FLOAT));
			}
			case 3 : {
				Double dl = (Double) dict.get(UPnPConstants.N_NUMBER);
				assertThat(dl).as("SERVER: " + UPnPConstants.N_NUMBER + " " + si
						+ " value is not ok: " + dl).isEqualTo(Double.valueOf(
						UPnPConstants.V_NUMBER));
			}
			case 2 : {
				Long lo = (Long) dict.get(UPnPConstants.N_UI4);
				assertThat(lo).as("SERVER: " + UPnPConstants.N_UI4 + " " + si
						+ " value is not ok: " + lo).isEqualTo(Long.valueOf(
						UPnPConstants.V_UI4));
			}
			case 1 : {
				Integer in = (Integer) dict.get(UPnPConstants.N_INT);
				assertThat(in).as("SERVER: " + UPnPConstants.N_INT + " " + si
						+ " value is not ok: " + in).isEqualTo(Integer.valueOf(
						UPnPConstants.V_INT));
			}
		}
	}

	private void testProps(String udn, Dictionary<String,Object> dic) {
		assertThat(dic).isNotNull();
		String test = dic.get("friendlyName").toString();
		System.out.println("Exported device FRIENDLY_NAME: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " FRINDLY_NAME not matched").isEqualTo(UPnPConstants.E_FN);
		test = dic.get("manufacturer").toString();
		System.out.println("Exported device MANUFACTURER: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " MANUFACTURER not matched").isEqualTo(UPnPConstants.E_MANU);
		test = dic.get("manufacturerURL").toString();
		System.out.println("Exported device MANUFACTURER_URL: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " MANUFACTURER_URL not matched").isEqualTo(UPnPConstants.E_MANU_URL);
		test = dic.get("modelName").toString();
		System.out.println("Exported device MODEL_NAME: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " MODEL_NAME not matched").isEqualTo(UPnPConstants.E_MOD_NAME);
		test = dic.get("modelNumber").toString();
		System.out.println("Exported device MODEL_NUMBER: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " MODEL_NUMBER not matched").isEqualTo(UPnPConstants.E_MOD_NUMB);
		test = dic.get("modelURL").toString();
		System.out.println("Exported device MODEL_URL: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " MODEL_URL not matched").isEqualTo(UPnPConstants.E_MOD_URL);
		test = dic.get("serialNumber").toString();
		System.out.println("Exported device SERIAL_NUMBER: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD
				+ " SERIAL_NUMBER not matched").isEqualTo(UPnPConstants.E_SN);
		test = dic.get("UPC").toString();
		System.out.println("Exported device UPC: " + test);
		assertThat(test).as("EXPORTED: " + udn + UPnPConstants.DD + " UPC not matched").isEqualTo(UPnPConstants.E_UPC);
	}

	private Hashtable<String,Vector<String>> parseSCPD_XML(String str)
			throws Exception {
		Hashtable<String,Vector<String>> hash = new Hashtable<>();
		// System.out.println("SCPD: " + str);
		XMLParser parser = new XMLParser(str);
		XMLTag rootTag = parser.getRootXMLTag();
		if (!rootTag.hasOnlyTags()) {
			fail("SCPD XML:Root tag has something else except tags");
		}
		Vector<Object> content = rootTag.getContent();
		int len = content.size();
		for (int i = 0; i < len; i++) {
			XMLTag tag = (XMLTag) content.elementAt(i);
			if (tag.getName().equalsIgnoreCase("actionList")) {
				Vector<Object> acts = tag.getContent();
				for (int j = 0; j < acts.size(); j++) {
					XMLTag act = (XMLTag) acts.elementAt(j);
					if (act.getName().equalsIgnoreCase("action")) {
						Vector<Object> names = act.getContent();
						Vector<String> argsNames = null;
						for (int k = 0; k < names.size(); k++) {
							XMLTag name = (XMLTag) names.elementAt(k);
							if (name.getName().equalsIgnoreCase("name")) {
								argsNames = new Vector<>();
								hash.put(name.getContent().elementAt(0)
										.toString(), argsNames);
							}
							else
								if (name.getName().equalsIgnoreCase(
										"argumentList")) {
									Vector<Object> args = name.getContent();
									for (int p = 0; p < args.size(); p++) {
										XMLTag arg = (XMLTag) args.elementAt(p);
										if (arg.getName().equalsIgnoreCase(
												"argument")) {
											Vector<Object> argsName = arg
													.getContent();
											String argname = null;
											for (int y = 0; y < argsName.size(); y++) {
												XMLTag xxx = (XMLTag) argsName
														.elementAt(y);
												if (xxx.hasOnlyText()
														&& xxx
																.getName()
																.equalsIgnoreCase(
																		"name")) {
													argname = xxx.getContent()
															.elementAt(0)
															.toString();
												}
												else
													if (xxx.hasOnlyText()
															&& xxx
																	.getName()
																	.equalsIgnoreCase(
																			"direction")) {
														String dir = xxx
																.getContent()
																.elementAt(0)
																.toString();
														if (dir.equals("in")) {
															argsNames
																	.addElement(argname);
															argname = null;
														}
													}
											}
										}
									}
								}
						}
					}
				}
			}
		}
		return hash;
	}

	private String[] changeType(Vector<String> args) {
		String[] names = new String[6];
		for (int i = 0; i < args.size(); i++) {
			names[i] = args.elementAt(i).toString();
		}
		return names;
	}

	private void prepareTestStart() throws Exception {
		http = getService(HttpService.class);
		System.out.println("Register Service Listener to listen for service changes");
		listener = new ServicesListener(context, desiredCount);
		listener.open();
		System.out.println("Start the UPnP Test Starter");
		start = new TestStarter(http);
		listener.waitFor(OSGiTestCaseProperties.getTimeout()
				* OSGiTestCaseProperties.getScaling());
		if (listener.size() < desiredCount) {
			listener.close();
			start.stop(http);
			ungetService(http);
			fail("timed out waiting for " + desiredCount + " UPnP devices");
		}
	}

	private void finalizeTestEnd() throws Exception {
		listener.close();
		start.stop(http);
		ungetService(http);
	}

	private void eventExportTest() {
		// try {
		// log("Starting event test of Export");
		// String event = null;
		// if (EVENT_SUB_URL.toLowerCase().startsWith("http")) {
		// event = EVENT_SUB_URL;
		// }
		// else {
		// event = URL_BASE + EVENT_SUB_URL;
		// }
		// URL eventURL = new URL(event);
		// String evHost = eventURL.getHost();
		// int evPort = eventURL.getPort();
		//
		// log("Create and start Eventing Server");
		// EventServer evs = new EventServer(evHost, evPort, bc, control);
		// int pr = evPort + 1;
		// String callback = "http://" + evHost + ":" + pr + "/";
		// log("Create subscribe message and send it");
		//
		// // pkr; added to have the same path URL
		// String match = (udn + SERVICE_ID).replace(':', '-');
		//
		// byte[] subscr = cp.createSUBSCRIBE(/*EVENT_SUB_URL*/ match, evHost,
		// evPort,
		// callback, 60);
		// Thread th = new Thread(evs, "EVS");
		// th.start();
		// evs.send(subscr);
		//
		// log("Create table with events");
		// Hashtable events = new Hashtable(6);
		// events.put(UPnPConstants.N_IN_STRING, UPnPConstants.E_STRING);
		// events.put(UPnPConstants.N_IN_BOOLEAN, UPnPConstants.E_BOOLEAN);
		// events.put(UPnPConstants.N_OUT_NUMBER, UPnPConstants.E_NUMBER);
		// events.put(UPnPConstants.N_OUT_INT, UPnPConstants.E_INT);
		// events.put(UPnPConstants.N_OUT_CHAR, UPnPConstants.E_CHAR);
		// events.put(UPnPConstants.N_OUT_FLOAT, UPnPConstants.E_FLOAT);
		// int count = 0;
		// while (evs.getListener() == null) {
		// count++;
		// Thread.sleep(1000);
		// if (count == 1000) {
		// log("No UPnPEventListener is found");
		// break;
		// }
		// }
		// log("Starting ServerSocket to listen for Notify msg");
		// SSNotify not = new SSNotify(pr, control);
		// not.start();
		// log("Notify UPnPEventListener service");
		// evs.getListener().notifyUPnPEvent(udn, SERVICE_ID, events);
		// evs.finish();
		// while (!not.isFinished) {
		// Thread.sleep(100);
		// if (cps.isError) {
		// break;
		// }
		// }
		// not.finish();
		// }
		// catch (Exception er) {
		// er.printStackTrace();
		// }
	}

}
