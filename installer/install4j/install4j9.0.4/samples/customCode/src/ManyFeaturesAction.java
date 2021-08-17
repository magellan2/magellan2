import com.install4j.api.Util;
import com.install4j.api.actions.AbstractInstallAction;
import com.install4j.api.beans.ExternalFile;
import com.install4j.api.beans.LocalizedExternalFile;
import com.install4j.api.beans.PropertyLoggingInterceptor;
import com.install4j.api.beans.ScriptProperty;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.UserCanceledException;

import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;

/* This example shows many features of bean infos in install4j
 * Please see the ManyFeaturesActionBeanInfo class to see how
 * the properties care registered for use in the install4j IDE
 */
public class ManyFeaturesAction extends AbstractInstallAction implements PropertyLoggingInterceptor {

    // This property will not be logged, see the getLogValueForProperty method
    private String hiddenProperty = "this should not be logged";

    private File file;
    private ExternalFile externalOrPackagedFile;
    private ExternalFile editableExternalTextFile;
    private ExternalFile editableExternalHtmlFile;
    // This value must not be null, it has to be initialized
    private LocalizedExternalFile localizedExternalFile = new LocalizedExternalFile();
    private File[] multipleFiles;
    private ExternalFile[] multipleExternalFiles;

    private ScriptProperty script;
    private ScriptProperty scriptWithParameters;
    private TestEnum testEnum = TestEnum.ONE;
    private String nestedPropertyOne = "value for one";
    private String nestedPropertyTwo = "value for two";
    private String nestedPropertyThree = "value for three";
    private int simulatedEnum = 0;

    private Color color;
    private Font font;
    private Insets insets;
    private Date date;
    private Date dateAndTime;
    private Integer integer;
    private String[] stringArray;
    private String launcher;
    // This value has to be initialized. This only works for properties of type LinkedHashMap, the generic signature is not necessary
    private LinkedHashMap<String, String> stringToStringMap = new LinkedHashMap<>();
    private boolean booleanValue = true;
    private String nestedProperty = "nested value";
    private String multilineString;

    // only method in the PropertyLoggingInterceptor interface
    // if a property value should not be written to the installation log file, you can
    // change the logged value here
    @Override
    public Object getLogValueForProperty(String propertyName, Object propertyValue) {
        if (propertyName.equals("hiddenProperty")) {
            return "****";
        } else {
            return propertyValue;
        }
    }

    @Override
    public boolean install(InstallerContext context) throws UserCanceledException {

        try {
            // Execute the script
            context.runScript(script, this);
        } catch (Exception e) {
            // User defined scripts may throw exceptions. It is advisable to
            // log the error and continue
            Util.log(e);
        }

        try {
            // Execute the script, passing the special script parameter
            // the return value is a boolean or null if the script has not been defined
            Object value = context.runScript(scriptWithParameters, this, "parameter value");
            if (value != null) {
                return (Boolean)value;
            } else {
                return true;
            }
        } catch (Exception e) {
            Util.log(e);
            return false;
        }
    }

    public String getHiddenProperty() {
        return hiddenProperty;
    }

    public void setHiddenProperty(String hiddenProperty) {
        this.hiddenProperty = hiddenProperty;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ExternalFile getExternalOrPackagedFile() {
        return externalOrPackagedFile;
    }

    public void setExternalOrPackagedFile(ExternalFile externalOrPackagedFile) {
        this.externalOrPackagedFile = externalOrPackagedFile;
    }

    public ExternalFile getEditableExternalTextFile() {
        return editableExternalTextFile;
    }

    public void setEditableExternalTextFile(ExternalFile editableExternalTextFile) {
        this.editableExternalTextFile = editableExternalTextFile;
    }

    public ExternalFile getEditableExternalHtmlFile() {
        return editableExternalHtmlFile;
    }

    public void setEditableExternalHtmlFile(ExternalFile editableExternalHtmlFile) {
        this.editableExternalHtmlFile = editableExternalHtmlFile;
    }

    public LocalizedExternalFile getLocalizedExternalFile() {
        return localizedExternalFile;
    }

    public void setLocalizedExternalFile(LocalizedExternalFile localizedExternalFile) {
        this.localizedExternalFile = localizedExternalFile;
    }

    public File[] getMultipleFiles() {
        return multipleFiles;
    }

    public void setMultipleFiles(File[] multipleFiles) {
        this.multipleFiles = multipleFiles;
    }

    public ExternalFile[] getMultipleExternalFiles() {
        return multipleExternalFiles;
    }

    public void setMultipleExternalFiles(ExternalFile[] multipleExternalFiles) {
        this.multipleExternalFiles = multipleExternalFiles;
    }

    public ScriptProperty getScript() {
        return script;
    }

    public void setScript(ScriptProperty script) {
        this.script = script;
    }

    public ScriptProperty getScriptWithParameters() {
        return scriptWithParameters;
    }

    public void setScriptWithParameters(ScriptProperty scriptWithParameters) {
        this.scriptWithParameters = scriptWithParameters;
    }

    public TestEnum getTestEnum() {
        // setAllowTextOverride(true) is called in the the property descriptor in the bean info
        return replaceWithTextOverride("testEnum", testEnum, TestEnum.class);
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }

    public String getNestedPropertyOne() {
        return nestedPropertyOne;
    }

    public void setNestedPropertyOne(String nestedPropertyOne) {
        this.nestedPropertyOne = nestedPropertyOne;
    }

    public String getNestedPropertyTwo() {
        return nestedPropertyTwo;
    }

    public void setNestedPropertyTwo(String nestedPropertyTwo) {
        this.nestedPropertyTwo = nestedPropertyTwo;
    }

    public String getNestedPropertyThree() {
        return nestedPropertyThree;
    }

    public void setNestedPropertyThree(String nestedPropertyThree) {
        this.nestedPropertyThree = nestedPropertyThree;
    }

    public int getSimulatedEnum() {
        return simulatedEnum;
    }

    public void setSimulatedEnum(int simulatedEnum) {
        this.simulatedEnum = simulatedEnum;
    }

    public Color getColor() {
        // setAllowTextOverride(true) is called in the the property descriptor in the bean info
        return replaceWithTextOverride("color", color, Color.class);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Insets getInsets() {
        return insets;
    }

    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(Date dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public String getLauncher() {
        return launcher;
    }

    public void setLauncher(String launcher) {
        this.launcher = launcher;
    }

    public LinkedHashMap<String, String> getStringToStringMap() {
        return stringToStringMap;
    }

    public void setStringToStringMap(LinkedHashMap<String, String> stringToStringMap) {
        this.stringToStringMap = stringToStringMap;
    }

    public boolean isBooleanValue() {
        // setAllowTextOverride(true) is called in the the property descriptor in the bean info
        return replaceWithTextOverride("booleanValue", booleanValue, Boolean.class);
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getNestedProperty() {
        return nestedProperty;
    }

    public void setNestedProperty(String nestedProperty) {
        this.nestedProperty = nestedProperty;
    }

    public String getMultilineString() {
        return multilineString;
    }

    public void setMultilineString(String multilineString) {
        this.multilineString = multilineString;
    }

    public enum TestEnum {
        ONE("Value one"), TWO("Value two"), THREE("Value three");

        private String verbose;

        TestEnum(String verbose) {
            this.verbose = verbose;
        }

        @Override
        public String toString() {
            return verbose;
        }
    }

}
