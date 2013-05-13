/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package uk.org.taverna.commandline;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.org.taverna.commandline.WorkflowTestSuite.Workflows;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.rdfxml.RDFXMLReader;

/**
 * Regression tests for Taverna Command Line Tool.
 *
 * @author David Withers
 */
@RunWith(WorkflowTestSuite.class)
public class TavernaCommandLineTest {

	private static String baseVersion = "2.4.0";
	private static boolean baseVersionReleased = true;

	private static String testVersion = "3.0.0-SNAPSHOT";
	private static boolean testVersionReleased = false;
	private static boolean testVersionSupportsScufl2 = true;

	private static String script = "executeworkflow.sh";
	private static String baseName = "taverna-commandline-" + baseVersion;
	private static String testName = "taverna-commandline-" + testVersion;
	private static String releasedLocation = "https://launchpad.net/taverna/t2/";
	private static String unreleasedLocation = "http://build.mygrid.org.uk/ci/job/t3-taverna-commandline-product/lastSuccessfulBuild/net.sf.taverna.t2$taverna-commandline/artifact/net.sf.taverna.t2/taverna-commandline/";

	private static String baseVersionLocation =  (baseVersionReleased ? releasedLocation : unreleasedLocation) + baseVersion + "/+download/" + baseName + ".zip";
	private static String testVersionLocation = (testVersionReleased ? releasedLocation : unreleasedLocation) + testVersion + "/" + testName + (testVersionReleased ? ".zip" : "-bin.zip");

	private static String baseCommand;
	private static String testCommand;

	private static File buildDirectory;

	private File workflowDirectory;
	private File baseOutput;
	private File testOutput;
	private boolean secure;
	private List<File> inputs;
	private String ignorePort;
	private String message;

	public TavernaCommandLineTest(File workflowDirectory) throws Exception {
		this.workflowDirectory = workflowDirectory;
		if (buildDirectory == null) {
			String buildDirectoryLocation = System.getProperty("buildDirectory");
			if (buildDirectoryLocation == null) {
				buildDirectoryLocation = "/Users/david/Documents/workspace-trunk/taverna-command-line-tests/target";
			}
			buildDirectory = new File(buildDirectoryLocation);
			buildDirectory.mkdirs();
		}
		if (baseCommand == null) {
			File commandDirectory = new File(buildDirectory, baseName);
			if (!commandDirectory.exists()) {
				System.out.println("Fetching " + baseName);
				fetchTaverna(baseVersionLocation, baseName);
			}
			baseCommand = new File(baseName, script).getPath();
		}
		if (testCommand == null) {
			File commandDirectory = new File(buildDirectory, testName);
			if (!commandDirectory.exists()) {
				System.out.println("Fetching " + testName);
				fetchTaverna(testVersionLocation, testName);
			}
			testCommand = new File(testName, script).getPath();
		}
		File outputsDirectory = new File(buildDirectory, "test-outputs");
		baseOutput = new File(outputsDirectory, workflowDirectory.getName() + "-" + baseVersion);
		testOutput = new File(outputsDirectory, workflowDirectory.getName() + "-" + testVersion);
		secure = workflowDirectory.getName().startsWith("secure");
		inputs = getInputs();
		ignorePort = getIgnorePort();
		message = "Running {0} with version {1}";
	}

    @Workflows
    public static List<File> workflows() {
    	List<File> workflows = new ArrayList<File>();
		for (File workflowDirectory : getResources("workflows")) {
			workflows.add(workflowDirectory);
		}
		for (File workflowDirectory : getResources("myexperiment")) {
			workflows.add(workflowDirectory);
		}
    	return workflows;
    }

