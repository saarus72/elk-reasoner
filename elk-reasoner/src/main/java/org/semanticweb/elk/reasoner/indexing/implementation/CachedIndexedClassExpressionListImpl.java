/*
 * #%L
 * ELK Reasoner
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 Department of Computer Science, University of Oxford
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
package org.semanticweb.elk.reasoner.indexing.implementation;

import java.util.List;

import org.semanticweb.elk.reasoner.indexing.caching.CachedIndexedClassExpressionList;
import org.semanticweb.elk.reasoner.indexing.caching.CachedIndexedObjectFilter;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpressionList;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableIndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.modifiable.ModifiableOntologyIndex;
import org.semanticweb.elk.reasoner.indexing.modifiable.OccurrenceIncrement;
import org.semanticweb.elk.reasoner.indexing.visitors.IndexedObjectVisitor;

/**
 * Implements {@link CachedIndexedClassExpressionList}
 * 
 * @author "Yevgeny Kazakov"
 */
class CachedIndexedClassExpressionListImpl
		extends
			CachedIndexedObjectImpl<CachedIndexedClassExpressionList, CachedIndexedClassExpressionList>
		implements
			CachedIndexedClassExpressionList {

	/**
	 * The elements of the list
	 */
	private final List<? extends ModifiableIndexedClassExpression> elements_;
	
	/**
	 * Counts how often this {@link IndexedClassExpressionList} occurs in the
	 * ontology.
	 */
	int totalOccurrenceNo_ = 0;

	CachedIndexedClassExpressionListImpl(
			List<? extends ModifiableIndexedClassExpression> members) {
		super(CachedIndexedClassExpressionList.Helper.structuralHashCode(members));
		this.elements_ = members;
	}
	
	@Override
	public final boolean occurs() {
		return totalOccurrenceNo_ > 0;
	}

	@Override
	public final List<? extends ModifiableIndexedClassExpression> getElements() {
		return elements_;
	}

	@Override
	public final CachedIndexedClassExpressionList structuralEquals(Object other) {
		return CachedIndexedClassExpressionList.Helper.structuralEquals(this,
				other);
	}

	@Override
	public boolean updateOccurrenceNumbers(ModifiableOntologyIndex index,
			OccurrenceIncrement increment) {
		
		totalOccurrenceNo_ += increment.totalIncrement;
		
		return true;
	}
		
	@Override
	public final String toStringStructural() {
		return elements_.toString();
	}

	@Override
	public CachedIndexedClassExpressionList accept(
			CachedIndexedObjectFilter filter) {
		return filter.filter(this);
	}
	
	@Override
	public <O> O accept(IndexedObjectVisitor<O> visitor) {
		return visitor.visit(this);
	}


}