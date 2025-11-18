# IntelliDoc Professional - Hints Examples

## Complexity Hints Examples

### Java Example

```java
public class UserService {

    // Complexity: O(1) - constant time
    public User findById(Long id) {
        return userRepository.findById(id);
    }

    // Complexity: O(n) - single loop
    public List<User> filterActiveUsers(List<User> users) {
        List<User> active = new ArrayList<>();
        for (User user : users) {
            if (user.isActive()) {
                active.add(user);
            }
        }
        return active;
    }

    // Complexity: O(n log n) - sorting algorithm
    public List<User> sortByName(List<User> users) {
        Collections.sort(users, Comparator.comparing(User::getName));
        return users;
    }

    // Complexity: O(n²) - nested loops (depth 2)
    public List<UserPair> findDuplicates(List<User> users) {
        List<UserPair> duplicates = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                if (users.get(i).getEmail().equals(users.get(j).getEmail())) {
                    duplicates.add(new UserPair(users.get(i), users.get(j)));
                }
            }
        }
        return duplicates;
    }
}
```

### Kotlin Example

```kotlin
class Calculator {

    // Complexity: O(1) - constant time
    fun add(a: Int, b: Int): Int {
        return a + b
    }

    // Complexity: O(n) - single loop
    fun sumList(numbers: List<Int>): Int {
        var sum = 0
        for (num in numbers) {
            sum += num
        }
        return sum
    }

    // Complexity: O(log n) - logarithmic pattern detected
    fun binarySearch(arr: IntArray, target: Int): Int {
        var left = 0
        var right = arr.size - 1
        while (left <= right) {
            val mid = left + (right - left) / 2
            when {
                arr[mid] == target -> return mid
                arr[mid] < target -> left = mid + 1
                else -> right = mid - 1
            }
        }
        return -1
    }
}
```

---

## Security Hints Examples

### Java - SQL Injection

```java
public class UserDao {

    // 🔴 Security: SQL Injection - Use PreparedStatement instead of string concatenation
    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = '" + email + "'";
        return jdbcTemplate.queryForObject(query, new UserRowMapper());
    }

    // ✅ CORRECT - No hint shown
    public User findByEmailSecure(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{email}, new UserRowMapper());
    }
}
```

### Java - Hardcoded Credentials

```java
public class DatabaseConfig {

    // 🔴 Security: Hardcoded credential - Use environment variables or secure vault
    private String password = "mySecretPassword123";

    // 🔴 Security: Hardcoded credential - Use environment variables or secure vault
    private String apiKey = "sk-1234567890abcdef";

    // ✅ CORRECT - No hint shown
    private String passwordFromEnv = System.getenv("DB_PASSWORD");
}
```

### Java - Insecure Random

```java
public class TokenGenerator {

    // 🟠 Security: Insecure Random - Use SecureRandom for cryptographic operations
    public String generateSessionToken() {
        long token = Math.random() * 1000000;
        return String.valueOf(token);
    }

    // ✅ CORRECT - No hint shown
    public String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
```

### Java - Command Injection

```java
public class SystemExecutor {

    // 🔴 Security: Command Injection - Validate and sanitize command parameters
    public String executeCommand(String userInput) {
        String cmd = "ls -la " + userInput;
        return Runtime.getRuntime().exec(cmd);
    }

    // ✅ CORRECT - No hint shown
    public String executeCommandSecure(String userInput) {
        if (!userInput.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid input");
        }
        ProcessBuilder pb = new ProcessBuilder("ls", "-la", userInput);
        return pb.start();
    }
}
```

### Java - XSS Vulnerability

```java
@Controller
public class UserController {

    // 🟠 Security: XSS vulnerability - Escape HTML output
    @GetMapping("/greeting")
    public String greeting(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        response.getWriter().write("<h1>Hello " + name + "</h1>");
    }

    // ✅ CORRECT - No hint shown
    @GetMapping("/greeting-safe")
    public String greetingSafe(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        String escaped = HtmlUtils.htmlEscape(name);
        response.getWriter().write("<h1>Hello " + escaped + "</h1>");
    }
}
```

