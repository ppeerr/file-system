package per.demo;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class UserLookupService extends UserPrincipalLookupService {
    @Override
    public UserPrincipal lookupPrincipalByName(String s) throws IOException {
        return null;
    }

    @Override
    public GroupPrincipal lookupPrincipalByGroupName(String s) throws IOException {
        return null;
    }
}
