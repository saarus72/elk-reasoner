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
package org.semanticweb.elk.reasoner.saturation.conclusions;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.semanticweb.elk.reasoner.saturation.rules.ConclusionProducer;
import org.semanticweb.elk.reasoner.saturation.rules.RuleVisitor;

/**
 * A {@link Subsumer} created using composition rules.
 * 
 * @author Frantisek Simancik
 * @author "Yevgeny Kazakov"
 * 
 */
public class ComposedSubsumer extends Subsumer {

	public ComposedSubsumer(IndexedClassExpression subsumer) {
		super(subsumer);
	}

	@Override
	public <R> R accept(ConclusionVisitor<R> visitor, Context context) {
		return visitor.visit(this, context);
	}

	@Override
	public void applyNonRedundantRules(RuleVisitor ruleAppVisitor,
			Context context, ConclusionProducer producer) {
		applyCompositionRules(ruleAppVisitor, context, producer);

	}

	@Override
	public void applyRedundantRules(RuleVisitor ruleAppVisitor,
			Context context, ConclusionProducer producer) {
		// if subsumer was composed, it is not necessary to decompose it
		applyDecompositionRules(ruleAppVisitor, context, producer);
	}

	@Override
	public String toString() {
		return "Composed" + super.toString();
	}
}