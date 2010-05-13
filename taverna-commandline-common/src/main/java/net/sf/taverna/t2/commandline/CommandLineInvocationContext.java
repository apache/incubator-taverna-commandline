package net.sf.taverna.t2.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;

public class CommandLineInvocationContext implements InvocationContext {

	private final ReferenceService referenceService;

	private final ProvenanceReporter provenanceReporter;

	private List<Object> entities = Collections
			.synchronizedList(new ArrayList<Object>());

	public CommandLineInvocationContext(ReferenceService referenceService,
			ProvenanceReporter provenanceReporter) {
		this.referenceService = referenceService;
		this.provenanceReporter = provenanceReporter;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public ProvenanceReporter getProvenanceReporter() {
		return provenanceReporter;
	}

	public <T extends Object> List<T> getEntities(Class<T> entityType) {
		List<T> entitiesOfType = new ArrayList<T>();
		synchronized (entities) {
			for (Object entity : entities) {
				if (entityType.isInstance(entity)) {
					entitiesOfType.add(entityType.cast(entity));
				}
			}
		}
		return entitiesOfType;
	}

	public void addEntity(Object entity) {
		entities.add(entity);
	}

	public void removeEntity(Object entity) {
		entities.remove(entity);
	}
}