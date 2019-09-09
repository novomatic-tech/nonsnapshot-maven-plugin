package at.nonblocking.maven.nonsnapshot;

import at.nonblocking.maven.nonsnapshot.impl.UpstreamDependencyHandlerDefaultImpl;
import at.nonblocking.maven.nonsnapshot.model.MavenArtifact;
import at.nonblocking.maven.nonsnapshot.version.Version;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class UpstreamDependencyHandlerDefaultImplTest {

  @Test
  public void testProcessDependencyList() {
    List<String> upstreamDependencyStrings = Arrays.asList(
        "at.nonblocking:test2:1.2.4",
        "at.nonblocking:test3:1.5",
        "at.nonblocking:test-*",
        "at.*:test5:LATEST",
        "at.*:*:1"
    );

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    List<ProcessedUpstreamDependency> upstreamDependencies = handler.processDependencyList(upstreamDependencyStrings, null, null, null, null);

    assertNotNull(upstreamDependencies);
    assertEquals(5, upstreamDependencies.size());

    assertEquals("at\\.nonblocking", upstreamDependencies.get(0).getGroupPattern().pattern());
    assertEquals("test2", upstreamDependencies.get(0).getArtifactPattern().pattern());
    assertEquals(new Integer(1), upstreamDependencies.get(0).getVersion().getMajorVersion());
    assertEquals(new Integer(2), upstreamDependencies.get(0).getVersion().getMiddleVersion());
    assertEquals(new Integer(4), upstreamDependencies.get(0).getVersion().getMinorVersion());

    assertEquals("at\\.nonblocking", upstreamDependencies.get(1).getGroupPattern().pattern());
    assertEquals("test3", upstreamDependencies.get(1).getArtifactPattern().pattern());
    assertEquals(new Integer(1), upstreamDependencies.get(1).getVersion().getMajorVersion());
    assertEquals(new Integer(5), upstreamDependencies.get(1).getVersion().getMiddleVersion());
    assertNull(upstreamDependencies.get(1).getVersion().getMinorVersion());

    assertEquals("at\\.nonblocking", upstreamDependencies.get(2).getGroupPattern().pattern());
    assertEquals("test-.*", upstreamDependencies.get(2).getArtifactPattern().pattern());
    assertNull(upstreamDependencies.get(2).getVersion().getMajorVersion());
    assertNull(upstreamDependencies.get(2).getVersion().getMiddleVersion());
    assertNull(upstreamDependencies.get(2).getVersion().getMinorVersion());

    assertEquals("at\\..*", upstreamDependencies.get(3).getGroupPattern().pattern());
    assertEquals("test5", upstreamDependencies.get(3).getArtifactPattern().pattern());
    assertNull(upstreamDependencies.get(3).getVersion().getMajorVersion());
    assertNull(upstreamDependencies.get(3).getVersion().getMiddleVersion());
    assertNull(upstreamDependencies.get(3).getVersion().getMinorVersion());

    assertEquals("at\\..*", upstreamDependencies.get(4).getGroupPattern().pattern());
    assertEquals(".*", upstreamDependencies.get(4).getArtifactPattern().pattern());
    assertEquals(new Integer(1), upstreamDependencies.get(4).getVersion().getMajorVersion());
    assertNull(upstreamDependencies.get(4).getVersion().getMiddleVersion());
    assertNull(upstreamDependencies.get(4).getVersion().getMinorVersion());
  }

  @Test
  public void testMatches() {

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(null, null, null));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    List<ProcessedUpstreamDependency> upstreamDependencies = Arrays.asList(dep1, dep2);

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    assertEquals(dep1, handler.findMatch(new MavenArtifact("at.nonblocking", "test1", null), upstreamDependencies));
    assertEquals(dep2, handler.findMatch(new MavenArtifact("at.test", "foo", null), upstreamDependencies));
    assertNull(handler.findMatch(new MavenArtifact("de.nonblocking", "test1", null), upstreamDependencies));
  }

  @Test
  public void testResolveLatestVersionNoVersionConstraint() throws Exception {
    RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
    RepositorySystemSession mockRepositorySystemSession = mock(RepositorySystemSession.class);
    List<RemoteRepository> mockRemoteRepositories = new ArrayList<>();

    org.eclipse.aether.version.Version mockVersion1 = mock(org.eclipse.aether.version.Version.class);
    when(mockVersion1.toString()).thenReturn("1.1.1-1234");

    VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
    result.setVersions(Arrays.asList(mockVersion1));

    ArgumentCaptor<VersionRangeRequest> captor = ArgumentCaptor.forClass(VersionRangeRequest.class);
    when(mockRepositorySystem.resolveVersionRange(any(RepositorySystemSession.class), captor.capture())).thenReturn(result);

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(null, null, null));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    MavenArtifact mavenArtifact = new MavenArtifact("at.nonblocking", "test1", "1.0.0");

    String version = handler.resolveLatestVersion(mavenArtifact, dep1, mockRepositorySystem, mockRepositorySystemSession, mockRemoteRepositories);

    assertNotNull(version);
    assertEquals("1.1.1-1234", version);
    assertEquals("at.nonblocking:test1:jar:(1.0.0,) < []", captor.getValue().toString());
  }

  @Test
  public void testResolveLatestVersionMajorVersionConstraint() throws Exception {
    RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
    RepositorySystemSession mockRepositorySystemSession = mock(RepositorySystemSession.class);
    List<RemoteRepository> mockRemoteRepositories = new ArrayList<>();

    org.eclipse.aether.version.Version mockVersion1 = mock(org.eclipse.aether.version.Version.class);
    when(mockVersion1.toString()).thenReturn("2.1.1-1234");

    VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
    result.setVersions(Arrays.asList(mockVersion1));

    ArgumentCaptor<VersionRangeRequest> captor = ArgumentCaptor.forClass(VersionRangeRequest.class);
    when(mockRepositorySystem.resolveVersionRange(any(RepositorySystemSession.class), captor.capture())).thenReturn(result);

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(2, null, null));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    MavenArtifact mavenArtifact = new MavenArtifact("at.nonblocking", "test1", "1.0.0");

    String version = handler.resolveLatestVersion(mavenArtifact, dep1, mockRepositorySystem, mockRepositorySystemSession, mockRemoteRepositories);

    assertNotNull(version);
    assertEquals("2.1.1-1234", version);
    assertEquals("at.nonblocking:test1:jar:(1.0.0,3.0.0) < []", captor.getValue().toString());
  }

  @Test
  public void testResolveLatestVersionMajorVersionConstraintNoMatch() throws Exception {
    RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
    RepositorySystemSession mockRepositorySystemSession = mock(RepositorySystemSession.class);
    List<RemoteRepository> mockRemoteRepositories = new ArrayList<>();

    org.eclipse.aether.version.Version mockVersion1 = mock(org.eclipse.aether.version.Version.class);
    when(mockVersion1.toString()).thenReturn("1.1.1-1234");

    VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
    result.setVersions(Arrays.asList(mockVersion1));

    ArgumentCaptor<VersionRangeRequest> captor = ArgumentCaptor.forClass(VersionRangeRequest.class);
    when(mockRepositorySystem.resolveVersionRange(any(RepositorySystemSession.class), captor.capture())).thenReturn(result);

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(2, null, null));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    MavenArtifact mavenArtifact = new MavenArtifact("at.nonblocking", "test1", "1.0.0");

    String version = handler.resolveLatestVersion(mavenArtifact, dep1, mockRepositorySystem, mockRepositorySystemSession, mockRemoteRepositories);

    assertNull(version);
    assertEquals("at.nonblocking:test1:jar:(1.0.0,3.0.0) < []", captor.getValue().toString());
  }

  @Test
  public void testResolveLatestVersionMajorMinorVersionConstraint() throws Exception {
    RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
    RepositorySystemSession mockRepositorySystemSession = mock(RepositorySystemSession.class);
    List<RemoteRepository> mockRemoteRepositories = new ArrayList<>();

    org.eclipse.aether.version.Version mockVersion1 = mock(org.eclipse.aether.version.Version.class);
    when(mockVersion1.toString()).thenReturn("2.3.9-1234");

    VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
    result.setVersions(Arrays.asList(mockVersion1));

    ArgumentCaptor<VersionRangeRequest> captor = ArgumentCaptor.forClass(VersionRangeRequest.class);
    when(mockRepositorySystem.resolveVersionRange(any(RepositorySystemSession.class), captor.capture())).thenReturn(result);

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(2, 3, null));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    MavenArtifact mavenArtifact = new MavenArtifact("at.nonblocking", "test1", "1.0.0");

    String version = handler.resolveLatestVersion(mavenArtifact, dep1, mockRepositorySystem, mockRepositorySystemSession, mockRemoteRepositories);

    assertNotNull(version);
    assertEquals("2.3.9-1234", version);
    assertEquals("at.nonblocking:test1:jar:(1.0.0,2.4.0) < []", captor.getValue().toString());
  }

  @Test
  public void testResolveLatestVersionMajorMinorIncrementVersionConstraint() throws Exception {
    RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
    RepositorySystemSession mockRepositorySystemSession = mock(RepositorySystemSession.class);
    List<RemoteRepository> mockRemoteRepositories = new ArrayList<>();

    org.eclipse.aether.version.Version mockVersion1 = mock(org.eclipse.aether.version.Version.class);
    when(mockVersion1.toString()).thenReturn("2.3.4-1234");

    VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
    result.setVersions(Arrays.asList(mockVersion1));

    ArgumentCaptor<VersionRangeRequest> captor = ArgumentCaptor.forClass(VersionRangeRequest.class);
    when(mockRepositorySystem.resolveVersionRange(any(RepositorySystemSession.class), captor.capture())).thenReturn(result);

    ProcessedUpstreamDependency dep1 = new ProcessedUpstreamDependency(Pattern.compile("at\\.nonblocking"), Pattern.compile("test1"), new Version(2, 3, 4));
    ProcessedUpstreamDependency dep2 = new ProcessedUpstreamDependency(Pattern.compile("at\\..*"), Pattern.compile(".*"), new Version(null, null, null));

    UpstreamDependencyHandler handler = new UpstreamDependencyHandlerDefaultImpl();

    MavenArtifact mavenArtifact = new MavenArtifact("at.nonblocking", "test1", "1.0.0");

    String version = handler.resolveLatestVersion(mavenArtifact, dep1, mockRepositorySystem, mockRepositorySystemSession, mockRemoteRepositories);

    assertNotNull(version);
    assertEquals("2.3.4-1234", version);
    assertEquals("at.nonblocking:test1:jar:(1.0.0,2.3.5) < []", captor.getValue().toString());
  }

}
