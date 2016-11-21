/*
 * #%L
 * ELK Reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Department of Computer Science, University of Oxford
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.semanticweb.elk.reasoner;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.TestInput;
import org.semanticweb.elk.testing.TestManifest;
import org.semanticweb.elk.testing.TestOutput;

/**
 * Base class for reasoning tests that are run with {@link PolySuite}.
 * Subclasses of this class specify order of steps that are performed during a
 * tests, the test delegate passed to the constructor implements these steps,
 * and the test manifest passed to the constructor describes input and output of
 * the test.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * @author Peter Skocovsky
 *
 * @param <I>
 *            The type of test input.
 * @param <AO>
 *            The type of actual test output.
 * @param <TM>
 *            The type of test manifest.
 * @param <TD>
 *            The type of test delegate.
 */
@RunWith(PolySuite.class)
public abstract class BaseReasoningCorrectnessTest<I extends TestInput, AO extends TestOutput, TM extends TestManifest<I>, TD extends ReasoningTestDelegate<AO>> {

	private final TM manifest_;
	private final TD delegate_;

	public BaseReasoningCorrectnessTest(final TM testManifest,
			final TD testDelegate) {
		this.manifest_ = testManifest;
		this.delegate_ = testDelegate;
	}

	public TM getManifest() {
		return manifest_;
	}

	public TD getDelegate() {
		return delegate_;
	}

	// Declare the size
	final static int megaBytes = 1024 * 1024;

	@Before
	public void before() throws Exception {
		// print memory usage
		Runtime runtime = Runtime.getRuntime();

		System.out.println("Memory (MB) Used/Total/Max: "
				+ (runtime.totalMemory() - runtime.freeMemory()) / megaBytes
				+ "/" + runtime.totalMemory() / megaBytes + "/"
				+ runtime.maxMemory() / megaBytes);
		org.junit.Assume.assumeFalse(ignore(manifest_.getInput()));
		delegate_.before();
	}

	@After
	public void after() {
		if (ignore(manifest_.getInput())) {
			return;
		}
		// else
		delegate_.after();
	}

	@SuppressWarnings("static-method")
	protected boolean ignore(TestInput input) {
		return false;
	}

}