/**
 * 
 */
package org.semanticweb.elk.reasoner.stages;
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

import java.util.Arrays;

import org.semanticweb.elk.owl.exceptions.ElkException;
import org.semanticweb.elk.reasoner.incremental.IncrementalStages;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.ClassExpressionSaturation;
import org.semanticweb.elk.reasoner.saturation.rules.RuleApplicationFactory;

/**
 * Completes saturation of all contexts which are not saturated at this point.
 * Useful, for example, to continue saturation after the ontology was proved inconsistent
 * and all workers have stopped, possibly in the middle of saturation.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class IncrementalCompletionStage extends AbstractReasonerStage {

	public IncrementalCompletionStage(AbstractReasonerState reasoner) {
		super(reasoner);
	}

	// logger for this class
	//private static final Logger LOGGER_ = Logger.getLogger(IncrementalCompletionStage.class);
	
	private ClassExpressionSaturation<IndexedClassExpression> completion_ = null;

	@Override
	public String getName() {
		return IncrementalStages.COMPLETION.toString();
	}

	@Override
	public boolean done() {
		return reasoner.incrementalState.getStageStatus(IncrementalStages.COMPLETION);
	}

	@Override
	public Iterable<ReasonerStage> getDependencies() {
		return Arrays.asList((ReasonerStage) new ChangesLoadingStage(reasoner));
	}

	@Override
	public void execute() throws ElkException {
		if (completion_ == null) {
			initComputation();
		}
		
		progressMonitor.start(getName());

		try {
			for (;;) {
				completion_.process();
				if (!interrupted())
					break;
			}
		} finally {
			progressMonitor.finish();
		}

		reasoner.incrementalState.setStageStatus(IncrementalStages.COMPLETION, true);
		markAllContextsAsSaturated();
		//reasoner.saturationState.getWriter().clearNotSaturatedContexts();
	}

	@Override
	void initComputation() {
		super.initComputation();

		RuleApplicationFactory appFactory = new RuleApplicationFactory(reasoner.saturationState);
		
		completion_ = new ClassExpressionSaturation<IndexedClassExpression>(
				reasoner.getProcessExecutor(),
				workerNo,
				reasoner.getProgressMonitor(),
				appFactory);
	}	
	
	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
	}

}