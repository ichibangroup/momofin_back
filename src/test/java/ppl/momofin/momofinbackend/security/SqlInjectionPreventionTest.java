package ppl.momofin.momofinbackend.security;

@ExtendWith(MockitoExtension.class)
class SqlInjectionPreventionTest {
    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private UUID orgId;
    private Organization testOrg;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orgId = UUID.randomUUID();

        // Setup test organization
        testOrg = new Organization("Test Org", "Test Description", "Test Industry", "Test Location");
        testOrg.setOrganizationId(orgId);

        // Setup test admin
        testAdmin = new User(testOrg, "admin", "Admin User", "admin@test.com", "password", "Admin", true);
    }

    @Test
    void createOrganization_WithSqlInjection_ShouldThrowException() {
        // Arrange
        String maliciousName = "company'; DROP TABLE users;--";
        String maliciousDescription = "description'; DELETE FROM organizations;--";

        when(sqlInjectionValidator.containsSqlInjection(maliciousName)).thenReturn(true);
        when(sqlInjectionValidator.containsSqlInjection(maliciousDescription)).thenReturn(true);

        // Act & Assert
        SecurityValidationException exception = assertThrows(SecurityValidationException.class, () ->
                organizationService.createOrganization(maliciousName, maliciousDescription, "Tech", "NY")
        );
        assertEquals("SQL injection detected in input", exception.getMessage());
    }

    @Test
    void createOrganization_WithValidInput_ShouldSucceed() {
        // This test ensures normal organization creation still works
        String validName = "Tech Company Inc";
        String validDescription = "A good company";
        Organization expectedOrg = new Organization(validName, validDescription, "Tech", "NY");

        when(sqlInjectionValidator.containsSqlInjection(any())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(expectedOrg);

        Organization result = organizationService.createOrganization(validName, validDescription, "Tech", "NY");

        assertNotNull(result);
        assertEquals(validName, result.getName());
    }

    @Test
    void updateOrganization_WithSqlInjection_ShouldThrowException() {
        // This test verifies that malicious updates are caught
        String maliciousName = "company'; TRUNCATE TABLE users;--";

        when(sqlInjectionValidator.containsSqlInjection(maliciousName)).thenReturn(true);

        assertThrows(SecurityValidationException.class, () ->
                organizationService.updateOrganization(orgId, maliciousName, "description", "Tech", "NY")
        );
    }
}