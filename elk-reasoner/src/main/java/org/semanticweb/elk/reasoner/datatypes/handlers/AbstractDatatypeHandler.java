package org.semanticweb.elk.reasoner.datatypes.handlers;
/*
 * #%L
 * ELK Reasoner
 * $Id:$
 * $HeadURL:$
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

import org.semanticweb.elk.owl.interfaces.ElkDataRange;
import org.semanticweb.elk.owl.interfaces.ElkDatatype;
import org.semanticweb.elk.owl.interfaces.ElkDatatypeRestriction;
import org.semanticweb.elk.owl.interfaces.literals.ElkLiteral;
import org.semanticweb.elk.owl.interfaces.literals.ElkRealLiteral;
import org.semanticweb.elk.owl.printers.OwlFunctionalStylePrinter;
import org.semanticweb.elk.owl.visitors.BaseElkDataRangeVisitor;
import org.semanticweb.elk.owl.visitors.BaseElkDatatypeVisitor;
import org.semanticweb.elk.owl.visitors.BaseElkLiteralVisitor;
import org.semanticweb.elk.owl.visitors.ElkDataRangeVisitor;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.EmptyValueSpace;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.PointValue;
import org.semanticweb.elk.reasoner.datatypes.valuespaces.ValueSpace;
import org.semanticweb.elk.reasoner.indexing.hierarchy.ElkIndexingException;
import org.semanticweb.elk.reasoner.indexing.hierarchy.ElkUnexpectedIndexingException;

/**
 * @author Pospishnyi Olexandr
 * @author Pavel Klinov
 */
abstract class AbstractDatatypeHandler implements DatatypeHandler {

	protected abstract ElkDataRangeVisitor<ValueSpace<?>> getDataRangeConverter() throws ElkIndexingException;

	@Override
	public ValueSpace<?> createValueSpace(ElkDataRange dataRange) throws ElkIndexingException {
		ValueSpace<?> vs = dataRange.accept(getDataRangeConverter());
		
		return vs.isEmpty() ? EmptyValueSpace.INSTANCE : vs;
	}
	
	protected static <T> void validateFacetValue(ElkDatatypeRestriction elkDatatypeRestriction, ElkDatatype datatype, T value) throws ElkIndexingException {
		if (!datatype.isCompatibleWith(elkDatatypeRestriction.getDatatype())) {
			throw new ElkUnexpectedIndexingException(
					"The facet value "
							+ value
							+ " does not belong to the value space of the datatype restriction "
							+ OwlFunctionalStylePrinter
									.toString(elkDatatypeRestriction));
		}
	}
	
	protected ElkRealLiteral asNumericLiteral(ElkLiteral literal) {
		return literal.accept(new BaseElkNumericLiteralVisitor<ElkRealLiteral>() {
			
			@Override
			protected ElkRealLiteral defaultNumericVisit(ElkRealLiteral elkLiteral) {
				return elkLiteral;
			}});
	}

	protected abstract static class BaseDatatypeVisitor<O> extends BaseElkDatatypeVisitor<O> {

		@Override
		protected O defaultVisit(ElkDatatype elkLiteral) {
			throw new ElkUnexpectedIndexingException("Unexpected datatype " + elkLiteral);
		}
		
	}
	
	protected abstract static class BaseLiteralConverter<T> extends BaseElkLiteralVisitor<PointValue<?, T>> {

		@Override
		protected PointValue<?, T> defaultVisit(ElkLiteral elkLiteral) {
			throw new ElkUnexpectedIndexingException("Unexpected type of ELK literal " + elkLiteral);
		}
		
	}
	
	protected abstract static class BaseDataRangeConverter extends BaseElkDataRangeVisitor<ValueSpace<?>> {

		@Override
		protected ValueSpace<?> defaultVisit(ElkDataRange dataRange) {
			throw new ElkUnexpectedIndexingException("Unexpected type of ELK literal " + dataRange);
		}
		
	}
}
