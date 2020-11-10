package per.demo.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class UploadFileContentValidator {

    private static final int maxContentSizeBytes = 1024 * 16; //16 kb

    public static void check(String fileName, String content) {
        checkFileName(fileName);
        checkContent(content);
    }

    public static void checkFileName(String fileName) {
        Validate.isTrue(StringUtils.isNotBlank(fileName), "fileName can't be blank");
        Validate.isTrue(fileName.matches("[a-zA-Z0-9]*"), "fileName must contains only latin letters and numbers");
    }

    private static void checkContent(String content) {
        Validate.isTrue(StringUtils.isNotBlank(content), "content can't be blank");
        Validate.isTrue(
                content.getBytes().length <= maxContentSizeBytes,
                "content size must not greater " + maxContentSizeBytes
        );
    }
}
