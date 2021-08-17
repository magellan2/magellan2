import com.install4j.api.beaninfo.FormComponentBeanInfo;
import com.install4j.api.beaninfo.Install4JPropertyDescriptor;

/**
 * BeanInfo for SampleFormComponent
 */
@SuppressWarnings("unused")
public class SampleFormComponentBeanInfo extends FormComponentBeanInfo {

    private static final String PROPERTY_LABEL_TEXT = "labelText";
    private static final String PROPERTY_TEXT = "text";

    public SampleFormComponentBeanInfo() {
        super("Sample form component", "The is a sample form component that shows a label and an uneditable text field.", null, null, SampleFormComponent.class);

        addPropertyDescriptor(Install4JPropertyDescriptor.create(PROPERTY_LABEL_TEXT, getBeanClass(), "Label text", "The text that is displayed in the label."));
        addPropertyDescriptor(Install4JPropertyDescriptor.create(PROPERTY_TEXT, getBeanClass(), "Text", "The text that is displayed in the uneditable text field."));
    }
}
