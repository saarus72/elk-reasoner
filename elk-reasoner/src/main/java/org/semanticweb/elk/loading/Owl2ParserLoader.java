/*
 * #%L
 * ELK Reasoner
 * 
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
package org.semanticweb.elk.loading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.parsing.Owl2ParseException;
import org.semanticweb.elk.owl.parsing.Owl2Parser;
import org.semanticweb.elk.owl.parsing.Owl2ParserAxiomProcessor;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;

/**
 * An {@link Loader} that loads an ontology using a provided {@link Owl2Parser}.
 * 
 * @author "Yevgeny Kazakov"
 * 
 */
abstract class Owl2ParserLoader implements Loader {

	/**
	 * the parser used to provide the axioms
	 */
	private final Owl2Parser parser_;
	/**
	 * the loader for the axioms
	 */
	private final ElkAxiomProcessor axiomLoader_;
	/**
	 * a bounded buffer into which the axioms are loaded; if the puffer is full
	 * the parser will block until axioms are taken
	 */
	private final BlockingQueue<ElkAxiom> axiomBuffer_;
	/**
	 * the thread in which the parser is running
	 */
	private final Thread parserThread_;
	/**
	 * the thread from which axioms are taken
	 */
	private volatile Thread controlThread_;
	/**
	 * {@code true} if the parser thread has started
	 */
	private volatile boolean started_;
	/**
	 * {@code true} if the parser has finished processing the ontology
	 */
	private volatile boolean finished_;
	/**
	 * {@code true} if the master thread expects new axioms from the parser
	 * thread
	 */
	private volatile boolean waiting_;
	/**
	 * the exception created if something goes wrong
	 */
	protected volatile ElkLoadingException exception;

	/**
	 * Creating an {@link Owl2ParserLoader}, which loads axioms generated using
	 * {@link Owl2Parser} through {@link ElkAxiomProcessor}. The axioms
	 * generated by the parser are stored in a buffer of the given
	 * {@code bufferSize}.
	 * 
	 * @param owlParser
	 *            the parser used to load the ontology
	 * @param axiomLoader
	 *            the loader for the axioms
	 * @param bufferSize
	 *            the size of the bounded buffer for loaded axioms
	 */
	public Owl2ParserLoader(Owl2Parser owlParser,
			ElkAxiomProcessor axiomLoader, int bufferSize) {
		this.parser_ = owlParser;
		this.axiomLoader_ = axiomLoader;
		this.axiomBuffer_ = new ArrayBlockingQueue<ElkAxiom>(bufferSize);
		this.finished_ = false;
		this.parserThread_ = new Thread(new Parser(), "elk-parser-thread");
		this.started_ = false;
		this.exception = null;
	}

	/**
	 * Creating an {@link Owl2ParserLoader}, which loads axioms generated using
	 * {@link Owl2Parser} through {@link ElkAxiomProcessor}.
	 * 
	 * @param owlParser
	 *            the parser used to load the ontology
	 * @param axiomLoader
	 *            the loader for the axioms
	 */
	public Owl2ParserLoader(Owl2Parser owlParser, ElkAxiomProcessor axiomLoader) {
		this(owlParser, axiomLoader, 256);
	}

	@Override
	public synchronized void load() throws ElkLoadingException {
		controlThread_ = Thread.currentThread();
		if (!started_) {
			parserThread_.start();
			started_ = true;
		}
		ElkAxiom axiom = null;
		waiting_ = true;
		try {
			for (;;) {
				if (Thread.currentThread().isInterrupted())
					break;
				if (finished_) {
					axiom = axiomBuffer_.poll();
				} else {
					try {
						axiom = axiomBuffer_.take();
					} catch (InterruptedException e) {
						/*
						 * we don't know for sure why the thread was
						 * interrupted, so we need to obey the master; it will
						 * restart the process if necessary; we need to restore
						 * the interrupt status of the thread in this case
						 */
						Thread.currentThread().interrupt();
						break;
					}
				}
				if (axiom == null)
					break;
				axiomLoader_.visit(axiom);
			}
		} finally {
			/*
			 * should be executed in any case
			 */
			synchronized (axiomBuffer_) {
				waiting_ = false;
			}
			if (exception != null)
				throw exception;
		}
	}

	/**
	 * the hook to close the parsing resources, e.g., streams after the parser
	 * is finished, to be implemented in subclasses
	 */
	protected abstract void closeParsingResources();

	/**
	 * The parser worker used to parse the ontology
	 * 
	 * @author "Yevgeny Kazakov"
	 * 
	 */
	private class Parser implements Runnable {
		@Override
		public void run() {
			try {
				parser_.accept(new AxiomInserter(axiomBuffer_));
			} catch (Exception e) {
				exception = new ElkLoadingException("Cannot load the ontology!",
						e);
			} finally {
				try {
					/*
					 * just don't want something like this to fail but not sure
					 * where one can save the exception
					 */
					closeParsingResources();
				} finally {
					/*
					 * should be executed in any case
					 */
					finished_ = true;
					synchronized (axiomBuffer_) {
						if (waiting_)
							controlThread_.interrupt();
					}
				}
			}
		}
	}

	/**
	 * A simple {@link ElkAxiomProcessor} that insert the parsed axioms into the
	 * given queue
	 * 
	 * @author "Yevgeny Kazakov"
	 * 
	 */
	private static class AxiomInserter implements Owl2ParserAxiomProcessor {

		final BlockingQueue<ElkAxiom> axiomBuffer;

		AxiomInserter(BlockingQueue<ElkAxiom> axiomBuffer) {
			this.axiomBuffer = axiomBuffer;
		}

		@Override
		public void visit(ElkAxiom elkAxiom) throws Owl2ParseException {
			try {
				axiomBuffer.put(elkAxiom);
			} catch (InterruptedException e) {
				throw new Owl2ParseException("ELK Parser was interrupted", e);
			}

		}
	}

}
