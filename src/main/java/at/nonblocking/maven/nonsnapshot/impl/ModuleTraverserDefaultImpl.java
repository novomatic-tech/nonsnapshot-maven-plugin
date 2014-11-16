package at.nonblocking.maven.nonsnapshot.impl;

import at.nonblocking.maven.nonsnapshot.ModuleTraverser;
import at.nonblocking.maven.nonsnapshot.exception.NonSnapshotPluginException;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a module traverser.
 *
 * @author Juergen Kofler
 */
@Component(role = ModuleTraverser.class, hint = "default")
public class ModuleTraverserDefaultImpl implements ModuleTraverser {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleTraverserDefaultImpl.class);

  @Override
  public List<Model> findAllModules(MavenProject baseProject) {
    List<Model> modelList = new ArrayList<>();

    recursiveFindModules(baseProject.getBasedir(), modelList);

    return modelList;
  }

  private void recursiveFindModules(File baseDir, List<Model> modelList) {
    MavenXpp3ReaderEx reader = new MavenXpp3ReaderEx();
    File pom = new File(baseDir, "pom.xml");

    Model model;

    try (XmlStreamReader xmlStreamReader = ReaderFactory.newXmlReader(pom)) {
      InputSource is = new InputSource();
      model = reader.read(xmlStreamReader, false, is);
      model.setPomFile(pom);
      LOG.debug("Found maven module: {}", pom.getParentFile().getAbsolutePath());

    } catch (IOException | XmlPullParserException e) {
      throw new NonSnapshotPluginException("Failed to load POM: " + pom.getAbsolutePath(), e);
    }

    modelList.add(model);

    for (String modulePath : model.getModules()) {
      File moduleDir = new File(baseDir, modulePath);
      recursiveFindModules(moduleDir, modelList);
    }
  }

}