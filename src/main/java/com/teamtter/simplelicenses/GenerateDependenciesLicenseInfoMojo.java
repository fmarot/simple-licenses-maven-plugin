package com.teamtter.simplelicenses;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Mojo(name = "generate" /** the goal */
		, threadSafe = false /** until proven otherwise, false */
		, defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, requiresProject = true)
@Slf4j
public class GenerateDependenciesLicenseInfoMojo extends AbstractMojo {

	@Parameter(property = "skip", defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject mavenProject;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	@Component
	protected ProjectBuilder projectBuilder;

	// @Parameter(property = "localRepository", required = true, readonly =
	// true)
	// protected ArtifactRepository localRepository;

	@Component
	private BuildContext buildContext;

	private static ObjectMapper jsonMapper;

	static {
		jsonMapper = new ObjectMapper();
		jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
		jsonMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			log.info("Skipping execution due to 'skip' == true");
		} else {
			try {
				generateDependenciesLicenseInfo();
			} catch (ProjectBuildingException e) {
				log.error("", e);
				throw new MojoExecutionException(e.getMessage());
			}
		}
	}

	private void generateDependenciesLicenseInfo() throws ProjectBuildingException {
		Artifacts2LicensesRepository repo = new Artifacts2LicensesRepository();

		ProjectBuildingRequest request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
		// Build the project and get the result

		List<Artifact> runtimeArtifacts = mavenProject.getRuntimeArtifacts();
		for (Artifact artifact : runtimeArtifacts) {
			MavenProject project = projectBuilder.build(artifact, request).getProject();
			List<String> licenses = project.getLicenses();
			log.info("licenses = {}", licenses);
			repo.add(artifact, project.getLicenses());
		}

	}

}