### Java - Path Traversal

```java
public class FileService {

    // 🟠 Security: Path Traversal - Validate and normalize file paths
    public File readFile(String filename) {
        return new File("/data/" + filename);
    }

    // ✅ CORRECT - No hint shown
    public File readFileSecure(String filename) throws IOException {
        File base = new File("/data/");
        File file = new File(base, filename);
        String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.startsWith(base.getCanonicalPath())) {
            throw new SecurityException("Path traversal detected");
        }
        return file;
    }
}
```

---

### Kotlin - SQL Injection

```kotlin
class UserRepository {

    // 🔴 Security: SQL Injection - Use parameterized queries or exposed/jooq DSL
    fun findByUsername(username: String): User? {
        val query = "SELECT * FROM users WHERE username = '$username'"
        return jdbcTemplate.queryForObject(query, UserRowMapper())
    }

    // ✅ CORRECT - No hint shown
    fun findByUsernameSecure(username: String): User? {
        val query = "SELECT * FROM users WHERE username = ?"
        return jdbcTemplate.queryForObject(query, arrayOf(username), UserRowMapper())
    }
}
```

### Kotlin - Hardcoded Credentials

```kotlin
class ApiClient {

    // 🔴 Security: Hardcoded credential - Use environment variables or secure vault
    private val apiKey = "sk-prod-1234567890"

    // 🔴 Security: Hardcoded credential - Use environment variables or secure vault
    private val secret = "my-secret-token"

    // ✅ CORRECT - No hint shown
    private val apiKeyFromEnv = System.getenv("API_KEY")
}
```

### Kotlin - Command Injection

```kotlin
class ProcessManager {

    // 🔴 Security: Command Injection - Validate and sanitize command parameters
    fun runCommand(input: String): String {
        val command = "bash -c \"echo $input\""
        return Runtime.getRuntime().exec(command).inputStream.bufferedReader().readText()
    }

    // ✅ CORRECT - No hint shown
    fun runCommandSecure(input: String): String {
        val sanitized = input.replace(Regex("[^a-zA-Z0-9]"), "")
        val pb = ProcessBuilder("echo", sanitized)
        return pb.start().inputStream.bufferedReader().readText()
    }
}
```

---

## Visual Representation

### How hints appear in the IDE:

```
┌─────────────────────────────────────────────────────────────────┐
│ File: UserService.java                                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│ public class UserService {                                      │
│                                                                  │
│     ⚙️ Complexity: O(n²) - nested loops (depth 2)              │
│                                                                  │
│     🔴 Security: SQL Injection - Use PreparedStatement          │
│                                                                  │
│     public List<User> findDuplicatesByEmail(String email) {    │
│         String query = "SELECT * FROM users WHERE email = '"    │
│                        + email + "'";                            │
│         List<User> users = executeQuery(query);                 │
│                                                                  │
│         for (int i = 0; i < users.size(); i++) {               │
│             for (int j = i + 1; j < users.size(); j++) {       │
│                 if (users.get(i).equals(users.get(j))) {       │
│                     // ...                                      │
│                 }                                               │
│             }                                                    │
│         }                                                        │
│         return duplicates;                                      │
│     }                                                            │
│ }                                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## Summary of Hint Types

### Complexity Hints:
- ⚙️ O(1) - constant time
- ⚙️ O(log n) - logarithmic pattern detected
- ⚙️ O(n) - single loop
- ⚙️ O(n log n) - sorting algorithm
- ⚙️ O(n²) - nested loops (depth 2)
- ⚙️ O(n³) - nested loops (depth 3+)
- ⚙️ O(2ⁿ) - double recursion
- ⚙️ O(n!) - factorial/permutation pattern

### Security Hints:
- 🔴 CRITICAL: SQL Injection, Hardcoded Credentials, Command Injection
- 🟠 HIGH: XSS, Path Traversal, Insecure Random

### Configuration:
Both hint types can be enabled/disabled from:
**Settings → Tools → IntelliDoc Professional**
- ☑️ Activate/Deactivate complexity hints
- ☑️ Activate/Deactivate security hints
