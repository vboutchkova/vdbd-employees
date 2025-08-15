

## 1. Run Backend
./gradlew bootRun

## Build jar
./gradlew build

## Run JAR after built
java -jar build/libs/SGHLongestPeriod-1.0-SNAPSHOT.jar


## 2. Run Frontend
cd frontend
npm install
ng serve

Backend Endpoints are accessible at http://localhost:8080
Frontend will run at: http://localhost:4200


## Development Notes

### Implementation Decision: Sorted Keys for Pairing

In the pairing logic, the keys from the map (employeesFromFile.keySet()) are sorted once before the nested iteration begins:

This design choice was made intentionally to:

   * Avoid duplicate pairings – Prevents logically identical pairs like (A, B) and (B, A) from being treated as distinct keys.

   * Ensure deterministic iteration – The result will always be the same regardless of HashMap key order.

   * Improve readability – Sorting up-front avoids having to manually compare and normalize every pair within the loop.

Although sorting has complexity O(n log n), the full pairing logic is O(n²) due to the double loop, so the sort does not affect the overall asymptotic complexity.


### Suggestions for Further Improvement:

See [todo.md](./todo.md) for reflections on the task, reasoning and pending improvements.


## Problem Description
Pair of employees who have worked together

Create an application that identifies the pair of employees who have worked
together on common projects for the longest period of time.

Input data:
A CSV file with data in the following format:

EmpID, ProjectID, DateFrom, DateTo

Sample data:
143, 12, 2013-11-01, 2014-01-05
218, 10, 2012-05-16, NULL
143, 10, 2009-01-01, 2011-04-27
...

Sample output:
143, 218, 8

Specific requirements
1) DateTo can be NULL, equivalent to today
2) The input data must be loaded to the program from a CSV file
3) The task solution needs to be uploaded in github.com, repository name must be in format:
   {FirstName}-{LastName}-employees

Bonus points

1) Create an UI:
   The user picks up a file from the file system and, after selecting it, all common projects of the
   pair are displayed in datagrid with the following columns:
   Employee ID #1, Employee ID #2, Project ID, Days worked

2) More than one date format to be supported, extra points will be given if all date formats are
   supported

