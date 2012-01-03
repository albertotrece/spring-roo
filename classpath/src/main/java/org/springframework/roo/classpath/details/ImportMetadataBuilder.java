package org.springframework.roo.classpath.details;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * Builder for {@link ImportMetadata}.
 * 
 * @author James Tyrrell
 * @since 1.1.1
 */
public class ImportMetadataBuilder extends
        AbstractIdentifiableJavaStructureBuilder<ImportMetadata> {

    // Fields
    private boolean isAsterisk;
    private boolean isStatic;
    private JavaPackage importPackage;
    private JavaType importType;

    /**
     * Builds an import of the given {@link JavaType} for use by the given
     * caller.
     * 
     * @param callerMID the metadata ID of the compilation unit to receive the
     *            import (required)
     * @param typeToImport the type to import (required)
     * @return a non-<code>null</code>, non-static, non-wildcard import
     * @since 1.2.0
     */
    public static ImportMetadata getImport(final String callerMID,
            final JavaType typeToImport) {
        return new ImportMetadataBuilder(callerMID, 0,
                typeToImport.getPackage(), typeToImport, false, false).build();
    }

    public ImportMetadataBuilder(final String declaredbyMetadataId) {
        super(declaredbyMetadataId);
    }

    public ImportMetadataBuilder(final ImportMetadata existing) {
        super(existing);
        this.importPackage = existing.getImportPackage();
        this.importType = existing.getImportType();
        this.isStatic = existing.isStatic();
        this.isAsterisk = existing.isAsterisk();
    }

    public ImportMetadataBuilder(final String declaredbyMetadataId,
            final int modifier, final JavaPackage importPackage,
            final JavaType importType, final boolean isStatic,
            final boolean isAsterisk) {
        this(declaredbyMetadataId);
        setModifier(modifier);
        this.importPackage = importPackage;
        this.importType = importType;
        this.isStatic = isStatic;
        this.isAsterisk = isAsterisk;
    }

    public ImportMetadata build() {
        return new DefaultImportMetadata(getCustomData().build(),
                getDeclaredByMetadataId(), getModifier(), importPackage,
                importType, isStatic, isAsterisk);
    }

    public JavaPackage getImportPackage() {
        return importPackage;
    }

    public void setImportPackage(final JavaPackage importPackage) {
        this.importPackage = importPackage;
    }

    public JavaType getImportType() {
        return importType;
    }

    public void setImportType(final JavaType importType) {
        this.importType = importType;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(final boolean aStatic) {
        isStatic = aStatic;
    }

    public boolean isAsterisk() {
        return isAsterisk;
    }

    public void setAsterisk(final boolean asterisk) {
        isAsterisk = asterisk;
    }
}