    @Before
    public void setup() throws Exception {
    	if (!baseOutput.exists()) {
    		if (baseVersion.equals("2.3.0") && workflowDirectory.getName().equals("tool")) return;//version 2.3.0 is missing tool plugin
    		String workflow = getWorkflow().toASCIIString();
    		System.out.println(MessageFormat.format(message, workflow, baseVersion) + (inputs.size() > 0 ? " using input values" : ""));
    		runWorkflow(baseCommand, workflow, baseOutput, true, secure, false);
    		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), baseOutput.exists());
    	}
    }

    public boolean testExcluded() {
    	//version 3.0.0 is missing biomoby activity
		if (testVersion.startsWith("3.0.0") && workflowDirectory.getName().contains("biomoby")) return true;
    	//version 3.0.0 is missing looping configuration
		if (testVersion.startsWith("3.0.0") && workflowDirectory.getName().equals("ebi_interproscan_newservices")) return true;
		if (testVersion.startsWith("3.0.0") && workflowDirectory.getName().equals("biomartandembossanalysis")) return true;
		return false;
    }

	@Test
	public void testWorkflowWithoutInputs() throws Exception {
		assumeTrue(!testExcluded());
		assumeTrue(baseOutput.exists());
		assumeTrue(inputs.isEmpty());
		FileUtils.deleteDirectory(testOutput);
		String workflow = getWorkflow().toASCIIString();
		System.out.println(MessageFormat.format(message, workflow, testVersion));
		runWorkflow(testCommand, workflow, testOutput, true, secure, false);
		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), testOutput.exists());
		assertOutputsEquals(baseOutput, testOutput);
	}

	@Test
	public void testWorkflowWithInputValues() throws Exception {
		assumeTrue(!testExcluded());
		assumeTrue(baseOutput.exists());
		assumeTrue(inputs.size() > 0);
		FileUtils.deleteDirectory(testOutput);
		String workflow = getWorkflow().toASCIIString();
		System.out.println(MessageFormat.format(message, workflow, testVersion) + " using input values");
		runWorkflow(testCommand, workflow, testOutput, true, secure, false);
		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), testOutput.exists());
		assertOutputsEquals(baseOutput, testOutput);
	}

	@Test@Ignore
	public void testWorkflowWithInputFiles() throws Exception {
		assumeTrue(!testExcluded());
		assumeTrue(baseOutput.exists());
		assumeTrue(inputs.size() > 0);
		FileUtils.deleteDirectory(testOutput);
		String workflow = getWorkflow().toASCIIString();
		System.out.println(MessageFormat.format(message, workflow, testVersion) + " using input files");
		runWorkflow(testCommand, workflow, testOutput, false, secure, false);
		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), testOutput.exists());
		assertOutputsEquals(baseOutput, testOutput);
	}

	@Test@Ignore
	public void testWorkflowWithDatabase() throws Exception {
		assumeTrue(!testExcluded());
		assumeTrue(baseOutput.exists());
		assumeTrue(inputs.size() > 0);
		FileUtils.deleteDirectory(testOutput);
		String workflow = getWorkflow().toASCIIString();
		System.out.println(MessageFormat.format(message, workflow, testVersion) + " using database");
		runWorkflow(testCommand, workflow, testOutput, true, secure, true);
		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), testOutput.exists());
		assertOutputsEquals(baseOutput, testOutput);
	}

	@Test
	public void testScufl2Workflow() throws Exception {
		assumeTrue(!testExcluded());
		assumeTrue(baseOutput.exists());
		assumeTrue(testVersionSupportsScufl2);

		//assumeTrue(workflowDirectory.getName().contains("rest"));//skip rest due to SCUFL2-121
		//assumeTrue(!workflowDirectory.getName().startsWith("secure-basic"));//skip rest due to SCUFL2-121
		//assumeTrue(!workflowDirectory.getName().startsWith("secure-client"));//skip rest due to SCUFL2-121
		//assumeTrue(!workflowDirectory.getName().startsWith("secure-digest"));//skip rest due to SCUFL2-121

		FileUtils.deleteDirectory(testOutput);
		String workflow = getScufl2Workflow().toASCIIString();
		System.out.println(MessageFormat.format(message, workflow, testVersion) + (inputs.size() > 0 ? " using input values" : ""));
		runWorkflow(testCommand, workflow, testOutput, true, secure, true);
		assertTrue(String.format("No output produced for %s", workflowDirectory.getName()), testOutput.exists());
		assertOutputsEquals(baseOutput, testOutput);
	}

	private synchronized void runWorkflow(String command, String workflow, File outputsDirectory, boolean inputValues, boolean secure, boolean database)
			throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder("sh", command);
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(buildDirectory);
		List<String> args = processBuilder.command();
		for (File input : inputs) {
			if (inputValues) {
				args.add("-inputvalue");
				args.add(input.getName());
				args.add(IOUtils.toString(new FileReader(input)));
			} else {
				args.add("-inputfile");
				args.add(input.getName());
				args.add(input.getAbsolutePath());
			}
		}
		args.add("-outputdir");
		args.add(outputsDirectory.getPath());
		if (secure) {
			args.add("-cmdir");
			args.add(getClass().getResource("/security").getFile());
			args.add("-cmpassword");
		}
		if (database) {
			args.add("-embedded");
		}
		args.add(workflow);
		Process process = processBuilder.start();
		if (secure) {
			PrintStream outputStream = new PrintStream(process.getOutputStream());
			outputStream.println("test");
			outputStream.flush();
		}
		waitFor(process);
	}

	private URI getWorkflow() throws Exception {
		File workflow = new File(workflowDirectory, workflowDirectory.getName() + ".t2flow");
		if (!workflow.exists()) {
			workflow = new File(workflowDirectory, workflowDirectory.getName() + ".url");
			return new URI(IOUtils.toString(new FileReader(workflow)));
		}
		return workflow.toURI();
	}

	private URI getScufl2Workflow() throws Exception {
		File workflow = new File(buildDirectory, workflowDirectory.getName() + ".scufl2");
//		if (!workflow.exists()) {
			WorkflowBundleIO workflowBundleIO = new WorkflowBundleIO();
			WorkflowBundle bundle = workflowBundleIO.readBundle(getWorkflow().toURL(), null);
			workflowBundleIO.writeBundle(bundle, workflow, RDFXMLReader.APPLICATION_VND_TAVERNA_SCUFL2_WORKFLOW_BUNDLE);
//		}
		return workflow.toURI();
	}

	private synchronized int waitFor(Process process) throws IOException {
		while (true) {
			try {
				wait(500);
			} catch (InterruptedException e) {
			}
			IOUtils.copy(process.getInputStream(), System.out);
			try {
				return process.exitValue();
			} catch (IllegalThreadStateException e) {
			}
		}
	}

	private void assertOutputsEquals(File directory1, File directory2)  {
		File[] directory1Files = directory1.listFiles();
		File[] directory2Files = directory2.listFiles();
		// assert directories contain same number of files
		assertEquals(
				String.format("%s has %s files but %s has %s files",
						directory1.getName(), directory1Files.length, directory2.getName(),
						directory2Files.length), directory1Files.length, directory2Files.length);
		// sort files in directory
		Arrays.sort(directory1Files, NameFileComparator.NAME_SYSTEM_COMPARATOR);
		Arrays.sort(directory2Files, NameFileComparator.NAME_SYSTEM_COMPARATOR);
		for (int i = 0; i < directory1Files.length; i++) {
			assertFilesEqual(directory1Files[i], directory2Files[i], !directory1Files[i].getName().equals(ignorePort));
		}
	}

	private void assertDirectoriesEquals(File directory1, File directory2, boolean checkFileContents)  {
		if (directory1.exists()) {
			assertTrue(String.format("%s exists but %s does not", directory1, directory2), directory2.exists());
		} else {
			assertFalse(String.format("%s does not exists but %s does", directory1, directory2), directory2.exists());
		}
		File[] directory1Files = directory1.listFiles();
		File[] directory2Files = directory2.listFiles();
		// assert directories contain same number of files
		assertEquals(
				String.format("%s has %s files but %s has %s files",
						directory1.getName(), directory1Files.length, directory2.getName(),
						directory2Files.length), directory1Files.length, directory2Files.length);
		// sort files in directory
		Arrays.sort(directory1Files, NameFileComparator.NAME_SYSTEM_COMPARATOR);
		Arrays.sort(directory2Files, NameFileComparator.NAME_SYSTEM_COMPARATOR);
		for (int i = 0; i < directory1Files.length; i++) {
			assertFilesEqual(directory1Files[i], directory2Files[i], checkFileContents);
		}
	}

	private void assertFilesEqual(File file1, File file2, boolean checkFileContents)  {
		if (file1.isHidden()) {
			assertTrue(String.format("%s is hidden but %s is not", file1, file2), file2.isHidden());
		} else {
			assertFalse(String.format("%s is not hidden but %s is", file1, file2), file2.isHidden());
			assertEquals(file1.getName(), file2.getName());
			if (file1.isDirectory()) {
				assertTrue(String.format("%s is a directory but %s is not", file1, file2), file2.isDirectory());
				assertDirectoriesEquals(file1, file2, checkFileContents);
			} else {
				assertFalse(String.format("%s is not a directory but %s is", file1, file2), file2.isDirectory());
				if (isZipFile(file1)) {
					assertZipFilesEqual(file1, file2);
				} else if (checkFileContents) {
					assertEquals(String.format("%s is a different length to %s", file1, file2), file1.length(), file2.length());
					try {
						byte[] byteArray1 = IOUtils.toByteArray(new FileReader(file1));
						byte[] byteArray2 = IOUtils.toByteArray(new FileReader(file2));
						assertArrayEquals(String.format("%s != %s", file1, file2), byteArray1, byteArray2);
					} catch (FileNotFoundException e) {
						fail(e.getMessage());
					} catch (IOException e) {
						fail(e.getMessage());
					}
				}
			}
		}
	}

	private void assertZipFilesEqual(File file1, File file2) {
		ZipFile zipFile1 = null;
		ZipFile zipFile2 = null;
		try {
			zipFile1 = new ZipFile(file1);
			zipFile2 = new ZipFile(file2);
		} catch (Exception e) {
			assertTrue(String.format("%s and %s are not both zip files"), zipFile1 == null);
		}
		if (zipFile1 != null && zipFile2 != null) {
			Enumeration<? extends ZipEntry> entries1 = zipFile1.entries();
			Enumeration<? extends ZipEntry> entries2 = zipFile2.entries();
			while (entries1.hasMoreElements()) {
				assertTrue(entries2.hasMoreElements());
				ZipEntry zipEntry1 = entries1.nextElement();
				ZipEntry zipEntry2 = entries2.nextElement();
				assertEquals(String.format("%s and %s are not both directories", zipEntry1, zipEntry2), zipEntry1.isDirectory(), zipEntry2.isDirectory());
				assertEquals(String.format("%s and %s have different names", zipEntry1, zipEntry2), zipEntry1.getName(), zipEntry2.getName());
				assertEquals(String.format("%s and %s have different sizes", zipEntry1, zipEntry2), zipEntry1.getSize(), zipEntry2.getSize());
				try {
					byte[] byteArray1 = IOUtils.toByteArray(zipFile1.getInputStream(zipEntry1));
					byte[] byteArray2 = IOUtils.toByteArray(zipFile2.getInputStream(zipEntry2));
					assertArrayEquals(String.format("%s != %s", zipEntry1, zipEntry2), byteArray1, byteArray2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			assertFalse(entries2.hasMoreElements());
		}
	}

	private boolean isZipFile(File file) {
		try {
			new ZipFile(file);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static File[] getResources(String directory) {
		return new File(TavernaCommandLineTest.class.getResource("/" + directory).getFile()).listFiles();
	}

	private List<File> getInputs() {
		File inputsDirectory = new File(workflowDirectory, "inputs");
		if (inputsDirectory.exists() && inputsDirectory.isDirectory()) {
			return Arrays.asList(inputsDirectory.listFiles());
		}
		return Collections.emptyList();
	}

	private String getIgnorePort() throws Exception {
		File ignorePort = new File(workflowDirectory, "ignorePort");
		if (ignorePort.exists()) {
			return IOUtils.toString(new FileReader(ignorePort));
		}
		return "";
	}

	private void fetchTaverna(String location, String name) throws Exception {
		File zipFile = new File(buildDirectory, name + ".zip");
		IOUtils.copy(new URL(location).openStream(), new FileOutputStream(zipFile));
		ProcessBuilder processBuilder = new ProcessBuilder("unzip", "-q", name);
		processBuilder.redirectErrorStream(true);
		processBuilder.directory(buildDirectory);
		System.out.println(processBuilder.command());
		Process process = processBuilder.start();
		waitFor(process);
	}

}
