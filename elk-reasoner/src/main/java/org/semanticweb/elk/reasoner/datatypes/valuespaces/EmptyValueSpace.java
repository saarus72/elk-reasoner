/*
 * #%L
 * ELK Reasoner
 * *
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
package org.semanticweb.elk.reasoner.datatypes.valuespaces;

import org.semanticweb.elk.owl.interfaces.ElkDatatype;
import org.semanticweb.elk.owl.managers.ElkDatatypeMap;

/**
 * Representation of the empty value space. This class always has a single
 * instance. The reason it was kept as a class is because it's sometimes handy
 * to check for the empty value space using visitors.
 * 
 * @author Pospishnyi Olexandr
 */
public class EmptyValueSpace implements ValueSpace<ElkDatatype>, PointValue<ElkDatatype, Object> {

	@Override
	public ElkDatatype getDatatype() {
		return ElkDatatypeMap.RDFS_LITERAL;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(ValueSpace<?> valueSpace) {
		return false;
	}

	@Override
	public boolean isSubsumedBy(ValueSpace<?> valueSpace) {
		return valueSpace.contains(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Ø";
	}

	@Override
	public <O> O accept(ValueSpaceVisitor<O> visitor) {
		return visitor.visit(this);
	}

	public static final EmptyValueSpace INSTANCE = new EmptyValueSpace();

	@Override
	public Object getValue() {
		return null;
	}
}
