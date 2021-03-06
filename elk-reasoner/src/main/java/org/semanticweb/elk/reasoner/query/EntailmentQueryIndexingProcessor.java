/*-
 * #%L
 * ELK Reasoner Core
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
package org.semanticweb.elk.reasoner.query;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.interfaces.ElkObject;
import org.semanticweb.elk.owl.printers.OwlFunctionalStylePrinter;
import org.semanticweb.elk.owl.visitors.DummyElkAxiomVisitor;
import org.semanticweb.elk.reasoner.entailments.model.Entailment;
import org.semanticweb.elk.reasoner.indexing.conversion.ElkIndexingUnsupportedException;
import org.semanticweb.elk.reasoner.indexing.model.ModifiableOntologyIndex;
import org.semanticweb.elk.util.logging.LogLevel;
import org.semanticweb.elk.util.logging.LoggerWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntailmentQueryIndexingProcessor extends
		DummyElkAxiomVisitor<IndexedEntailmentQuery<? extends Entailment>> {

	private static final Logger LOGGER_ = LoggerFactory
			.getLogger(EntailmentQueryIndexingProcessor.class);

	public static final String ADDITION = "addition", REMOVAL = "removal";

	private final EntailmentQueryConverter converter_;

	private final String type_;

	public EntailmentQueryIndexingProcessor(final ElkObject.Factory elkFactory,
			final ModifiableOntologyIndex index, final String type) {
		if (!ADDITION.equals(type) && !REMOVAL.equals(type)) {
			throw new IllegalArgumentException("type must be one of \""
					+ ADDITION + "\" or \"" + REMOVAL + "\"!");
		}
		this.type_ = type;
		this.converter_ = new EntailmentQueryConverter(elkFactory, index,
				ADDITION.equals(type) ? 1 : -1);
	}

	@Override
	protected IndexedEntailmentQuery<? extends Entailment> defaultVisit(
			final ElkAxiom axiom) {
		if (LOGGER_.isTraceEnabled()) {
			LOGGER_.trace("$$ indexing {} for {}",
					OwlFunctionalStylePrinter.toString(axiom), type_);
		}
		try {
			return axiom.accept(converter_);
		} catch (final ElkIndexingUnsupportedException e) {
			// TODO: messages for user !!!
			LoggerWrap.log(LOGGER_, LogLevel.WARN,
					"reasoner.indexing.queryIgnored",
					e.getMessage() + " Query results may be incomplete: "
							+ OwlFunctionalStylePrinter.toString(axiom));
			return null;
		}
	}

}
