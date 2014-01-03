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
 * Copyright (C) 2011 - 2013 Department of Computer Science, University of Oxford
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

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.semanticweb.elk.reasoner.indexing.OntologyIndex;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedObjectSomeValuesFrom;
import org.semanticweb.elk.reasoner.saturation.ContextCreationListener;
import org.semanticweb.elk.reasoner.saturation.ContextImpl;
import org.semanticweb.elk.reasoner.saturation.ContextModificationListener;
import org.semanticweb.elk.reasoner.saturation.ExtendedSaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.SaturationState;
import org.semanticweb.elk.reasoner.saturation.SaturationStateWriter;
import org.semanticweb.elk.reasoner.saturation.SaturationStatistics;
import org.semanticweb.elk.reasoner.saturation.SaturationUtils;
import org.semanticweb.elk.reasoner.saturation.conclusions.BackwardLink;
import org.semanticweb.elk.reasoner.saturation.conclusions.ComposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.Conclusion;
import org.semanticweb.elk.reasoner.saturation.conclusions.DecomposedSubsumer;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.CombinedConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionInsertionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionOccurranceCheckingVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionStatistics;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.ConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.conclusions.visitors.NonRedundantRuleApplicationConclusionVisitor;
import org.semanticweb.elk.reasoner.saturation.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies rules to all conclusions of partially completed contexts to close
 * them deductively. Uses a local saturation state to iterate over all
 * conclusions, adds previously non-existent conclusions to the ToDo queues in
 * the main saturation state.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class ContextCompletionFactory extends RuleApplicationFactory {

	// logger for this class
	protected static final Logger LOGGER_ = LoggerFactory
			.getLogger(ContextCompletionFactory.class);

	private final LocalSaturationState localState_;

	public ContextCompletionFactory(SaturationState saturationState) {
		super(saturationState);
		localState_ = new LocalSaturationState(
				saturationState.getOntologyIndex());
	}

	@Override
	public AbstractLocalRuleEngine getDefaultEngine(
			ContextCreationListener listener,
			ContextModificationListener modListener) {
		return new ContextCompletionEngine();
	}

	@Override
	public SaturationState getSaturationState() {
		return localState_;
	}

	/**
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	private class ContextCompletionEngine extends AbstractLocalRuleEngine {
		private RuleVisitor initRuleAppVisitor_;
		// used for iteration over conclusions
		private ExtendedSaturationStateWriter iterationWriter_;
		// used for producing conclusions to the main contexts' ToDo and
		// creating main contexts, if needed
		private ExtendedSaturationStateWriter mainStateWriter_;
		// used to count produced conclusions
		private ConclusionVisitor<?> conclusionStatsVisitor_;

		protected ContextCompletionEngine() {
			super(ruleApplicationFactory, new SaturationStatistics());
		}

		@Override
		public void submit(IndexedClassExpression root) {
			// create a local context for this ICE
			getConclusionProducer().getCreateContext(root);
		}

		@Override
		protected ConclusionProducer getConclusionProducer() {
			if (iterationWriter_ == null) {
				ConclusionStatistics stats = localStatistics
						.getConclusionStatistics();

				initRuleAppVisitor_ = SaturationUtils
						.getStatsAwareRuleVisitor(localStatistics
								.getRuleStatistics());
				conclusionStatsVisitor_ = SaturationUtils
						.addStatsToConclusionVisitor(stats);
				iterationWriter_ = localState_.getExtendedWriter(
						ContextCreationListener.DUMMY,
						ContextModificationListener.DUMMY, initRuleAppVisitor_,
						conclusionStatsVisitor_, false);
				mainStateWriter_ = saturationState.getExtendedWriter(
						ContextCreationListener.DUMMY,
						ContextModificationListener.DUMMY, initRuleAppVisitor_,
						conclusionStatsVisitor_, true);
			}

			return iterationWriter_;
		}

		@Override
		protected ConclusionVisitor<Boolean> getBaseConclusionProcessor(
				ConclusionProducer producer) {

			RuleStatistics ruleStats = localStatistics.getRuleStatistics();
			// this decomposition visitor applies decomposition rules for
			// iterating over the conclusions
			SubsumerDecompositionVisitor iterationVisitor = SaturationUtils
					.getStatsAwareDecompositionRuleAppVisitor(
							new GapFillingDecompositionVisitor(
									iterationWriter_, mainStateWriter_),
							ruleStats);
			// this decomposition visitor applies decomposition rules for
			// producing conclusions obtained by decomposing negative subsumers
			// (those should not be stored in the main contexts)
			SubsumerDecompositionVisitor produceVisitor = SaturationUtils
					.getStatsAwareDecompositionRuleAppVisitor(
							new GapFillingDecompositionVisitor(localState_
									.getWriterForDecompositionVisitor(
											conclusionStatsVisitor_,
											initRuleAppVisitor_),
									mainStateWriter_), ruleStats);
			// this visitor applies rules to fill all gaps in the
			// deductive closure
			ConclusionGapFillingVisitor gapFiller = new ConclusionGapFillingVisitor(
					producer,
					SaturationUtils.getStatsAwareRuleVisitor(localStatistics
							.getRuleStatistics()), iterationVisitor,
					produceVisitor);

			return new CombinedConclusionVisitor(
					new ConclusionInsertionVisitor(),
					getUsedConclusionsCountingVisitor(gapFiller));
		}

	}

	/**
	 * Maintains a map of "local" contexts for class expressions, used as a
	 * cache to avoid infinite looping when iterating over all conclusions which
	 * belong to a certain context.
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	private class LocalSaturationState implements SaturationState {

		// private final RuleApplicationVisitor initRuleAppVisitor_ = new
		// BasicCompositionRuleApplicationVisitor();
		private final ConcurrentHashMap<IndexedClassExpression, Context> contextMap_;
		private final OntologyIndex ontologyIndex_;
		private final Queue<Context> activeContexts_ = new ConcurrentLinkedQueue<Context>();

		LocalSaturationState(OntologyIndex index) {
			contextMap_ = new ConcurrentHashMap<IndexedClassExpression, Context>();
			ontologyIndex_ = index;
		}

		@Override
		public OntologyIndex getOntologyIndex() {
			return ontologyIndex_;
		}

		@Override
		public Context getContext(IndexedClassExpression ice) {
			return contextMap_.get(ice);
		}

		@Override
		public Collection<Context> getContexts() {
			return contextMap_.values();
		}

		@Override
		public Collection<IndexedClassExpression> getNotSaturatedContexts() {
			return contextMap_.keySet();
		}

		@Override
		public ExtendedSaturationStateWriter getExtendedWriter(
				ContextCreationListener contextCreationListener,
				ContextModificationListener contextModificationListener,
				RuleVisitor ruleAppVisitor,
				ConclusionVisitor<?> conclusionVisitor,
				boolean trackNewContextsAsUnsaturated) {
			return new IterationSaturationStateWriter(conclusionVisitor,
					ruleAppVisitor, saturationState.getExtendedWriter(
							contextCreationListener,
							contextModificationListener, ruleAppVisitor,
							conclusionVisitor, trackNewContextsAsUnsaturated));
		}

		@Override
		public SaturationStateWriter getWriter(
				ContextModificationListener contextModificationListener,
				ConclusionVisitor<?> conclusionVisitor) {
			return getDefaultWriter(conclusionVisitor);
		}

		@Override
		public SaturationStateWriter getWriter(
				ConclusionVisitor<?> conclusionVisitor) {
			return getDefaultWriter(conclusionVisitor);
		}

		@Override
		public ExtendedSaturationStateWriter getExtendedWriter(
				ConclusionVisitor<?> conclusionVisitor) {
			return getDefaultWriter(conclusionVisitor);
		}

		private IterationSaturationStateWriter getDefaultWriter(
				ConclusionVisitor<?> conclusionVisitor) {
			return new IterationSaturationStateWriter(conclusionVisitor,
					new BasicRuleVisitor(),
					saturationState.getExtendedWriter(conclusionVisitor));
		}

		private ExtendedSaturationStateWriter getWriterForDecompositionVisitor(
				ConclusionVisitor<?> conclusionVisitor,
				RuleVisitor initRuleAppVisitor) {
			return new OptimizedLocalSaturationStateWriter(conclusionVisitor,
					initRuleAppVisitor);
		}

		/**
		 * Only produces conclusions for the local contexts. Used by the
		 * decomposition rule application visitor which should not produce the
		 * results of decomposition of negative subsumers to the main contexts.
		 * 
		 * @author Pavel Klinov
		 * 
		 *         pavel.klinov@uni-ulm.de
		 */
		private class OptimizedLocalSaturationStateWriter implements
				ExtendedSaturationStateWriter {

			private final RuleVisitor initRuleAppVisitor_;

			// needed for statistics
			private final ConclusionVisitor<?> conclusionVisitor_;

			private final ConclusionVisitor<Boolean> checker_;

			OptimizedLocalSaturationStateWriter(ConclusionVisitor<?> visitor,
					RuleVisitor ruleAppVisitor) {
				conclusionVisitor_ = visitor;
				checker_ = new ConclusionOccurranceCheckingVisitor();
				initRuleAppVisitor_ = ruleAppVisitor;
			}

			@Override
			public IndexedClassExpression getOwlThing() {
				return ontologyIndex_.getIndexedOwlThing();
			}

			@Override
			public IndexedClassExpression getOwlNothing() {
				return ontologyIndex_.getIndexedOwlNothing();
			}

			@Override
			public Context pollForActiveContext() {
				return activeContexts_.poll();
			}

			boolean existsGlobally(Context context, Conclusion conclusion) {
				return conclusion.accept(checker_, context.getRoot()
						.getContext());
			}

			void produceLocally(Context context, Conclusion conclusion) {
				Context localContext = getContext(context.getRoot());

				if (localContext == null) {
					localContext = getCreateContext(context.getRoot());
				}

				// used for stats
				conclusion.accept(conclusionVisitor_, localContext);

				if (localContext.addToDo(conclusion)) {
					// context was activated
					activeContexts_.add(localContext);
				}
			}

			/*
			 * Normally, since the rules are applied on main contexts, it should
			 * be the main contexts which are passed into this method. The
			 * exceptions occur during applications of the initialization rules
			 * which also happen on local contexts
			 */
			@Override
			public void produce(Context context, Conclusion conclusion) {

				if (existsGlobally(context, conclusion)) {
					LOGGER_.trace(
							"{}: conclusion {} exists in the main context, producing locally",
							context, conclusion);
					// produce the conclusion for the local copy of the context
					produceLocally(context, conclusion);
				}
			}

			@Override
			public boolean markAsNotSaturated(Context context) {
				return false;
			}

			@Override
			public void clearNotSaturatedContexts() {
				// this state doesn't maintain unsaturated contexts
			}

			@Override
			public void resetContexts() {
				contextMap_.clear();
			}

			@Override
			public Context getCreateContext(IndexedClassExpression root) {
				Context context = new ContextImpl(root);
				Context oldContext = contextMap_.putIfAbsent(root, context);

				if (oldContext == null) {
					initContext(context);

					if (LOGGER_.isTraceEnabled()) {
						LOGGER_.trace(context.getRoot()
								+ ": local context created");
					}

					return context;
				}
				return oldContext;
			}

			@Override
			public void initContext(Context context) {
				SaturationUtils.initContext(ontologyIndex_,
						initRuleAppVisitor_, context, this);
			}

			@Override
			public void removeContext(Context context) {
				contextMap_.remove(context.getRoot());
			}
		}

		/**
		 * This writer is used for iterating over the conclusions whuch belong
		 * to a certain context in the main saturation state.
		 * 
		 * It produces conclusions to two contexts: the local copy (if the
		 * conclusion exists in the main context) and the main context
		 * otherwise.
		 * 
		 * @author Pavel Klinov
		 * 
		 *         pavel.klinov@uni-ulm.de
		 */
		private class IterationSaturationStateWriter extends
				OptimizedLocalSaturationStateWriter {

			private final ExtendedSaturationStateWriter mainStateWriter_;

			IterationSaturationStateWriter(ConclusionVisitor<?> visitor,
					RuleVisitor ruleAppVisitor,
					ExtendedSaturationStateWriter writer) {
				super(visitor, ruleAppVisitor);
				mainStateWriter_ = writer;
			}

			@Override
			public IndexedClassExpression getOwlThing() {
				return ontologyIndex_.getIndexedOwlThing();
			}

			@Override
			public IndexedClassExpression getOwlNothing() {
				return ontologyIndex_.getIndexedOwlNothing();
			}

			@Override
			public Context pollForActiveContext() {
				return activeContexts_.poll();
			}

			void produceGlobally(Context context, Conclusion conclusion) {
				// insert to the main context's ToDo
				LOGGER_.trace(
						"{}: conclusion {} does NOT exist in the main context, insert into TODO",
						context, conclusion);

				mainStateWriter_.produce(context.getRoot().getContext(),
						conclusion);
			}

			@Override
			public void produce(Context context, Conclusion conclusion) {
				if (existsGlobally(context, conclusion)) {
					// produce the conclusion for the local copy of the context
					// but only if the main context is not modified
					// (the same logic as was previously used for cleaning)
					Context sourceContext = conclusion
							.getSourceContext(context);

					if (sourceContext == null || !sourceContext.isSaturated()) {
						produceLocally(context, conclusion);
					}

				} else {
					// produce the conclusion for the main context
					produceGlobally(context, conclusion);
				}
			}
		}
	}

	/**
	 * Decomposition rule application visitor which can create new global
	 * contexts when decomposing existentials.
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	private static class GapFillingDecompositionVisitor extends
			BasicDecompositionRuleApplicationVisitor {
		// depending on this writer, results of decompositions may or may not be
		// produced to main contexts
		private final SaturationStateWriter localWriter_;

		private final ExtendedSaturationStateWriter mainWriter_;

		GapFillingDecompositionVisitor(SaturationStateWriter localWriter,
				ExtendedSaturationStateWriter mainWriter) {
			localWriter_ = localWriter;
			mainWriter_ = mainWriter;
		}

		@Override
		public void visit(IndexedObjectSomeValuesFrom ice, Context context) {
			// may need to create a new main context for the filler
			Context fillerContext = mainWriter_.getCreateContext(ice
					.getFiller());

			localWriter_.produce(fillerContext,
					new BackwardLink(context, ice.getRelation()));
		}

		@Override
		protected ConclusionProducer getConclusionProducer() {
			return localWriter_;
		}

	}

	/**
	 * Applies conclusion rules to the main context of the class expression (not
	 * the local context).
	 * 
	 * This visitor uses two different decomposition rule application visitors:
	 * One is used for iterating over the set of conclusions for a context (this
	 * requires decomposition of all subsumers, positive and negative, to not
	 * miss any conclusion regardless of the order of rule applications). The
	 * other is used for "gap filling", i.e. adding missing conclusions to the
	 * main context's ToDo. It does not require decomposition of negative
	 * subsumers.
	 * 
	 * @author Pavel Klinov
	 * 
	 *         pavel.klinov@uni-ulm.de
	 */
	private static class ConclusionGapFillingVisitor extends
			NonRedundantRuleApplicationConclusionVisitor implements
			ConclusionVisitor<Boolean> {

		private final SaturationStateWriter iterationWriter_;
		private final RuleVisitor ruleAppVisitor_;
		private final SubsumerDecompositionVisitor iterateDecompRuleAppVisitor_;
		private final SubsumerDecompositionVisitor produceDecompRuleAppVisitor_;

		public ConclusionGapFillingVisitor(SaturationStateWriter enumWriter,
				RuleVisitor ruleAppVisitor,
				SubsumerDecompositionVisitor enumVisitor,
				SubsumerDecompositionVisitor produceVisitor) {
			super(ruleAppVisitor, enumWriter);
			this.iterationWriter_ = enumWriter;
			this.ruleAppVisitor_ = ruleAppVisitor;
			this.iterateDecompRuleAppVisitor_ = enumVisitor;
			this.produceDecompRuleAppVisitor_ = produceVisitor;

		}

		@Override
		public Boolean defaultVisit(Conclusion conclusion, Context context) {
			conclusion.applyNonRedundantRules(ruleAppVisitor_, context
					.getRoot().getContext(), iterationWriter_);
			return true;
		}

		@Override
		public Boolean visit(ComposedSubsumer negSCE, Context context) {
			defaultVisit(negSCE, context);
			negSCE.getExpression().accept(produceDecompRuleAppVisitor_,
					context.getRoot().getContext());
			return true;
		}

		@Override
		public Boolean visit(DecomposedSubsumer posSCE, Context context) {
			defaultVisit(posSCE, context);
			posSCE.getExpression().accept(iterateDecompRuleAppVisitor_,
					context.getRoot().getContext());
			return true;
		}

	}

}
