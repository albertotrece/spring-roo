package org.springframework.roo.project;

import org.springframework.roo.project.packaging.PackagingProvider;
import org.springframework.roo.support.util.Assert;

/**
 * Provides available project types for the project. Currently only war and jar
 * types are supported, but other types can be added in future. TODO check how
 * this type can/should be replaced by {@link PackagingProvider}
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
public class ProjectType {

    public static final ProjectType JAR = new ProjectType("jar");
    public static final ProjectType WAR = new ProjectType("war");

    // Fields
    private final String name;

    /**
     * Constructor
     * 
     * @param name the name of this type of project (required)
     */
    public ProjectType(final String name) {
        Assert.hasText(name, "Name required");
        this.name = name;
    }

    /**
     * Returns the name of this type of project
     * 
     * @return a non-blank name
     * @deprecated use {@link #getName()} instead
     */
    @Deprecated
    public String getType() {
        return getName();
    }

    /**
     * Returns the name of this type of project
     * 
     * @return a non-blank name
     * @since 1.2.0
     */
    public String getName() {
        return name;
    }
}
