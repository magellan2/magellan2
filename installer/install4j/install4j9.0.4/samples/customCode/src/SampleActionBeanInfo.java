import com.install4j.api.beaninfo.*;
import com.install4j.api.beans.Bean;

/**
 * BeanInfo for SampleAction
 */
@SuppressWarnings("unused")
public class SampleActionBeanInfo extends ActionBeanInfo implements BeanValidator {

    private static final String PROPERTY_FAIL = "fail";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_DETAIL = "detail";

    public SampleActionBeanInfo() {
        super("Sample action", "This is just a sample action", "Samples", true, false, null, SampleAction.class);

        addPropertyDescriptor(Install4JPropertyDescriptor.create(PROPERTY_FAIL, getBeanClass(), "Always fail", "If true the action will fail. Please see the error handling properties for options how to handle the failure."));
        addPropertyDescriptor(Install4JPropertyDescriptor.create(PROPERTY_MESSAGE, getBeanClass(), "Display message", "The progress message that the action will display. Cannot be empty."));

        // note the context object at the end
        addPropertyDescriptor(Install4JPropertyDescriptor.create(PROPERTY_DETAIL, getBeanClass(), "Detail messages", "The type of the detail messages that willbe displayed as the action is performed.", DetailEnumerationMapper.DETAIL_CONTEXT));
        // The detail property is enumerated, this means that the user can choose its value from a drop-down list in the install4j IDE
        // enumerated property types require that you specify an enumeration mapper for them
        setEnumerationMappers(new EnumerationMapper[] {new DetailEnumerationMapper()});
    }

    @Override
    public void validateBean(Bean bean) throws BeanValidationException {
        checkNotEmpty(PROPERTY_MESSAGE, bean);
    }

    private static class DetailEnumerationMapper implements EnumerationMapper {

        // a non-default context is required for enumerated properties with the Java type that is already handled by install4j
        private static final String DETAIL_CONTEXT = "detailContext";

        @Override
        public Class getEnumerationClass() {
            return int.class;
        }

        @Override
        public EnumerationMapEntry[] getEnumerationMapEntries() {
            return new EnumerationMapEntry[] {
                // primitive values are wrapped for enumeration entries
                new EnumerationMapEntry("None", SampleAction.DETAIL_NONE),
                new EnumerationMapEntry("Show percent", SampleAction.DETAIL_PERCENT),
                new EnumerationMapEntry("Show counter", SampleAction.DETAIL_COUNTER)
            };
        }

        @Override
        public String getContext() {
            return DETAIL_CONTEXT;
        }
    }
}
