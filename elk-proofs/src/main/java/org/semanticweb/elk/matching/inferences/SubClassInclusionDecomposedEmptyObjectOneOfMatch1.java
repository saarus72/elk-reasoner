package org.semanticweb.elk.matching.inferences;

/*-
 * #%L
 * ELK Proofs Package
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

import java.util.Collections;

import org.semanticweb.elk.matching.conclusions.ConclusionMatchExpressionFactory;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionDecomposedMatch1;
import org.semanticweb.elk.matching.conclusions.SubClassInclusionDecomposedMatch2;
import org.semanticweb.elk.matching.root.IndexedContextRootMatch;
import org.semanticweb.elk.owl.interfaces.ElkIndividual;

public class SubClassInclusionDecomposedEmptyObjectOneOfMatch1
		extends AbstractSubClassInclusionDecomposedCanonizerMatch1 {

	SubClassInclusionDecomposedEmptyObjectOneOfMatch1(
			SubClassInclusionDecomposedMatch1 parent,
			IndexedContextRootMatch extendedDestinationMatch) {
		super(parent, extendedDestinationMatch);
	}

	@Override
	SubClassInclusionDecomposedMatch2 getPremiseMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionDecomposedMatch2(getParent(),
				getExtendedDestinationMatch(), factory.getObjectOneOf(
						Collections.<ElkIndividual> emptyList()));
	}

	@Override
	public SubClassInclusionDecomposedMatch2 getConclusionMatch(
			ConclusionMatchExpressionFactory factory) {
		return factory.getSubClassInclusionDecomposedMatch2(getParent(),
				getExtendedDestinationMatch(), factory.getOwlNothing());
	}

	@Override
	public <O> O accept(InferenceMatch.Visitor<O> visitor) {
		return visitor.visit(this);
	}

	/**
	 * The visitor pattern for instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 * @param <O>
	 *            the type of the output
	 */
	public interface Visitor<O> {

		O visit(SubClassInclusionDecomposedEmptyObjectOneOfMatch1 inferenceMatch1);

	}

	/**
	 * A factory for creating instances
	 * 
	 * @author Yevgeny Kazakov
	 *
	 */
	public interface Factory {

		SubClassInclusionDecomposedEmptyObjectOneOfMatch1 getSubClassInclusionDecomposedEmptyObjectOneOfMatch1(
				SubClassInclusionDecomposedMatch1 parent,
				IndexedContextRootMatch extendedDestinationMatch);

	}

}
