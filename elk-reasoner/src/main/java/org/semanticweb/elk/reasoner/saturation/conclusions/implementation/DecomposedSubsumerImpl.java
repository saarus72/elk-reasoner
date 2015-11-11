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
package org.semanticweb.elk.reasoner.saturation.conclusions.implementation;

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedContextRoot;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.DecomposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.interfaces.Subsumer;

/**
 * An implementation of {@link DecomposedSubsumer}.
 * 
 * @author Frantisek Simancik
 * @author "Yevgeny Kazakov"
 * 
 */
public class DecomposedSubsumerImpl extends
		AbstractSubsumer<IndexedClassExpression> implements DecomposedSubsumer {

	protected DecomposedSubsumerImpl(IndexedContextRoot root,
			IndexedClassExpression subsumer) {
		super(root, subsumer);
	}

	@Override
	public <I, O> O accept(Subsumer.Visitor<I, O> visitor, I input) {
		return visitor.visit(this, input);
	}

	@Override
	public <I, O> O accept(DecomposedSubsumer.Visitor<I, O> visitor, I input) {
		return visitor.visit(this, input);
	}

}