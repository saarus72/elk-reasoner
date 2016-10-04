/*
 * #%L
 * ELK OWL API Binding
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2016 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.owlapi.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.semanticweb.elk.io.IOUtils;
import org.semanticweb.elk.owlapi.OwlApiReasoningTestDelegate;
import org.semanticweb.elk.reasoner.query.BaseClassExpressionQueryTest;
import org.semanticweb.elk.reasoner.query.ClassExpressionQueryTestManifest;
import org.semanticweb.elk.reasoner.query.ClassQueryTestInput;
import org.semanticweb.elk.reasoner.query.RelatedEntitiesTestOutput;
import org.semanticweb.elk.testing.ConfigurationUtils;
import org.semanticweb.elk.testing.ConfigurationUtils.TestManifestCreator;
import org.semanticweb.elk.testing.PolySuite;
import org.semanticweb.elk.testing.PolySuite.Config;
import org.semanticweb.elk.testing.PolySuite.Configuration;
import org.semanticweb.elk.testing.TestInput;
import org.semanticweb.elk.testing.TestManifestWithOutput;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.NodeSet;

@RunWith(PolySuite.class)
public class OwlApiClassExpressionInstancesQueryTest extends
		BaseClassExpressionQueryTest<OWLClassExpression, RelatedEntitiesTestOutput<OWLNamedIndividual>> {

	// @formatter:off
	static final String[] IGNORE_LIST = {
			"Inconsistent.owl",// Throwing InconsistentOntologyException
			"InconsistentInstances.owl",// Throwing InconsistentOntologyException
		};
	// @formatter:on

	static {
		Arrays.sort(IGNORE_LIST);
	}

	@Override
	protected boolean ignore(final TestInput input) {
		return Arrays.binarySearch(IGNORE_LIST, input.getName()) >= 0;
	}

	public OwlApiClassExpressionInstancesQueryTest(
			final TestManifestWithOutput<ClassQueryTestInput<OWLClassExpression>, RelatedEntitiesTestOutput<OWLNamedIndividual>, RelatedEntitiesTestOutput<OWLNamedIndividual>> manifest) {
		super(manifest,
				new OwlApiReasoningTestDelegate<RelatedEntitiesTestOutput<OWLNamedIndividual>>(
						manifest) {

					@Override
					public RelatedEntitiesTestOutput<OWLNamedIndividual> getActualOutput()
							throws Exception {
						final NodeSet<OWLNamedIndividual> subNodes = reasoner_
								.getInstances(
										manifest.getInput().getClassQuery(),
										true);
						return new OwlApiRelatedEntitiesTestOutput<OWLNamedIndividual>(
								subNodes);
					}

				});
	}

	@Config
	public static Configuration getConfig()
			throws IOException, URISyntaxException {

		return ConfigurationUtils.loadFileBasedTestConfiguration(
				INPUT_DATA_LOCATION, BaseClassExpressionQueryTest.class, "owl",
				"expected",
				new TestManifestCreator<ClassQueryTestInput<OWLClassExpression>, RelatedEntitiesTestOutput<OWLNamedIndividual>, RelatedEntitiesTestOutput<OWLNamedIndividual>>() {

					@Override
					public TestManifestWithOutput<ClassQueryTestInput<OWLClassExpression>, RelatedEntitiesTestOutput<OWLNamedIndividual>, RelatedEntitiesTestOutput<OWLNamedIndividual>> create(
							final URL input, final URL output)
							throws IOException {

						InputStream outputIS = null;
						try {
							outputIS = output.openStream();
							final ExpectedTestOutputLoader expected = ExpectedTestOutputLoader
									.load(outputIS);

							return new ClassExpressionQueryTestManifest<OWLClassExpression, RelatedEntitiesTestOutput<OWLNamedIndividual>>(
									input, expected.getQueryClass(),
									expected.getInstancesTestOutput());

						} finally {
							IOUtils.closeQuietly(outputIS);
						}

					}

				});

	}

	@Test
	@Ignore
	@Override
	public void testWithInterruptions() throws Exception {
		super.testWithInterruptions();
	}

}