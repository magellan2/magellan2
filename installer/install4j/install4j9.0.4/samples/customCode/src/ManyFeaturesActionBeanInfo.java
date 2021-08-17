import com.install4j.api.beaninfo.*;

// Bean info for the ManyFeaturesAction class
// This example shows many features of bean infos in install4j
@SuppressWarnings("unused")
public class ManyFeaturesActionBeanInfo extends ActionBeanInfo {

    private static final String FILE_PROPERTIES = "File properties";
    private static final String STANDARD_EDITORS = "Standard editors";

    private static final String CONTEXT_ENUM_INTEGER = "enumInteger";

    public ManyFeaturesActionBeanInfo() {
        // Note the last parameter in the super invocation, this sets the customizer that adds another tab to the config
        // panel
        super(
            "Action with many features",
            "This is an action that shows many of the features in the install4j bean system.",
            null, true, false, null,
            ManyFeaturesAction.class,
            ManyFeaturesActionCustomizer.class
        );

        // The customizer will be added after the properties so that the properties list is always shown first 
        setCustomizerPlacement(CustomizerPlacement.AFTER_PROPERTIES);

        addPropertyDescriptor(Install4JPropertyDescriptor.create("hiddenProperty", getBeanClass(),
            "Hidden property",
            "A property whose value will not be logged in the installation log file.")
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("launcher", getBeanClass(),
            "Launcher",
            "A property with a string value that can only contain launcher IDs. In the IDE, the defined launchers are offered to the user.",
            // This done with a special context
            Install4JPropertyDescriptor.CONTEXT_LAUNCHER_ID)
        );

        addStandardEditors();
        addScriptProperties();
        addSimulatedEnum();
        addEnumProperties();
        addFileProperties();
    }

