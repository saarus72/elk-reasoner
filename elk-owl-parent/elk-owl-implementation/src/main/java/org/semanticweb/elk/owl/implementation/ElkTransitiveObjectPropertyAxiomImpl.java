/*
 * #%L
 * elk-reasoner
 * 
 * $Id: ElkTransitiveObjectPropertyAxiom.java 71 2011-06-05 11:11:41Z mak@aifb.uni-karlsruhe.de $
 * $HeadURL: https://elk-reasoner.googlecode.com/svn/trunk/elk-reasoner/src/main/java/org/semanticweb/elk/syntax/ElkTransitiveObjectPropertyAxiom.java $
 * %%
 * Copyright (C) 2011 Oxford University Computing Laboratory
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
/**
 * @author Yevgeny Kazakov, Apr 8, 2011
 */
package org.semanticweb.elk.owl.implementation;

import org.semanticweb.elk.owl.interfaces.ElkObjectPropertyExpression;
import org.semanticweb.elk.owl.interfaces.ElkTransitiveObjectPropertyAxiom;
import org.semanticweb.elk.owl.visitors.ElkObjectPropertyAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkPropertyAxiomVisitor;
import org.semanticweb.elk.owl.visitors.ElkTransitiveObjectPropertyAxiomVisitor;

/**
 * Implementation of {@link ElkTransitiveObjectPropertyAxiom}.
 * 
 * @author Markus Kroetzsch
 * @author "Yevgeny Kazakov"
 * 
 */
public class ElkTransitiveObjectPropertyAxiomImpl extends
		ElkPropertyAxiomImpl<ElkObjectPropertyExpression> implements
		ElkTransitiveObjectPropertyAxiom {

	ElkTransitiveObjectPropertyAxiomImpl(
			ElkObjectPropertyExpression property) {
		super(property);
	}

	@Override
	public <O> O accept(ElkObjectPropertyAxiomVisitor<O> visitor) {
		return accept((ElkTransitiveObjectPropertyAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkPropertyAxiomVisitor<O> visitor) {
		return accept((ElkTransitiveObjectPropertyAxiomVisitor<O>) visitor);
	}

	@Override
	public <O> O accept(ElkTransitiveObjectPropertyAxiomVisitor<O> visitor) {
		return visitor.visit(this);
	}

}
