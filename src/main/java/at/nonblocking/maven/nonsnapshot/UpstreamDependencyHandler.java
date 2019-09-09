/*
 * Copyright 2012-2013 the original author or authors.
 *
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
 */
package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotDependencyResolverException;
import at.nonblocking.maven.nonsnapshot.model.MavenArtifact;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

/**
 * Handler for upstream dependencies
 *
 * @author Juergen Kofler
 */
public interface UpstreamDependencyHandler {

  /**
   * Process the upstream dependency list from the configuration and create objects from it.
   *
   * @param upstreamDependencies List<>
   * @param mavenPomHandler MavenPomHandler
   * @param repositorySystem RepositorySystem
   * @param repositorySystemSession RepositorySystemSession
   * @param remoteRepositories List<RemoteRepository>
   * @return List<ProcessedUpstreamDependency>
   * @throws NonSnapshotDependencyResolverException
   */
  List<ProcessedUpstreamDependency> processDependencyList(List upstreamDependencies,
                                                          MavenPomHandler mavenPomHandler,
                                                          RepositorySystem repositorySystem,
                                                          RepositorySystemSession repositorySystemSession,
                                                          List<RemoteRepository> remoteRepositories);

  /**
   * Find a matching upstream dependency declaration for given maven artifact.
   * <br/>
   * Used to decide if a given dependency is an upstream dependency.
   *
   * @param mavenArtifact MavenArtifact
   * @param upstreamDependencies List<ProcessedUpstreamDependency>
   * @return ProcessedUpstreamDependency
   */
  ProcessedUpstreamDependency findMatch(MavenArtifact mavenArtifact, List<ProcessedUpstreamDependency> upstreamDependencies);

  /**
   * Try to find a newer version for given upstream dependency. Return null if no newer exits.
   *
   * @param mavenArtifact MavenArtifact
   * @param upstreamDependency UpstreamDependency
   * @param repositorySystem RepositorySystem
   * @param repositorySystemSession RepositorySystemSession
   * @param remoteRepositories List<RemoteRepository>
   * @return String
   * @throws NonSnapshotDependencyResolverException
   */
  String resolveLatestVersion(MavenArtifact mavenArtifact, ProcessedUpstreamDependency upstreamDependency,
                              RepositorySystem repositorySystem, RepositorySystemSession repositorySystemSession,
                              List<RemoteRepository> remoteRepositories) throws NonSnapshotDependencyResolverException;

}