    private void addStandardEditors() {
        addPropertyDescriptor(Install4JPropertyDescriptor.create("color", getBeanClass(),
            "Color",
            "A property with a <tt>java.awt.Color</tt> value." // Note how you can use HTML in the short descriptions
            ).setAllowTextOverride(true) // see the getter
                .setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("font", getBeanClass(),
            "Font",
            "A property with a <tt>java.awt.Font</tt> value."
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("insets", getBeanClass(),
            "Insets",
            "A property with a <tt>java.awt.Insets</tt> value."
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("date", getBeanClass(),
            "Date",
            "A property with a <tt>java.util.Date</tt> value, showing the date part only."
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("dateAndTime", getBeanClass(),
            "Date and time",
            "A property with a <tt>java.awt.Date</tt> value, showing both date and time parts.",
            // This is done with a special context
            Install4JPropertyDescriptor.CONTEXT_DATETIME
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("integer", getBeanClass(),
            "Integer",
            "A property with an <tt>int</tt> value."
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("stringArray", getBeanClass(),
            "String array",
            "A property with a <tt>String[]</tt> value. You can invoke an editor with the \"...\" button or separate array elements with semicolons."
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("booleanValue", getBeanClass(),
            "Boolean value",
            "A property with a <tt>boolean</tt> value."
            ).setAllowTextOverride(true) // see the getter
                .setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("nestedProperty", getBeanClass(),
            "Nested string property",
            "A property that is nested in a boolean property is only shown if the parent property is selected."
            ).setParentProperty("booleanValue")
            // No category is set here, since the category header would be repeated for the nested level 
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("stringToStringMap", getBeanClass(),
            "String to string map",
            "A property with a <tt>java.util.Map</tt> value where both keys and values are strings.",
            // This is done with a special context
            Install4JPropertyDescriptor.CONTEXT_STRING_TO_STRING_MAP
            ).setPropertyCategory(STANDARD_EDITORS)
        );
        addPropertyDescriptor(Install4JPropertyDescriptor.create("multilineString", getBeanClass(),
            "Multiline string",
            "A multiline string. You can invoke the editor with the \"...\" button to get an editor for the string value.",
            // This is done with a special context
            Install4JPropertyDescriptor.CONTEXT_MULTILINE
            ).setPropertyCategory(STANDARD_EDITORS)
        );

    }

    private void addScriptProperties() {
        addPropertyDescriptor(ScriptPropertyDescriptor.create("script", getBeanClass(),
            "A script",
            "The user can enter a script that is then executed at run time. See the install method of the action on how to invoke the script.",
            // this script returns nothing and has no special parameters
            void.class, null)
        );
        addPropertyDescriptor(ScriptPropertyDescriptor.create("scriptWithParameters", getBeanClass(),
            "A script with parameters",
            "The user can enter a script that is then executed at run time. See the install method of the action on how to invoke the script.",
            // this script has a boolean return type and one special parameters
            boolean.class, new ScriptParameter[] {new ScriptParameter("myParameter", String.class)})
        );
    }

    private void addSimulatedEnum() {
        setEnumerationMappers(new EnumerationMapper[] {
            new EnumerationMapper() {
                @Override
                public Class getEnumerationClass() {
                    return int.class;
                }

                @Override
                public EnumerationMapEntry[] getEnumerationMapEntries() {
                    return new EnumerationMapEntry[] {
                        new EnumerationMapEntry("Value 0", 0),
                        new EnumerationMapEntry("Value 1", 1),
                        new EnumerationMapEntry("Value 2", 2),
                        new EnumerationMapEntry("Value 3", 3)
                    };
                }

                @Override
                public String getContext() {
                    return CONTEXT_ENUM_INTEGER;
                }
            }
        });

        addPropertyDescriptor(Install4JPropertyDescriptor.create("simulatedEnum", getBeanClass(),
            "Simulated enum",
            "A property with an integer value that is presented as an enumeration in the IDE.",
            // the context is important, otherwise the IDE will just show the usual integer editor
            // The enumeration mapper above has been registered for the same context
            CONTEXT_ENUM_INTEGER)
        );
    }

    // The file properties are all placed into the  "File properties" category
    private void addFileProperties() {
        addPropertyDescriptor(FilePropertyDescriptor.create("file", getBeanClass(),
            "File property",
            "A packaged file or directory",
            FileSelectionMode.FILES_AND_DIRECTORIES).setPropertyCategory(FILE_PROPERTIES)
        );

        addPropertyDescriptor(FilePropertyDescriptor.create("externalOrPackagedFile", getBeanClass(),
            "External or packaged file",
            "Either an external or a package file, the user can choose which one.",
            FilePropertyDescriptor.CONTEXT_EXTERNAL_OR_INTERNAL).setPropertyCategory(FILE_PROPERTIES)
        );

        // The "setFileContentType" call makes the file editable in the IDE
        addPropertyDescriptor(FilePropertyDescriptor.create("editableExternalTextFile", getBeanClass(),
            "Editable external text file",
            "An external text file that can be edited in the install4j IDE",
            FileSelectionMode.FILES_ONLY, new String[] {"txt"}, "Text files").setFileContentType(FileContentType.TEXT).setPropertyCategory(FILE_PROPERTIES)
        );

        addPropertyDescriptor(FilePropertyDescriptor.create("editableExternalHtmlFile", getBeanClass(),
            "Editable external HTML file",
            "An external HTML file that can be edited in the install4j IDE",
            FileSelectionMode.FILES_ONLY, new String[] {"html", "htm"}, "HTML files").setFileContentType(FileContentType.HTML).setPropertyCategory(FILE_PROPERTIES)
        );

        addPropertyDescriptor(FilePropertyDescriptor.create("localizedExternalFile", getBeanClass(),
            "Editable and localized external HTML file",
            "An external HTML file with localized variants that can be edited in the install4j IDE. You can configure one file for the principal language and one file for each additional language.",
            FileSelectionMode.FILES_ONLY, new String[] {"html", "htm"}, "HTML files").setFileContentType(FileContentType.HTML).setPropertyCategory(FILE_PROPERTIES)
        );

        addPropertyDescriptor(FilePropertyDescriptor.create("multipleFiles", getBeanClass(),
            "Multiple files",
            "A property with an array of files. You can add files in the editor with the \"...\" button or separate multiple files by semicolons.",
            FileSelectionMode.FILES_AND_DIRECTORIES).setPropertyCategory(FILE_PROPERTIES)
        );

    }

    private void addEnumProperties() {
        addPropertyDescriptor(Install4JPropertyDescriptor.create("testEnum", getBeanClass(),
            "Test enum property",
            "A property with an enum value. The selected value controls the visibility of the 3 nested properties.")
                .setAllowTextOverride(true) // see the getter
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("nestedPropertyOne", getBeanClass(),
            "Nested property for value one",
            "A nested property that is only shown if value one is selected for the test enum parent property."
            ).setParentProperty("testEnum").setVisibilityDiscriminator((bean, parentPropertyValue) -> parentPropertyValue == ManyFeaturesAction.TestEnum.ONE)
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("nestedPropertyTwo", getBeanClass(),
            "Nested property for value two",
            "A nested property that is only shown if value two is selected for the test enum parent property."
            ).setParentProperty("testEnum").setVisibilityDiscriminator((bean, parentPropertyValue) -> parentPropertyValue == ManyFeaturesAction.TestEnum.TWO)
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("nestedPropertyThree", getBeanClass(),
            "Nested property for value three",
            "A nested property that is only shown if value three is selected for the test enum parent property."
            ).setParentProperty("testEnum").setVisibilityDiscriminator((bean, parentPropertyValue) -> parentPropertyValue == ManyFeaturesAction.TestEnum.THREE)
        );
    }
}
