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

public abstract class AbstractProperEntailmentQueryResult extends
		AbstractEntailmentQueryResult implements ProperEntailmentQueryResult {

	public AbstractProperEntailmentQueryResult(final ElkAxiom query) {
		super(query);
	}

	@Override
	public <O> O accept(final ProperEntailmentQueryResult.Visitor<O> visitor)
			throws ElkQueryException {
		return accept((EntailmentQueryResult.Visitor<O>) visitor);
	}

	@Override
	public <O> O accept(final EntailmentQueryResult.Visitor<O> visitor)
			throws ElkQueryException {
		return visitor.visit(this);
	}

}