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

import static org.assertj.core.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;

/**
 * The service helpers these tests used from DefaultTestBundleControl,
 * unchanged: a fetched service remembers its reference, so it can be released
 * by the service object.
 */
abstract class UPnPTestSupport {
	@InjectBundleContext
	protected BundleContext								context;

	private final Map<Object,ServiceReference< ? >>	fetchedServices	= new HashMap<>();

	public <S> S getService(Class<S> clazz) {
		try {
			return getService(clazz, null);
		} catch (InvalidSyntaxException e) {
			/* null can not give an exception of this type */
		}
		return null;
	}

	public <S> S getService(Class<S> clazz, String filter)
			throws InvalidSyntaxException {
		@SuppressWarnings("unchecked")
		ServiceReference<S>[] refs = (ServiceReference<S>[]) context
				.getServiceReferences(clazz.getName(), filter);
		if (refs == null) {
			fail("Can't get service reference for " + clazz.getName() + filter);
		}
		ServiceReference<S> chosenRef = pickServiceReference(refs);
		S service = context.getService(chosenRef);
		if (service == null) {
			fail("Can't get service for " + clazz.getName() + filter);
		}
		/* Save the service and its reference */
		synchronized (fetchedServices) {
			fetchedServices.put(service, chosenRef);
		}
		return service;
	}

	public void ungetService(Object service) {
		ServiceReference< ? > ref;
		synchronized (fetchedServices) {
			ref = fetchedServices.remove(service);
		}
		if (ref == null) {
			fail("trying to unget a service which is not currently bound");
		}
		context.ungetService(ref);
	}

	/* highest ranking, then lowest service id: the ServiceReference order */
	private static <S> ServiceReference<S> pickServiceReference(
			ServiceReference<S>[] refs) {
		ServiceReference<S> highest = refs[0];
		for (int i = 1; i < refs.length; i++) {
			if (refs[i].compareTo(highest) > 0) {
				highest = refs[i];
			}
		}
		return highest;
	}
}
