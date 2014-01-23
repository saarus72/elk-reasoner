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

import org.semanticweb.elk.owl.exceptions.ElkException;
import org.semanticweb.elk.reasoner.indexing.hierarchy.IndexedClassExpression;
import org.semanticweb.elk.reasoner.saturation.ClassExpressionSaturation;
import org.semanticweb.elk.reasoner.saturation.rules.RuleApplicationFactory;
import org.semanticweb.elk.reasoner.saturation.tracing.TracingEnabledRuleApplicationFactory;
import org.semanticweb.elk.reasoner.saturation.tracing.LocalTracingSaturationState;

/**
 
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 */
public class ContextTracingStage extends AbstractReasonerStage {

	private ClassExpressionSaturation<IndexedClassExpression> tracing_;

	public ContextTracingStage(AbstractReasonerState reasoner,
			AbstractReasonerStage... preStages) {
		super(reasoner, preStages);
	}

	@Override
	public String getName() {
		return "Context Tracing";
	}

	@Override
	public void printInfo() {
		if (tracing_ != null) {
			tracing_.printStatistics();
		}
	}

	@Override
	public boolean preExecute() {
		if (!super.preExecute()) {
			return false;
		}
		
		LocalTracingSaturationState tracingState = reasoner.traceState.getSaturationState();
		//TraceStore.Writer inferenceWriter = reasoner.traceState.getTraceStore().getWriter();
		RuleApplicationFactory ruleAppFactory = new TracingEnabledRuleApplicationFactory(reasoner.saturationState, tracingState, reasoner.traceState.getTraceStore());
		
		tracing_ = new ClassExpressionSaturation<IndexedClassExpression>(
				reasoner.traceState.getTracingQueue().keySet(), reasoner.getProcessExecutor(), workerNo,
				reasoner.getProgressMonitor(), ruleAppFactory);

		return true;
	}

	@Override
	void executeStage() throws ElkException {
		for (;;) {
			tracing_.process();

			if (!spuriousInterrupt())
				break;
		}
	}

	@Override
	public boolean postExecute() {
		if (!super.postExecute()) {
			return false;
		}
		
		reasoner.ruleAndConclusionStats.add(tracing_.getRuleAndConclusionStatistics());
		reasoner.traceState.flushQueue();
		tracing_ = null;

		return true;
	}

}
