import com.install4j.api.beaninfo.Install4JBeanInfo;
import com.install4j.api.beaninfo.Install4JPropertyDescriptor;

/**
 * BeanInfo for SunnySkyBackgroundStyle
 */
@SuppressWarnings("unused")
public class SunnySkyBackgroundStyleBeanInfo extends Install4JBeanInfo {

    public SunnySkyBackgroundStyleBeanInfo() {
        super("Sunny sky background style",
            "A sample style that wraps another style with a sunny sky background.",
            null, true, false, null,
            SunnySkyBackgroundStyle.class
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("nestedStyleId", getBeanClass(), "Nested style",
            "The nested style that will be added in the center",
            Install4JPropertyDescriptor.CONTEXT_STYLE_ID) // With this special context, a drop-down with all styles in the project is shown
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("insets", getBeanClass(), "Insets for nested style",
            "The insets around the nested style in the center that will be used for displaying the graphical element.")
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("numberOfRays", getBeanClass(), "Number of sun rays",
            "The number of arc segments of the radial sun rays.")
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("sunColor", getBeanClass(), "Sun color",
            "The color for the sun disk and the sun rays.")
        );

        addPropertyDescriptor(Install4JPropertyDescriptor.create("skyColor", getBeanClass(), "Sky color",
            "The background color for the sky.")
        );
    }
}
