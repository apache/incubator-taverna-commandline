/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.taverna.commandline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandLineUtils {
	
	public static boolean safeIsSameFile(Path path1, Path path2) throws IOException {
		if (Files.exists(path1) != Files.exists(path2)) {
			// Either both must exist, or both must not exist
			return false;
		}
		// Now somewhat safe to call Files.isSameFile
		return Files.isSameFile(path1, path2);
	}

}
