package error;

public class OrganizationNotFoundException extends RuntimeException{
    public OrganizationNotFoundException(String organizationName) {
        super("The organization "+ organizationName + " is not registered to our database");
    }
}
