package com.teamtter.simplelicenses;

import java.util.Comparator;
import java.util.List;

import org.apache.maven.artifact.Artifact;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import lombok.extern.slf4j.Slf4j;

/** GroupId & ArtifactId comparator */
@Slf4j
class GAArtifactComparator implements Comparator<Artifact> {

	@Override
	public int compare(Artifact a0, Artifact a1) {
		
		int groupIdComparison = a0.getGroupId().compareTo(a1.getGroupId());
		if (groupIdComparison != 0) {
			return groupIdComparison;
		} else {
			int artifactIdComparison = a0.getArtifactId().compareTo(a1.getArtifactId());
			if (artifactIdComparison != 0) {
				return artifactIdComparison;
			} else {
				// sanity check to output a WARNING just in case
				if (! (a0.getVersion().equals(a1.getVersion())
						&& a0.getClassifier().equals(a1.getClassifier())) ) {
					log.warn("Maybe a problem with Artifacts comparison: {} - {} ", a0, a1);
				}
				return 0;
			}
		}
	}
}

public class Artifacts2LicensesRepository {
	
	private Multimap<Artifact, String> artifacts2Licenses 
		= TreeMultimap.create(new GAArtifactComparator(), Ordering.natural());
	
	public Artifacts2LicensesRepository() {
	}
	
	public void add(Artifact artifact, List<String> licenses) {
		artifacts2Licenses.putAll(artifact, licenses);
	}

}
