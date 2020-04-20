/*******************************************************************************************************
 *
 * gama.runtime.benchmark.BenchmarkCSVExporter.java, in plugin gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.runtime.benchmark;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import gama.GAMA;
import gama.common.interfaces.IBenchmarkable;
import gama.common.interfaces.experiment.IExperimentPlan;
import gama.common.util.FileUtils;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gama.util.file.CsvWriter;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gama.util.tree.GamaTree.Order;
import gaml.operators.Files;
import gaml.types.Types;

public class BenchmarkCSVExporter {
	private static final String exportFolder = "benchmarks";

	public void save(final IExperimentPlan experiment, final Benchmark records) {
		final IScope scope = experiment.getExperimentScope();
		try {
			Files.newFolder(scope, exportFolder);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to create folder " + exportFolder);
			GAMA.reportError(scope, e1, false);
			e1.printStackTrace();
			return;
		}
		final IMap<IScope, Benchmark.ScopeRecord> scopes = GamaMapFactory.wrap(Types.NO_TYPE, Types.NO_TYPE, records);
		final String exportFileName =
				FileUtils.constructAbsoluteFilePath(scope, exportFolder + "/" + experiment.getModel().getName()
						+ "_benchmark_" + Instant.now().toString().replace(':', '_') + ".csv", false);

		final List<String> headers = new ArrayList<>();
		final List<List<String>> contents = new ArrayList<>();
		headers.add("Execution");
		scopes.forEach((scopeRecord, record) -> {
			headers.add("Time in ms in " + scopeRecord);
			headers.add("Invocations in " + scopeRecord);
		});
		contents.add(headers);
		records.tree.visit(Order.PRE_ORDER, (n) -> {
			final IBenchmarkable r = n.getData();
			final List<String> line = new ArrayList<>();
			contents.add(line);
			line.add(r.getNameForBenchmarks());
			scopes.forEach((scope1, scopeRecord) -> {
				final BenchmarkRecord record1 = scopeRecord.find(r);
				line.add(record1.isUnrecorded() ? "" : String.valueOf(record1.milliseconds));
				line.add(record1.isUnrecorded() ? "" : String.valueOf(record1.times));
			});
		});

		try (final CsvWriter writer = new CsvWriter(exportFileName)) {
			writer.setDelimiter(';');
			writer.setUseTextQualifier(false);
			for (final List<String> ss : contents) {
				writer.writeRecord(ss.toArray(new String[ss.size()]));
			}
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

}