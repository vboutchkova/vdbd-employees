# Improve Readability / Robustness
* **Immutability by Default** For simple DTOs:

  * Use record (Java 16+) or Lombok’s @Value instead of @Data for DTOs when they don’t need to be mutable.

    * **Descriptive Types. Instead of using plain String employeeId / String projectId, define small value objects:


    public record EmployeeId(String value) { public String toString(){return value;} }
    public record ProjectId(String value) { public String toString(){return value;} }

This prevents accidental mixing of IDs and makes methods self-documenting.

* **Validated and Inclusive Period.
  If counting days inclusively is needed, create a helper:


    public static long inclusiveDays(LocalDate a, LocalDate b) { return DAYS.between(a, b) + 1; }

And reuse it in the analyzer.

* **Single Responsibility per Method.** In EmployeePairsAnalyzer, keep:

  * buildIndex(...) (if building index per project/employee);

  * commonDaysForPair(...);

  * findMaxPair(...).

Shorter methods with clear names → easier to review.

* **Streams, but keep them readable.** Avoid deeply nested chains like .map(...).mapToLong(...).sum() in a single line. Use:


    return epp1.periods().stream()
        .flatMapToLong(p1 -> epp2.periods().stream().mapToLong(p2 -> getCommonDays(p1, p2)))
        .sum();

Or even use a double for loop — easier to debug.

* **Avoid „null“ where possible.* In the parser:

  * Centralize „NULL → today“ handling;
  * Gather all supported formats in one place (a list of DateTimeFormatter, loop through them).


* **equals/hashCode** for key classes. In case of using Pair<EmployeeId,EmployeeId> as a key — make sure:

  * It’s normalized (smaller ID goes first), or
  * You use a symmetric comparator.

* **Logging and Errors.** In ProjectsFileParser, return meaningful messages (line number, reason, expected format). In REST layer — respond with 400 and a clear message.

* **Tests.** onsider adding adding:

  * A parameterized test for Period,
  * A test for the parser with various formats and NULL.
 
# Dependency Inversion Principle

Current issue: The high-level business service EmployeePairsAnalyzer depends on the low-level concrete implementation ProjectsFileParser. This violates DIP.
Fix: Make the service depend on an abstraction (interface) and let Spring inject the desired implementation.

* **Interface**
```java
  public interface ProjectsDataParser {
     Map<String, EmployeeProjects> processFile(File file, String dateForNull, String dateFormat);
  }
```

* **CSV implementation**

```java
@Component
public class CsvProjectsDataParser implements ProjectsDataParser {
    @Override
    public Map<String, EmployeeProjects> processFile(File file, String dateForNull, String dateFormat) {
        // CSV parsing logic
    }
}
```



* **High-level service depends on the abstraction**

```java
  @Component
  public class EmployeePairsAnalyzer {
  
      private final ProjectsDataParser parser;
  
      @Autowired
      public EmployeePairsAnalyzer(ProjectsDataParser parser) {
          this.parser = parser;
      }
  
      // ...
  }
```

If you have multiple implementations, mark a default with @Primary or qualify explicitly with constructor EmployeePairsAnalyzer(@Qualifier("csvProjectsDataParser") ProjectsDataParser parser)

# Example of Centralized Date Parsing

```java
    private static final List<DateTimeFormatter> SUPPORTED = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    static LocalDate parseDate(String raw, Clock clock) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("NULL")) {
            return LocalDate.now(clock);
        }
        String s = raw.trim();
        for (var f : SUPPORTED) {
            try { return LocalDate.parse(s, f); } catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Unsupported date: " + raw);
    }
```
