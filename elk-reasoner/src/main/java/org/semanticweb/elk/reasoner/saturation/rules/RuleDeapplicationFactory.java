/**
 * 
 */
package org.semanticweb.elk.reasoner.saturation.rules;

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

import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.ContextCreationListener;
import org.semanticweb.elk.reasoner.saturation.ContextModificationListener;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.SaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.SaturationStatistics;
import org.semanticweb.elk.reasoner.saturation.SaturationUtils;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.CombinedConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionDeletionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionOccurranceCheckingVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.rules.subsumers.SubsumerDecompositionVisitor;

/**
 * Creates an engine which applies rules backwards, e.g., removes conclusions
 * from the context instead of adding them
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * 
 * @author "Yevgeny Kazakov"
 */
public class RuleDeapplicationFactory extends RuleApplicationFactory {

	public RuleDeapplicationFactory(final SaturationState saturationState,
			boolean trackModifiedContexts) {
		super(saturationState, trackModifiedContexts);
	}

	@Override
	public DeapplicationEngine getDefaultEngine(
			ContextCreationListener listener,
			ContextModificationListener modListener) {
		return new DeapplicationEngine(modListener);
	}

	/**
	 * 
	 */
	public class DeapplicationEngine extends AbstractLocalRuleEngine {

		private final SaturationStateWriter writer_;

		protected DeapplicationEngine(ContextModificationListener listener) {
			super(new SaturationStatistics());

			writer_ = saturationState.getWriter(SaturationUtils
					.addStatsToContextModificationListener(listener,
							localStatistics.getContextStatistics()),
					SaturationUtils.addStatsToConclusionVisitor(localStatistics
							.getConclusionStatistics()));
		}

		@Override
		protected ConclusionVisitor<Boolean> getBaseConclusionProcessor(
				ConclusionProducer producer) {

			return new CombinedConclusionVisitor(
					new CombinedConclusionVisitor(
							new ConclusionOccurranceCheckingVisitor(),
							getUsedConclusionsCountingVisitor(new ConclusionRuleApplicationVisitorMax(
									producer,
									SaturationUtils
											.getStatsAwareRuleVisitor(localStatistics
													.getRuleStatistics()),
									SaturationUtils
											.getStatsAwareDecompositionRuleAppVisitor(
													getDecompositionRuleApplicationVisitor(),
													localStatistics
															.getRuleStatistics())))),
					new ConclusionDeletionVisitor());
		}

		@Override
		public void submit(IndexedClassExpression job) {
		}

		@Override
		protected ConclusionProducer getConclusionProducer() {
			return writer_;
		}

		@Override
		protected SubsumerDecompositionVisitor getDecompositionRuleApplicationVisitor() {
			// this decomposition visitor takes the basic writer which cannot
			// create new contexts
			return new BackwardDecompositionRuleApplicationVisitor(
					getConclusionProducer());
		}
	}

}
